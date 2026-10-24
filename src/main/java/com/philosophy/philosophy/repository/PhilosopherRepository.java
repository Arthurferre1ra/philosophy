package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.exception.PhilosopherNotFoundException;
import com.philosophy.philosophy.model.Period;
import com.philosophy.philosophy.model.Philosopher;
import com.philosophy.philosophy.model.School;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PhilosopherRepository {
    private final EntityManager entityManager;
    private final PhilosopherJpaRepository jpa;

    public List<com.philosophy.philosophy.dto.Philosopher> findAll() {
        return jpa.findAllWithRelationships().stream().map(LibraryMapper::philosopher).toList();
    }

    public Optional<com.philosophy.philosophy.dto.Philosopher> findById(UUID id) {
        return jpa.findWithRelationshipsById(id).map(LibraryMapper::philosopher);
    }

    public Optional<PhilosopherForm> findFormById(UUID id) {
        return jpa.findWithRelationshipsById(id).map(entity -> new PhilosopherForm(
                entity.getName(), entity.getPeriod() == null ? null : entity.getPeriod().getName(),
                entity.getSchools().stream().map(School::getName).sorted()
                        .collect(Collectors.joining(", ")),
                entity.getYears(), entity.getQuote(), entity.getColor(), entity.getBiography()));
    }

    public boolean existsWithNameInPeriod(String name, String period, UUID excludedId) {
        return excludedId == null
                ? jpa.existsByNameIgnoreCaseAndPeriod_NameIgnoreCase(name, period)
                : jpa.existsByNameIgnoreCaseAndPeriod_NameIgnoreCaseAndIdNot(name, period, excludedId);
    }

    public UUID insert(PhilosopherForm form, UUID periodId) {
        var philosopher = new Philosopher();
        applyForm(philosopher, form, periodId);
        return jpa.save(philosopher).getId();
    }

    public void update(UUID id, PhilosopherForm form, UUID periodId, UUID schoolId) {
        var philosopher = jpa.findWithRelationshipsById(id)
                .orElseThrow(() -> new PhilosopherNotFoundException(id));
        applyForm(philosopher, form, periodId);
        if (schoolId != null) {
            philosopher.getSchools().clear();
            philosopher.getSchools().add(entityManager.getReference(School.class, schoolId));
        }
        jpa.save(philosopher);
    }

    public void delete(UUID id) {
        var philosopher = jpa.findWithRelationshipsById(id)
                .orElseThrow(() -> new PhilosopherNotFoundException(id));
        jpa.delete(philosopher);
    }

    private void applyForm(Philosopher philosopher, PhilosopherForm form, UUID periodId) {
        philosopher.setName(form.name());
        philosopher.setBiography(form.biography());
        philosopher.setQuote(form.quote());
        philosopher.setPeriod(entityManager.getReference(Period.class, periodId));
        philosopher.setYears(form.years());
        philosopher.setColor(form.color() == null ? "" : form.color());
    }

    public void associateSchool(UUID philosopherId, UUID schoolId) {
        var philosopher = entityManager.getReference(Philosopher.class, philosopherId);
        philosopher.getSchools().add(entityManager.getReference(School.class, schoolId));
    }
}
