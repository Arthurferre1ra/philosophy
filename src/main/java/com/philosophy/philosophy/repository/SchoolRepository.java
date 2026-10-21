package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.model.School;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class SchoolRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public SchoolRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<School> findAll() {
        return jdbc.query("""
                SELECT nome, descricao, origem, ano_inicio, ano_fim
                FROM escola_pensamento
                ORDER BY nome, id
                """, (rs, rowNum) -> new School(
                LibraryRowMapper.initialFor(rs.getString("nome")), rs.getString("nome"),
                LibraryRowMapper.displayOriginOrRange(rs),
                LibraryRowMapper.defaultText(rs.getString("descricao"), "Descrição a completar."),
                LibraryRowMapper.colorFor(rs.getString("nome"))));
    }

    public long findOrCreate(String name) {
        return Objects.requireNonNull(jdbc.queryForObject("""
                INSERT INTO escola_pensamento (nome) VALUES (:name)
                ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome
                RETURNING id
                """, Map.of("name", name), Long.class));
    }
}
