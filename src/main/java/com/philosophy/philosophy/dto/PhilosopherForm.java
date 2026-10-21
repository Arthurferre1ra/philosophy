package com.philosophy.philosophy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Apenas os campos editáveis do cadastro; identificadores são gerados pelo banco. */
public record PhilosopherForm(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.") String name,
        @NotBlank(message = "Informe o período.")
        @Size(max = 100, message = "O período deve ter no máximo 100 caracteres.") String period,
        @NotBlank(message = "Informe a escola.")
        @Size(max = 150, message = "A escola deve ter no máximo 150 caracteres.") String school,
        @Size(max = 100, message = "Os anos de vida devem ter no máximo 100 caracteres.") String years,
        @NotBlank(message = "Informe uma frase ou ideia principal.")
        @Size(max = 5000, message = "A ideia principal deve ter no máximo 5000 caracteres.") String quote,
        @Pattern(regexp = "|blue|green|rose|lilac|terracotta", message = "Selecione uma cor válida.") String color,
        @Size(max = 20000, message = "O resumo deve ter no máximo 20000 caracteres.") String biography) {

    public PhilosopherForm {
        name = normalize(name);
        period = normalize(period);
        school = normalize(school);
        years = normalize(years);
        quote = normalize(quote);
        color = normalize(color);
        biography = normalize(biography);
    }

    public static PhilosopherForm empty() {
        return new PhilosopherForm(null, null, null, null, null, null, null);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
