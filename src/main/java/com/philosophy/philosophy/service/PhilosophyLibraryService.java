package com.philosophy.philosophy.service;

import com.philosophy.philosophy.model.Period;
import com.philosophy.philosophy.model.Philosopher;
import com.philosophy.philosophy.model.School;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PhilosophyLibraryService {

    private final List<Philosopher> philosophers = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public PhilosophyLibraryService() {
        addSeed("Sócrates", "Antiguidade", "470 - 399 a.C.", "Filosofia clássica", "Só sei que nada sei.", "", "Filósofo ateniense que colocou o diálogo e a pergunta no centro da investigação ética.");
        addSeed("Platão", "Antiguidade", "428 - 348 a.C.", "Idealismo", "A direção em que a educação inicia uma pessoa determinará o seu futuro.", "blue", "Fundador da Academia, pensou justiça, conhecimento e política a partir da teoria das ideias.");
        addSeed("Aristóteles", "Antiguidade", "384 - 322 a.C.", "Aristotelismo", "A excelência não é um ato, mas um hábito.", "green", "Investigou lógica, ética, natureza e política, defendendo a busca pela virtude como prática.");
        addSeed("René Descartes", "Modernidade", "1596 - 1650", "Racionalismo", "Penso, logo existo.", "rose", "Marco da filosofia moderna, propôs a dúvida metódica e buscou fundamentos seguros para o conhecimento.");
        addSeed("Immanuel Kant", "Modernidade", "1724 - 1804", "Iluminismo", "Ouse saber! Tenha coragem de usar seu próprio entendimento.", "lilac", "Analisou os limites da razão e formulou uma ética baseada na autonomia e no dever.");
        addSeed("Simone de Beauvoir", "Contemporânea", "1908 - 1986", "Existencialismo", "Não se nasce mulher: torna-se mulher.", "terracotta", "Pensadora existencialista que relacionou liberdade, opressão e construção histórica da identidade.");
    }

    public List<Philosopher> listPhilosophers() {
        return philosophers;
    }

    public Optional<Philosopher> findPhilosopher(Long id) {
        return philosophers.stream()
                .filter(philosopher -> philosopher.getId().equals(id))
                .findFirst();
    }

    public Philosopher addPhilosopher(Philosopher philosopher) {
        philosopher.setId(nextId.getAndIncrement());
        philosopher.setInitials(Philosopher.buildInitials(philosopher.getName()));
        if (philosopher.getYears() == null || philosopher.getYears().isBlank()) {
            philosopher.setYears("Dados a completar");
        }
        if (philosopher.getColor() == null || philosopher.getColor().isBlank()) {
            philosopher.setColor("blue");
        }
        if (philosopher.getBiography() == null || philosopher.getBiography().isBlank()) {
            philosopher.setBiography("Registro criado pelo formulário da aplicação. Complete este perfil com mais contexto histórico.");
        }
        philosophers.add(0, philosopher);
        return philosopher;
    }

    public List<School> listSchools() {
        return List.of(
                new School("S", "Estoicismo", "Antiguidade", "Uma filosofia prática sobre serenidade, virtude e a distinção entre aquilo que podemos ou não controlar.", "stoic"),
                new School("R", "Racionalismo", "Modernidade", "Defende a razão como fonte central do conhecimento e investiga os limites da dúvida e da certeza.", "rational"),
                new School("E", "Existencialismo", "Século XX", "Coloca liberdade, responsabilidade e sentido no centro da experiência humana concreta.", "existential"),
                new School("I", "Idealismo", "Antiguidade", "Explora a relação entre realidade, conhecimento e as formas que estruturam nossa compreensão do mundo.", "green")
        );
    }

    public List<Period> listPeriods() {
        return List.of(
                new Period("01", "Antiguidade", "Do nascimento da filosofia grega à queda do Império Romano. A investigação passa pela natureza, pela cidade e pela vida virtuosa.", "600 a.C. - 476 d.C.", "gold"),
                new Period("02", "Período Medieval", "Fé, razão e metafísica se encontram em debates sobre existência, ética e a relação entre o ser humano e o divino.", "476 - 1453", "terracotta"),
                new Period("03", "Modernidade", "A razão, a ciência e a autonomia individual ganham protagonismo na formação do pensamento moderno.", "1453 - 1789", "blue"),
                new Period("04", "Contemporaneidade", "Novas perspectivas questionam linguagem, sociedade, existência e estruturas que organizam a vida comum.", "1789 - hoje", "green")
        );
    }

    private void addSeed(String name, String period, String years, String school, String quote, String color, String biography) {
        philosophers.add(new Philosopher(nextId.getAndIncrement(), name, period, years, school, quote, color, biography));
    }
}
