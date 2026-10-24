package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.model.Period;
import com.philosophy.philosophy.model.Philosopher;
import com.philosophy.philosophy.model.School;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EntityPersistenceTests {
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired PhilosopherRepository philosophers;
    @Autowired PeriodRepository periods;
    @Autowired SchoolRepository schools;

    @Test
    void generatesUuidsAndPersistsRelationshipsThroughTheRepository() {
        var period = new Period();
        period.setName("Contemporânea");
        var school = new School();
        school.setName("Filosofia política");
        assertThat(period.getId()).isNull();
        assertThat(school.getId()).isNull();
        entityManager.persist(period);
        entityManager.persist(school);

        var form = new PhilosopherForm("Hannah Arendt", period.getName(), school.getName(),
                "1906 - 1975", "Pensar.", "blue", "Resumo");
        UUID id = philosophers.insert(form, period.getId());
        philosophers.associateSchool(id, school.getId());
        philosophers.associateSchool(id, school.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(java.util.List.of(id, period.getId(), school.getId()))
                .doesNotHaveDuplicates().allSatisfy(uuid -> assertThat(uuid.version()).isEqualTo(4));
        var loaded = philosophers.findById(id).orElseThrow();
        assertThat(loaded.getId()).isEqualTo(id);
        assertThat(loaded.getInitials()).isEqualTo("HA");
        assertThat(loaded.getPeriod()).isEqualTo(period.getName());
        assertThat(loaded.getSchool()).isEqualTo(school.getName());
        assertThat(loaded.getYears()).isEqualTo(form.years());
        assertThat(loaded.getQuote()).isEqualTo(form.quote());
        assertThat(loaded.getColor()).isEqualTo(form.color());
        assertThat(loaded.getBiography()).isEqualTo(form.biography());
        assertThat(periods.findAll()).extracting(com.philosophy.philosophy.dto.Period::id)
                .contains(period.getId());
        assertThat(schools.findAll()).extracting(com.philosophy.philosophy.dto.School::id)
                .contains(school.getId());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM filosofo_escola WHERE filosofo_id = ?",
                Long.class, id)).isEqualTo(1L);

        entityManager.remove(entityManager.find(Philosopher.class, id));
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(Period.class, period.getId())).isNotNull();
        assertThat(entityManager.find(School.class, school.getId())).isNotNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM filosofo_escola WHERE filosofo_id = ?",
                Long.class, id)).isZero();
    }

    @Test
    void updatesAuditTimestampWithoutReplacingTheIdOrCreationTimestamp() {
        var philosopher = new Philosopher();
        philosopher.setName("Nome inicial");
        assertThat(philosopher.getId()).isNull();
        entityManager.persist(philosopher);
        entityManager.flush();
        UUID id = philosopher.getId();
        entityManager.clear();

        var loaded = entityManager.find(Philosopher.class, id);
        var createdAt = loaded.getCreatedAt();
        var updatedAt = loaded.getUpdatedAt();
        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();
        loaded.setName("Nome atualizado");
        entityManager.flush();
        entityManager.clear();

        var updated = entityManager.find(Philosopher.class, id);
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
        assertThat(updated.getName()).isEqualTo("Nome atualizado");
    }
}
