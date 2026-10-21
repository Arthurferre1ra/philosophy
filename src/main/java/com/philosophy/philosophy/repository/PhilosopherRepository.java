package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.model.Philosopher;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PhilosopherRepository {
    // A agregação correlacionada mantém uma linha por filósofo, sem GROUP BY no perfil inteiro.
    private static final String SELECT = """
            SELECT f.id, f.nome, f.data_nascimento, f.data_falecimento,
                   f.biografia, f.principais_ideias, f.anos_vida, f.cor,
                   COALESCE(p.nome, 'Sem período') AS periodo,
                   COALESCE((
                       SELECT STRING_AGG(e.nome, ', ' ORDER BY e.nome)
                       FROM filosofo_escola fe
                       JOIN escola_pensamento e ON e.id = fe.escola_id
                       WHERE fe.filosofo_id = f.id
                   ), 'Sem escola') AS escola
            FROM filosofo f
            LEFT JOIN periodo_historico p ON p.id = f.periodo_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public PhilosopherRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Philosopher> findAll() {
        return jdbc.query(SELECT + " ORDER BY f.criado_em DESC, f.id DESC", LibraryRowMapper::philosopher);
    }

    public Optional<Philosopher> findById(long id) {
        return jdbc.query(SELECT + " WHERE f.id = :id", Map.of("id", id), LibraryRowMapper::philosopher)
                .stream().findFirst();
    }

    public long insert(PhilosopherForm form, long periodId) {
        var parameters = new MapSqlParameterSource()
                .addValue("name", form.name())
                .addValue("biography", form.biography())
                .addValue("quote", form.quote())
                .addValue("periodId", periodId)
                .addValue("years", form.years())
                .addValue("color", form.color() == null ? "" : form.color());
        return Objects.requireNonNull(jdbc.queryForObject("""
                INSERT INTO filosofo (nome, biografia, principais_ideias, periodo_id, anos_vida, cor)
                VALUES (:name, :biography, :quote, :periodId, :years, :color)
                RETURNING id
                """, parameters, Long.class));
    }

    public void associateSchool(long philosopherId, long schoolId) {
        jdbc.update("""
                INSERT INTO filosofo_escola (filosofo_id, escola_id)
                VALUES (:philosopherId, :schoolId)
                ON CONFLICT (filosofo_id, escola_id) DO NOTHING
                """, Map.of("philosopherId", philosopherId, "schoolId", schoolId));
    }
}
