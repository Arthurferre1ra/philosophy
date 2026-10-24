package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.model.Period;
import com.philosophy.philosophy.model.Philosopher;
import com.philosophy.philosophy.model.School;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PhilosopherRepository {
    private static final String SELECT = """
            SELECT DISTINCT f FROM Philosopher f
            LEFT JOIN FETCH f.period
            LEFT JOIN FETCH f.schools
            """;

    private final EntityManager entityManager;

    public List<com.philosophy.philosophy.dto.Philosopher> findAll() {
        return entityManager.createQuery(SELECT + " ORDER BY f.createdAt DESC, f.id DESC", Philosopher.class)
                .getResultList().stream().map(LibraryMapper::philosopher).toList();
    }

    public Optional<com.philosophy.philosophy.dto.Philosopher> findById(UUID id) {
        return entityManager.createQuery(SELECT + " WHERE f.id = :id", Philosopher.class)
                .setParameter("id", id).getResultList().stream().findFirst().map(LibraryMapper::philosopher);
    }

    public UUID insert(PhilosopherForm form, UUID periodId) {
        var philosopher = new Philosopher();
        philosopher.setName(form.name());
        philosopher.setBiography(form.biography());
        philosopher.setQuote(form.quote());
        philosopher.setPeriod(entityManager.getReference(Period.class, periodId));
        philosopher.setYears(form.years());
        philosopher.setColor(form.color() == null ? "" : form.color());
        entityManager.persist(philosopher);
        return philosopher.getId();
    }

    public void associateSchool(UUID philosopherId, UUID schoolId) {
        var philosopher = entityManager.getReference(Philosopher.class, philosopherId);
        philosopher.getSchools().add(entityManager.getReference(School.class, schoolId));
    }
}
