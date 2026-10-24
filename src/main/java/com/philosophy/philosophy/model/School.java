package com.philosophy.philosophy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "escola_pensamento")
@Getter
@Setter
@NoArgsConstructor
public class School extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(name = "nome", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "descricao", columnDefinition = "text")
    private String description;

    @Column(name = "origem", length = 150)
    private String origin;

    @Column(name = "ano_inicio")
    private Integer startYear;

    @Column(name = "ano_fim")
    private Integer endYear;
}
