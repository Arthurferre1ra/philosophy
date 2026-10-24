package com.philosophy.philosophy.controller;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercita o formulário, controller, transação e PostgreSQL em um schema isolado. */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "PG_TEST_URL", matches = ".+")
class PostgresRegistrationTests {
    private static final String SCHEMA = "philosophy_http_test_" + UUID.randomUUID().toString().replace("-", "");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getenv("PG_TEST_URL"));
        properties.add("spring.datasource.username", PostgresRegistrationTests::username);
        properties.add("spring.datasource.password", PostgresRegistrationTests::password);
        properties.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        properties.add("spring.datasource.hikari.schema", () -> SCHEMA);
        properties.add("spring.sql.init.mode", () -> "never");
        properties.add("spring.flyway.enabled", () -> true);
        properties.add("spring.flyway.schemas", () -> SCHEMA);
        properties.add("spring.flyway.default-schema", () -> SCHEMA);
    }

    @AfterAll
    static void removeTestSchema() throws Exception {
        try (var connection = DriverManager.getConnection(System.getenv("PG_TEST_URL"), username(), password());
             var statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
        }
    }

    @Test
    void formSavesToPostgresAndRemainsUsableAfterClearingAllRecords() throws Exception {
        jdbc.execute("TRUNCATE filosofo_escola, filosofo, escola_pensamento, periodo_historico");

        mvc.perform(get("/filosofos/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<input(?=[^>]*name=\"period\")(?=[^>]*list=\"period-options\")[^>]*>.*")));

        mvc.perform(post("/filosofos")
                        .param("name", "  Primeiro cadastro  ")
                        .param("period", "Novo período").param("school", "Nova escola")
                        .param("years", "1900 - 1980").param("quote", "Ideia persistida")
                        .param("color", "").param("biography", "Biografia persistida"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/filosofos"))
                .andExpect(flash().attributeExists("message"));

        // Uma conexão independente comprova o COMMIT, além da leitura pelo serviço.
        var external = new JdbcTemplate(new DriverManagerDataSource(System.getenv("PG_TEST_URL"), username(), password()));
        var row = external.queryForMap("SELECT * FROM " + SCHEMA + ".filosofo WHERE nome = ?", "Primeiro cadastro");
        assertThat(row.get("id")).isInstanceOf(UUID.class);
        assertThat(row.get("periodo_id")).isInstanceOf(UUID.class);
        assertThat(row).containsEntry("nome", "Primeiro cadastro")
                .containsEntry("anos_vida", "1900 - 1980")
                .containsEntry("principais_ideias", "Ideia persistida")
                .containsEntry("cor", "")
                .containsEntry("biografia", "Biografia persistida");
        assertThat(external.queryForObject("SELECT COUNT(*) FROM " + SCHEMA + ".filosofo_escola", Long.class)).isEqualTo(1L);
        assertThat(external.queryForObject("SELECT nome FROM " + SCHEMA + ".periodo_historico", String.class)).isEqualTo("Novo período");
        assertThat(external.queryForObject("SELECT nome FROM " + SCHEMA + ".escola_pensamento", String.class)).isEqualTo("Nova escola");

        for (String path : new String[]{"/", "/filosofos", "/filosofos/" + row.get("id")}) {
            mvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(content().string(containsString("Primeiro cadastro")));
        }
        mvc.perform(get("/filosofos"))
                .andExpect(content().string(containsString("/filosofos/" + row.get("id"))));

        mvc.perform(post("/filosofos").param("name", "Inválido"))
                .andExpect(status().isBadRequest());
        assertThat(external.queryForObject("SELECT COUNT(*) FROM " + SCHEMA + ".filosofo", Long.class)).isEqualTo(1L);

        UUID id = (UUID) row.get("id");
        mvc.perform(get("/filosofos/{id}/editar", id))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Primeiro cadastro")));
        mvc.perform(post("/filosofos/{id}/editar", id)
                        .param("name", "Perfil editado").param("period", "Outro período")
                        .param("school", "Outra escola").param("years", "1901 - 1981")
                        .param("quote", "Ideia editada").param("color", "green").param("biography", "Resumo editado"))
                .andExpect(status().is3xxRedirection());
        var edited = external.queryForMap("SELECT f.nome, f.anos_vida, p.nome AS periodo, e.nome AS escola FROM "
                + SCHEMA + ".filosofo f JOIN " + SCHEMA + ".periodo_historico p ON p.id = f.periodo_id JOIN "
                + SCHEMA + ".filosofo_escola fe ON fe.filosofo_id = f.id JOIN " + SCHEMA
                + ".escola_pensamento e ON e.id = fe.escola_id WHERE f.id = ?", id);
        assertThat(edited).containsEntry("nome", "Perfil editado").containsEntry("anos_vida", "1901 - 1981")
                .containsEntry("periodo", "Outro período").containsEntry("escola", "Outra escola");
        mvc.perform(post("/filosofos").param("name", "PERFIL EDITADO").param("period", "Outro período")
                        .param("school", "Outra escola").param("quote", "Duplicado"))
                .andExpect(status().isBadRequest()).andExpect(model().attributeHasFieldErrors("philosopher", "name"));
        mvc.perform(post("/filosofos/{id}/excluir", id)).andExpect(status().is3xxRedirection());
        assertThat(external.queryForObject("SELECT COUNT(*) FROM " + SCHEMA + ".filosofo", Long.class)).isZero();
        assertThat(external.queryForObject("SELECT COUNT(*) FROM " + SCHEMA + ".filosofo_escola", Long.class)).isZero();
        assertThat(external.queryForObject("SELECT COUNT(*) FROM " + SCHEMA + ".escola_pensamento", Long.class)).isEqualTo(2L);
        assertThat(external.queryForObject("SELECT COUNT(*) FROM " + SCHEMA + ".periodo_historico", Long.class)).isEqualTo(2L);
    }

    private static String username() {
        return System.getenv().getOrDefault("PG_TEST_USERNAME", "postgres");
    }

    private static String password() {
        return System.getenv().getOrDefault("PG_TEST_PASSWORD", "postgres");
    }
}
