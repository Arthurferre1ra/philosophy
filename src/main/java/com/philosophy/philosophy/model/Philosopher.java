package com.philosophy.philosophy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "filosofo")
@Getter
@Setter
@NoArgsConstructor
public class Philosopher extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150)
    private String name;

    @Column(name = "nome_completo", length = 200)
    private String fullName;

    @Column(name = "data_nascimento")
    private LocalDate birthDate;

    @Column(name = "data_falecimento")
    private LocalDate deathDate;

    @Column(name = "local_nascimento", length = 150)
    private String birthplace;

    @Column(name = "nacionalidade", length = 100)
    private String nationality;

    @Column(name = "biografia", columnDefinition = "text")
    private String biography;

    @Column(name = "principais_ideias", columnDefinition = "text")
    private String quote;

    @Column(name = "imagem_url", length = 500)
    private String imageUrl;

    @Column(name = "anos_vida", length = 100)
    private String years;

    @Column(name = "cor", length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id")
    private Period period;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "filosofo_escola",
            joinColumns = @JoinColumn(name = "filosofo_id"),
            inverseJoinColumns = @JoinColumn(name = "escola_id"))
    private Set<School> schools = new LinkedHashSet<>();
}
