package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.model.Philosopher;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Locale;

final class LibraryRowMapper {
    private LibraryRowMapper() {
    }

    static Philosopher philosopher(ResultSet rs, int rowNum) throws SQLException {
        String period = rs.getString("periodo");
        return new Philosopher(
                rs.getLong("id"), rs.getString("nome"), period,
                defaultText(rs.getString("anos_vida"), formatLifeRange(
                        rs.getObject("data_nascimento", LocalDate.class),
                        rs.getObject("data_falecimento", LocalDate.class))),
                rs.getString("escola"),
                defaultText(rs.getString("principais_ideias"), "Ideias a completar"),
                rs.getString("cor") == null ? colorFor(period) : rs.getString("cor"),
                defaultText(rs.getString("biografia"), "Perfil em construção."));
    }

    static String displayOriginOrRange(ResultSet rs) throws SQLException {
        String origin = normalize(rs.getString("origem"));
        if (origin != null) {
            return origin;
        }
        return formatHistoricalRange(readInteger(rs, "ano_inicio"), readInteger(rs, "ano_fim"));
    }

    static String formatLifeRange(LocalDate birth, LocalDate death) {
        if (birth == null && death == null) {
            return "Dados a completar";
        }
        if (birth == null) {
            return "Até " + death.getYear();
        }
        if (death == null) {
            return "Nasc. " + birth.getYear();
        }
        return birth.getYear() + " - " + death.getYear();
    }

    static String formatHistoricalRange(Integer start, Integer end) {
        if (start == null && end == null) {
            return "Sem recorte definido";
        }
        if (start == null) {
            return "Até " + formatYear(end);
        }
        if (end == null) {
            return formatYear(start) + " - hoje";
        }
        return formatYear(start) + " - " + formatYear(end);
    }

    private static String formatYear(Integer year) {
        if (year == null) {
            return "";
        }
        if (year < 0) {
            return Math.abs(year) + " a.C.";
        }
        return String.valueOf(year);
    }

    static Integer readInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    static String defaultText(String value, String fallback) {
        String normalized = normalize(value);
        return normalized == null ? fallback : normalized;
    }

    static String initialFor(String value) {
        String normalized = normalize(value);
        return normalized == null ? "?" : normalized.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    static String colorFor(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "blue";
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.contains("moderna") || lower.contains("modernidade") || lower.contains("racional")) {
            return "blue";
        }
        if (lower.contains("medieval") || lower.contains("terracota")) {
            return "terracotta";
        }
        if (lower.contains("existencial") || lower.contains("contempor")) {
            return "green";
        }
        if (lower.contains("ilumin")) {
            return "lilac";
        }
        if (lower.contains("arist")) {
            return "rose";
        }
        return "";
    }
}
