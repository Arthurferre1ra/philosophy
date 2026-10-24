package com.philosophy.philosophy.dto;

import java.util.Locale;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Philosopher {

    private UUID id;
    private String name;
    private String initials;
    private String period;
    private String years;
    private String school;
    private String quote;
    private String color;
    private String biography;

    public Philosopher(UUID id, String name, String period, String years, String school, String quote, String color, String biography) {
        this.id = id;
        this.name = name;
        this.initials = buildInitials(name);
        this.period = period;
        this.years = years;
        this.school = school;
        this.quote = quote;
        this.color = color;
        this.biography = biography;
    }

    public static String buildInitials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    public void setName(String name) {
        this.name = name;
        this.initials = buildInitials(name);
    }
}
