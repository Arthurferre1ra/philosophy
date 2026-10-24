package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.School;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SchoolRepository {
    private final EntityManager entityManager;
    private final NamedParameterJdbcTemplate jdbc;

    public List<School> findAll() {
        return entityManager.createQuery("""
                SELECT s FROM School s ORDER BY s.name, s.id
                """, com.philosophy.philosophy.model.School.class).getResultList().stream()
                .map(school -> new School(school.getId(), LibraryMapper.initialFor(school.getName()), school.getName(),
                        LibraryMapper.defaultText(school.getOrigin(),
                                LibraryMapper.formatHistoricalRange(school.getStartYear(), school.getEndYear())),
                        LibraryMapper.defaultText(school.getDescription(), "Descrição a completar."),
                        LibraryMapper.colorFor(school.getName())))
                .toList();
    }

    public UUID findOrCreate(String name) {
        // O DEFAULT do banco gera o UUID; o upsert evita duplicatas concorrentes.
        return Objects.requireNonNull(jdbc.queryForObject("""
                INSERT INTO escola_pensamento (nome) VALUES (:name)
                ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome
                RETURNING id
                """, Map.of("name", name), UUID.class));
    }
}
