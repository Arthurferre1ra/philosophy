package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.model.Period;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class PeriodRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public PeriodRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Period> findAll() {
        return jdbc.query("""
                SELECT nome, descricao, ano_inicio, ano_fim
                FROM periodo_historico
                ORDER BY ano_inicio NULLS LAST, nome, id
                """, (rs, rowNum) -> new Period(
                String.format("%02d", rowNum + 1), rs.getString("nome"),
                LibraryRowMapper.defaultText(rs.getString("descricao"), "Descrição a completar."),
                LibraryRowMapper.formatHistoricalRange(LibraryRowMapper.readInteger(rs, "ano_inicio"),
                        LibraryRowMapper.readInteger(rs, "ano_fim")),
                LibraryRowMapper.colorFor(rs.getString("nome"))));
    }

    public long findOrCreate(String name) {
        // O upsert devolve o ID também quando outra transação cadastra o mesmo nome.
        return Objects.requireNonNull(jdbc.queryForObject("""
                INSERT INTO periodo_historico (nome) VALUES (:name)
                ON CONFLICT (nome) DO UPDATE SET nome = EXCLUDED.nome
                RETURNING id
                """, Map.of("name", name), Long.class));
    }
}
