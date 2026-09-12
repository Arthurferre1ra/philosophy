package com.philosophy.philosophy.model;

public class Philosopher {

    private Long id;
    private String name;
    private String initials;
    private String period;
    private String years;
    private String school;
    private String quote;
    private String color;
    private String biography;

    public Philosopher() {
    }

    public Philosopher(Long id, String name, String period, String years, String school, String quote, String color, String biography) {
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
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.initials = buildInitials(name);
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getYears() {
        return years;
    }

    public void setYears(String years) {
        this.years = years;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }
}
