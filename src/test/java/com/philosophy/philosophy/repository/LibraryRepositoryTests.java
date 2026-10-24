package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.Philosopher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LibraryRepositoryTests {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PhilosopherRepository philosophers;
    @Autowired private PeriodRepository periods;
    @Autowired private SchoolRepository schools;

    @BeforeEach
    void clearRows() {
        jdbc.update("DELETE FROM filosofo_escola");
        jdbc.update("DELETE FROM filosofo");
        jdbc.update("DELETE FROM escola_pensamento");
        jdbc.update("DELETE FROM periodo_historico");
    }

    @Test
    void readsPreserveProfilesWithoutRelationshipsAndMissingIds() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO filosofo (id, nome) VALUES (?, 'Sem vínculos')", id);
        Philosopher philosopher = philosophers.findById(id).orElseThrow();
        assertThat(philosopher.getId()).isEqualTo(id);
        assertThat(philosopher.getPeriod()).isEqualTo("Sem período");
        assertThat(philosopher.getSchool()).isEqualTo("Sem escola");
        assertThat(philosopher.getYears()).isEqualTo("Dados a completar");
        assertThat(philosophers.findById(UUID.randomUUID())).isEmpty();
        assertThat(philosophers.findAll()).hasSize(1);
    }

    @Test
    void aggregatesSchoolsInOrderWithoutDuplicatingPhilosophers() {
        UUID periodId = UUID.randomUUID();
        UUID firstSchoolId = UUID.randomUUID();
        UUID secondSchoolId = UUID.randomUUID();
        UUID philosopherId = UUID.randomUUID();
        jdbc.update("INSERT INTO periodo_historico (id, nome) VALUES (?, 'Antiguidade')", periodId);
        jdbc.update("INSERT INTO escola_pensamento (id, nome) VALUES (?, 'Zeta'), (?, 'Alfa')", firstSchoolId, secondSchoolId);
        jdbc.update("INSERT INTO filosofo (id, nome, periodo_id, anos_vida, cor) VALUES (?, 'Um Nome', ?, '400 - 300 a.C.', 'rose')", philosopherId, periodId);
        jdbc.update("INSERT INTO filosofo_escola (filosofo_id, escola_id) VALUES (?, ?), (?, ?)", philosopherId, firstSchoolId, philosopherId, secondSchoolId);
        var profiles = philosophers.findAll();
        assertThat(profiles).hasSize(1);
        assertThat(profiles.getFirst().getSchool()).isEqualTo("Alfa, Zeta");
        assertThat(profiles.getFirst().getYears()).isEqualTo("400 - 300 a.C.");
        assertThat(profiles.getFirst().getColor()).isEqualTo("rose");
        assertThat(profiles.getFirst().getInitials()).isEqualTo("UN");
    }

    @Test
    void catalogsHandleNullYearsAndHistoricalRanges() {
        jdbc.update("INSERT INTO periodo_historico (nome, ano_inicio, ano_fim) VALUES ('Antiguidade', -600, 476), ('Sem datas', NULL, NULL)");
        jdbc.update("INSERT INTO escola_pensamento (nome, origem, ano_inicio) VALUES ('Estoicismo', NULL, -300)");
        assertThat(periods.findAll()).extracting(p -> p.name()).containsExactly("Antiguidade", "Sem datas");
        assertThat(periods.findAll().getFirst().range()).isEqualTo("600 a.C. - 476");
        assertThat(schools.findAll().getFirst().period()).isEqualTo("300 a.C. - hoje");
    }
}
