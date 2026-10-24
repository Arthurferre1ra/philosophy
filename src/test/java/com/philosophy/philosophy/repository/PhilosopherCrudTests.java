package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.exception.DuplicatePhilosopherException;
import com.philosophy.philosophy.exception.PhilosopherNotFoundException;
import com.philosophy.philosophy.service.PhilosophyLibraryService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** JPA e Service reais; somente os upserts específicos de PostgreSQL são substituídos. */
@SpringBootTest
@Transactional
class PhilosopherCrudTests {
    @Autowired PhilosophyLibraryService service;
    @Autowired PhilosopherRepository philosophers;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;
    @MockitoBean PeriodRepository periods;
    @MockitoBean SchoolRepository schools;

    private UUID periodId;
    private UUID schoolId;

    @BeforeEach
    void prepareCatalogs() {
        periodId = UUID.randomUUID();
        schoolId = UUID.randomUUID();
        jdbc.update("INSERT INTO periodo_historico (id, nome) VALUES (?, 'Contemporânea')", periodId);
        jdbc.update("INSERT INTO escola_pensamento (id, nome) VALUES (?, 'Filosofia política')", schoolId);
        when(periods.findOrCreate("Contemporânea")).thenReturn(periodId);
        when(schools.findOrCreate("Filosofia política")).thenReturn(schoolId);
    }

    @Test
    void createEditAndDeletePersistRelationshipsAndKeepSharedCatalogs() {
        var saved = service.addPhilosopher(form("Hannah Arendt", "Contemporânea", "Filosofia política"));
        flushAndClear();
        assertThat(jdbc.queryForObject("SELECT escola_id FROM filosofo_escola WHERE filosofo_id = ?",
                UUID.class, saved.getId())).isEqualTo(schoolId);

        UUID newPeriod = UUID.randomUUID();
        UUID newSchool = UUID.randomUUID();
        jdbc.update("INSERT INTO periodo_historico (id, nome) VALUES (?, 'Modernidade')", newPeriod);
        jdbc.update("INSERT INTO escola_pensamento (id, nome) VALUES (?, 'Racionalismo')", newSchool);
        when(periods.findOrCreate("Modernidade")).thenReturn(newPeriod);
        when(schools.findOrCreate("Racionalismo")).thenReturn(newSchool);
        var edited = form("Nome editado", "Modernidade", "Racionalismo");
        service.updatePhilosopher(saved.getId(), edited);
        flushAndClear();
        assertThat(service.findPhilosopherForm(saved.getId())).isEqualTo(edited);
        assertThat(jdbc.queryForObject("SELECT periodo_id FROM filosofo WHERE id = ?", UUID.class, saved.getId()))
                .isEqualTo(newPeriod);
        assertThat(jdbc.queryForList("SELECT escola_id FROM filosofo_escola WHERE filosofo_id = ?", UUID.class, saved.getId()))
                .containsExactly(newSchool);

        service.deletePhilosopher(saved.getId());
        flushAndClear();
        assertThat(philosophers.findById(saved.getId())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM filosofo_escola WHERE filosofo_id = ?", Long.class, saved.getId())).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM escola_pensamento", Long.class)).isEqualTo(2L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM periodo_historico", Long.class)).isEqualTo(2L);
    }

    @Test
    void duplicateNamesInSamePeriodAreRejectedButOwnNameAndOtherPeriodsAreAllowed() {
        var saved = service.addPhilosopher(form("Hannah Arendt", "Contemporânea", "Filosofia política"));
        flushAndClear();
        assertThatThrownBy(() -> service.addPhilosopher(form("HANNAH ARENDT", "contemporânea", "Filosofia política")))
                .isInstanceOf(DuplicatePhilosopherException.class);
        assertThat(philosophers.existsWithNameInPeriod("hannah arendt", "CONTEMPORÂNEA", saved.getId())).isFalse();
        assertThat(philosophers.existsWithNameInPeriod("Hannah Arendt", "Modernidade", null)).isFalse();
    }

    @Test
    void editingUnchangedProfileKeepsAllExistingSchoolLinksAndRawOptionalValues() {
        var saved = service.addPhilosopher(new PhilosopherForm("Teste", "Contemporânea", "Filosofia política",
                null, "Ideia", null, null));
        flushAndClear();
        UUID secondSchool = UUID.randomUUID();
        jdbc.update("INSERT INTO escola_pensamento (id, nome) VALUES (?, 'Outra escola')", secondSchool);
        jdbc.update("INSERT INTO filosofo_escola (filosofo_id, escola_id) VALUES (?, ?)", saved.getId(), secondSchool);
        var form = service.findPhilosopherForm(saved.getId());
        assertThat(form.years()).isNull();
        assertThat(form.biography()).isNull();
        service.updatePhilosopher(saved.getId(), form);
        flushAndClear();
        assertThat(jdbc.queryForList("SELECT escola_id FROM filosofo_escola WHERE filosofo_id = ?", UUID.class, saved.getId()))
                .containsExactlyInAnyOrder(schoolId, secondSchool);
    }

    @Test
    void missingRecordCannotBeEditedOrDeleted() {
        UUID missing = UUID.randomUUID();
        assertThatThrownBy(() -> service.updatePhilosopher(missing, form("Ausente", "Contemporânea", "Filosofia política")))
                .isInstanceOf(PhilosopherNotFoundException.class);
        assertThatThrownBy(() -> service.deletePhilosopher(missing))
                .isInstanceOf(PhilosopherNotFoundException.class);
        verifyNoInteractions(periods, schools);
    }

    @Test
    void editingCannotReuseAnotherPhilosophersNameInTheSamePeriod() {
        service.addPhilosopher(form("Primeiro", "Contemporânea", "Filosofia política"));
        var second = service.addPhilosopher(form("Segundo", "Contemporânea", "Filosofia política"));
        flushAndClear();
        assertThatThrownBy(() -> service.updatePhilosopher(second.getId(), form("PRIMEIRO", "Contemporânea", "Filosofia política")))
                .isInstanceOf(DuplicatePhilosopherException.class);
        assertThat(service.findPhilosopher(second.getId()).getName()).isEqualTo("Segundo");
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static PhilosopherForm form(String name, String period, String school) {
        return new PhilosopherForm(name, period, school, "1900 - 1980", "Uma ideia", "green", "Biografia");
    }
}
