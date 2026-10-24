package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.Period;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PeriodRepository {
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;

    public List<Period> findAll() {
        var periods = entityManager.createQuery("""
                SELECT p FROM Period p ORDER BY p.startYear NULLS LAST, p.name, p.id
                """, com.philosophy.philosophy.model.Period.class).getResultList();
        var result = new ArrayList<Period>(periods.size());
        for (int index = 0; index < periods.size(); index++) {
            var period = periods.get(index);
            result.add(new Period(period.getId(), String.format("%02d", index + 1), period.getName(),
                    LibraryMapper.defaultText(period.getDescription(), "Descrição a completar."),
                    LibraryMapper.formatHistoricalRange(period.getStartYear(), period.getEndYear()),
                    LibraryMapper.colorFor(period.getName())));
        }
        return result;
    }

    public UUID findOrCreate(String name) {
        // O upsert atômico preserva a unicidade mesmo com cadastros concorrentes.
        // Neste caminho SQL, o DEFAULT gen_random_uuid() do banco gera o ID.
        return Objects.requireNonNull(jdbc.queryForObject("""
                INSERT INTO periodo_historico (nome) VALUES (:name)
                ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome
                RETURNING id
                """, Map.of("name", name), UUID.class));
    }
}
