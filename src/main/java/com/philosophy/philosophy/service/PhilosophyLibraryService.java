package com.philosophy.philosophy.service;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.exception.PhilosopherNotFoundException;
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
        UUID periodId = periods.findOrCreate(form.period());
        UUID schoolId = schools.findOrCreate(form.school());
        UUID philosopherId = philosophers.insert(form, periodId);
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
