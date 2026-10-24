package com.philosophy.philosophy.service;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.exception.PhilosopherNotFoundException;
import com.philosophy.philosophy.exception.DuplicatePhilosopherException;
import com.philosophy.philosophy.dto.Period;
import com.philosophy.philosophy.dto.Philosopher;
import com.philosophy.philosophy.dto.School;
import com.philosophy.philosophy.repository.PeriodRepository;
import com.philosophy.philosophy.repository.PhilosopherRepository;
import com.philosophy.philosophy.repository.SchoolRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class PhilosophyLibraryService {
    private final PhilosopherRepository philosophers;
    private final SchoolRepository schools;
    private final PeriodRepository periods;

    public List<Philosopher> listPhilosophers() {
        return philosophers.findAll();
    }

    public Philosopher findPhilosopher(@NotNull UUID id) {
        return philosophers.findById(id).orElseThrow(() -> new PhilosopherNotFoundException(id));
    }

    @Transactional
    public Philosopher addPhilosopher(@NotNull @Valid PhilosopherForm form) {
        requireUniqueProfile(form, null);
        UUID periodId = periods.findOrCreate(form.period());
        UUID schoolId = schools.findOrCreate(form.school());
        UUID philosopherId = philosophers.insert(form, periodId);
        philosophers.associateSchool(philosopherId, schoolId);
        return philosophers.findById(philosopherId)
                .orElseThrow(() -> new IllegalStateException("Filósofo salvo não foi encontrado"));
    }

    public PhilosopherForm findPhilosopherForm(@NotNull UUID id) {
        return philosophers.findFormById(id).orElseThrow(() -> new PhilosopherNotFoundException(id));
    }

    @Transactional
    public Philosopher updatePhilosopher(@NotNull UUID id, @NotNull @Valid PhilosopherForm form) {
        var previous = findPhilosopherForm(id);
        requireUniqueProfile(form, id);
        UUID periodId = periods.findOrCreate(form.period());
        UUID schoolId = form.school().equals(previous.school()) ? null : schools.findOrCreate(form.school());
        philosophers.update(id, form, periodId, schoolId);
        return findPhilosopher(id);
    }

    @Transactional
    public void deletePhilosopher(@NotNull UUID id) {
        // Escolas e períodos são compartilhados e permanecem disponíveis no catálogo.
        philosophers.delete(id);
    }

    private void requireUniqueProfile(PhilosopherForm form, UUID excludedId) {
        // Regra de negócio: um mesmo nome não pode se repetir no mesmo período,
        // desconsiderando maiúsculas/minúsculas e o próprio registro na edição.
        if (philosophers.existsWithNameInPeriod(form.name(), form.period(), excludedId)) {
            throw new DuplicatePhilosopherException();
        }
    }

    public List<School> listSchools() {
        return schools.findAll();
    }

    public List<Period> listPeriods() {
        return periods.findAll();
    }
}
