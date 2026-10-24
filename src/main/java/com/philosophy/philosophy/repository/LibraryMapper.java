package com.philosophy.philosophy.repository;

import com.philosophy.philosophy.dto.Philosopher;
import com.philosophy.philosophy.model.School;
import java.time.LocalDate;
import java.util.Locale;
import java.util.stream.Collectors;

final class LibraryMapper {
    private LibraryMapper() {
    }

    static Philosopher philosopher(com.philosophy.philosophy.model.Philosopher entity) {
        String period = entity.getPeriod() == null ? "Sem período" : entity.getPeriod().getName();
        String schools = entity.getSchools().stream().map(School::getName).sorted()
                .collect(Collectors.joining(", "));
        return new Philosopher(
                entity.getId(), entity.getName(), period,
                defaultText(entity.getYears(), formatLifeRange(entity.getBirthDate(), entity.getDeathDate())),
                schools.isEmpty() ? "Sem escola" : schools,
                defaultText(entity.getQuote(), "Ideias a completar"),
                entity.getColor() == null ? colorFor(period) : entity.getColor(),
                defaultText(entity.getBiography(), "Perfil em construção."));
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
