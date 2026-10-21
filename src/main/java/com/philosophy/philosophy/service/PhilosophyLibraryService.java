package com.philosophy.philosophy.service;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.exception.PhilosopherNotFoundException;
import com.philosophy.philosophy.model.Period;
import com.philosophy.philosophy.model.Philosopher;
import com.philosophy.philosophy.model.School;
import com.philosophy.philosophy.repository.PeriodRepository;
import com.philosophy.philosophy.repository.PhilosopherRepository;
import com.philosophy.philosophy.repository.SchoolRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class PhilosophyLibraryService {
    private final PhilosopherRepository philosophers;
    private final SchoolRepository schools;
    private final PeriodRepository periods;

    public PhilosophyLibraryService(PhilosopherRepository philosophers, SchoolRepository schools,
                                    PeriodRepository periods) {
        this.philosophers = philosophers;
        this.schools = schools;
        this.periods = periods;
    }

    public List<Philosopher> listPhilosophers() {
        return philosophers.findAll();
    }

    public Philosopher findPhilosopher(@Positive long id) {
        return philosophers.findById(id).orElseThrow(() -> new PhilosopherNotFoundException(id));
    }

    @Transactional
    public Philosopher addPhilosopher(@NotNull @Valid PhilosopherForm form) {
        long periodId = periods.findOrCreate(form.period());
        long schoolId = schools.findOrCreate(form.school());
        long philosopherId = philosophers.insert(form, periodId);
        philosophers.associateSchool(philosopherId, schoolId);
        return philosophers.findById(philosopherId)
                .orElseThrow(() -> new IllegalStateException("Filósofo salvo não foi encontrado"));
    }

    public List<School> listSchools() {
        return schools.findAll();
    }

    public List<Period> listPeriods() {
        return periods.findAll();
    }
}
