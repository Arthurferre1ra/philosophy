package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.service.PhilosophyLibraryService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

/** Executar somente contra um banco de testes: cria e remove um schema exclusivo. */
@EnabledIfEnvironmentVariable(named = "PG_TEST_URL", matches = ".+")
class PostgresPersistenceTests {
    private JdbcTemplate admin;
    private String schema;
    private DriverManagerDataSource dataSource;
    private AnnotationConfigApplicationContext context;
    private PhilosophyLibraryService service;

    @BeforeEach
    void setUp() {
        admin = new JdbcTemplate(dataSource(null));
        schema = "philosophy_test_" + UUID.randomUUID().toString().replace("-", "");
        admin.execute("CREATE SCHEMA " + schema);
        dataSource = dataSource(schema);
        migrations().migrate();
        context = new AnnotationConfigApplicationContext();
        context.registerBean(DataSource.class, () -> dataSource);
        context.register(PersistenceConfiguration.class);
        context.refresh();
        service = context.getBean(PhilosophyLibraryService.class);
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
        if (admin != null && schema != null) admin.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
    }

    @Test
    void migrationsAndRegistrationPersistAllEditableFields() {
        assertThat(service.listPhilosophers()).hasSize(6);
        var form = form("D'Ávila", "Período de teste", "Escola de teste");
        var saved = service.addPhilosopher(form);
        var loaded = service.findPhilosopher(saved.getId());
        assertThat(loaded.getName()).isEqualTo(form.name());
        assertThat(loaded.getYears()).isEqualTo(form.years());
        assertThat(loaded.getColor()).isEqualTo(form.color());
        assertThat(loaded.getSchool()).isEqualTo(form.school());
        assertThat(loaded.getPeriod()).isEqualTo(form.period());
        assertThat(loaded.getQuote()).isEqualTo(form.quote());
        assertThat(loaded.getBiography()).isEqualTo(form.biography());
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.update("UPDATE periodo_historico SET descricao = 'Edição preservada' WHERE nome = 'Antiguidade'");
        assertThat(migrations().migrate().migrationsExecuted).isZero();
        assertThat(jdbc.queryForObject("SELECT descricao FROM periodo_historico WHERE nome = 'Antiguidade'", String.class))
                .isEqualTo("Edição preservada");
    }

    @Test
    void concurrentRegistrationsReuseCatalogRows() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> service.addPhilosopher(form("Primeiro", "Mesmo período", "Mesma escola")));
            var second = executor.submit(() -> service.addPhilosopher(form("Segundo", "Mesmo período", "Mesma escola")));
            assertThat(first.get(15, TimeUnit.SECONDS).getId()).isNotEqualTo(second.get(15, TimeUnit.SECONDS).getId());
        }
        var jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM periodo_historico WHERE nome = 'Mesmo período'", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM escola_pensamento WHERE nome = 'Mesma escola'", Long.class)).isEqualTo(1L);
    }

    @Test
    void databaseFailureRollsBackCatalogsAndPhilosopher() {
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("ALTER TABLE filosofo_escola ADD CONSTRAINT reject_test_link CHECK (escola_id < 0) NOT VALID");
        assertThatThrownBy(() -> service.addPhilosopher(form("Falha", "Período rollback", "Escola rollback")))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM filosofo WHERE nome = 'Falha'", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM periodo_historico WHERE nome = 'Período rollback'", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM escola_pensamento WHERE nome = 'Escola rollback'", Long.class)).isZero();
    }

    private Flyway migrations() {
        return Flyway.configure().dataSource(dataSource).defaultSchema(schema)
                .locations("classpath:db/migration").cleanDisabled(true).load();
    }

    private static DriverManagerDataSource dataSource(String schema) {
        var ds = new DriverManagerDataSource(System.getenv("PG_TEST_URL"),
                System.getenv().getOrDefault("PG_TEST_USERNAME", "postgres"),
                System.getenv().getOrDefault("PG_TEST_PASSWORD", "postgres"));
        if (schema != null) {
            var properties = new Properties();
            properties.setProperty("currentSchema", schema);
            ds.setConnectionProperties(properties);
        }
        return ds;
    }

    private static PhilosopherForm form(String name, String period, String school) {
        return new PhilosopherForm(name, period, school, "1900 - 1980", "Uma ideia", "green", "Uma biografia");
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @Import({PhilosopherRepository.class, SchoolRepository.class, PeriodRepository.class, PhilosophyLibraryService.class})
    static class PersistenceConfiguration {
        @Bean NamedParameterJdbcTemplate jdbc(DataSource ds) { return new NamedParameterJdbcTemplate(ds); }
        @Bean DataSourceTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean static MethodValidationPostProcessor validation() { return new MethodValidationPostProcessor(); }
    }
}
