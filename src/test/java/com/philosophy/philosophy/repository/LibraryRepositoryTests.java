package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.model.Philosopher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryRepositoryTests {
    private EmbeddedDatabase database;
    private JdbcTemplate jdbc;
    private SingleConnectionDataSource connection;
    private PhilosopherRepository philosophers;
    private PeriodRepository periods;
    private SchoolRepository schools;

    @BeforeAll
    void setUp() throws Exception {
        database = new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2).build();
        // H2 mantém comparadores de CHECK ligados à sessão que criou a constraint.
        connection = new SingleConnectionDataSource(database.getConnection(), true);
        jdbc = new JdbcTemplate(connection);
        jdbc.execute("SET MODE PostgreSQL");
        new ResourceDatabasePopulator(
                new ClassPathResource("db/migration/V1__create_library_schema.sql"),
                new ClassPathResource("db/migration/V3__persist_profile_fields_and_add_indexes.sql"))
                .execute(connection);
        var namedJdbc = new NamedParameterJdbcTemplate(connection);
        philosophers = new PhilosopherRepository(namedJdbc);
        periods = new PeriodRepository(namedJdbc);
        schools = new SchoolRepository(namedJdbc);
    }

    @BeforeEach
    void clearRows() {
        jdbc.update("DELETE FROM filosofo_escola");
        jdbc.update("DELETE FROM filosofo");
        jdbc.update("DELETE FROM escola_pensamento");
        jdbc.update("DELETE FROM periodo_historico");
    }

    @AfterAll
    void tearDown() {
        connection.destroy();
        database.shutdown();
    }

    @Test
    void readsPreserveProfilesWithoutRelationshipsAndMissingIds() {
        jdbc.update("INSERT INTO filosofo (id, nome) VALUES (1, 'Sem vínculos')");
        Philosopher philosopher = philosophers.findById(1).orElseThrow();
        assertThat(philosopher.getPeriod()).isEqualTo("Sem período");
        assertThat(philosopher.getSchool()).isEqualTo("Sem escola");
        assertThat(philosopher.getYears()).isEqualTo("Dados a completar");
        assertThat(philosophers.findById(999)).isEmpty();
        assertThat(philosophers.findAll()).hasSize(1);
    }

    @Test
    void aggregatesSchoolsInOrderWithoutDuplicatingPhilosophers() {
        jdbc.update("INSERT INTO periodo_historico (id, nome) VALUES (1, 'Antiguidade')");
        jdbc.update("INSERT INTO escola_pensamento (id, nome) VALUES (1, 'Zeta'), (2, 'Alfa')");
        jdbc.update("INSERT INTO filosofo (id, nome, periodo_id, anos_vida, cor) VALUES (1, 'Um Nome', 1, '400 - 300 a.C.', 'rose')");
        jdbc.update("INSERT INTO filosofo_escola (filosofo_id, escola_id) VALUES (1, 1), (1, 2)");
        var profiles = philosophers.findAll();
        assertThat(profiles).hasSize(1);
        assertThat(profiles.getFirst().getSchool()).isEqualTo("Alfa, Zeta");
        assertThat(profiles.getFirst().getYears()).isEqualTo("400 - 300 a.C.");
        assertThat(profiles.getFirst().getColor()).isEqualTo("rose");
        assertThat(profiles.getFirst().getInitials()).isEqualTo("UN");
    }

    @Test
    void catalogsHandleNullYearsAndHistoricalRanges() {
        jdbc.update("INSERT INTO periodo_historico (id, nome, ano_inicio, ano_fim) VALUES (1, 'Antiguidade', -600, 476), (2, 'Sem datas', NULL, NULL)");
        jdbc.update("INSERT INTO escola_pensamento (nome, origem, ano_inicio) VALUES ('Estoicismo', NULL, -300)");
        assertThat(periods.findAll()).extracting(p -> p.name()).containsExactly("Antiguidade", "Sem datas");
        assertThat(periods.findAll().getFirst().range()).isEqualTo("600 a.C. - 476");
        assertThat(schools.findAll().getFirst().period()).isEqualTo("300 a.C. - hoje");
    }
}
