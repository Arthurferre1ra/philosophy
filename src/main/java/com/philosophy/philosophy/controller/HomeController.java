package com.philosophy.philosophy.controller;

import com.philosophy.philosophy.model.Philosopher;
import com.philosophy.philosophy.service.PhilosophyLibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final PhilosophyLibraryService libraryService;

    public HomeController(PhilosophyLibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        addSharedData(model);
        model.addAttribute("activePage", "home");
        return "index";
    }

    @GetMapping("/filosofos")
    public String filosofos(Model model) {
        addSharedData(model);
        model.addAttribute("activePage", "filosofos");
        return "filosofos";
    }

    @GetMapping("/filosofos/novo")
    public String novoFilosofo(Model model) {
        model.addAttribute("philosopher", new Philosopher());
        model.addAttribute("activePage", "filosofos");
        return "cadastro";
    }

    @PostMapping("/filosofos")
    public String cadastrarFilosofo(@ModelAttribute("philosopher") Philosopher philosopher, RedirectAttributes redirectAttributes) {
        Philosopher savedPhilosopher = libraryService.addPhilosopher(philosopher);
        redirectAttributes.addFlashAttribute("message", savedPhilosopher.getName() + " foi adicionado ao atlas.");
        return "redirect:/filosofos";
    }

    @GetMapping("/filosofos/{id}")
    public String detalhesFilosofo(@PathVariable Long id, Model model) {
        Philosopher philosopher = libraryService.findPhilosopher(id).orElseThrow(() -> new IllegalArgumentException("Filósofo não encontrado"));
        model.addAttribute("philosopher", philosopher);
        model.addAttribute("activePage", "filosofos");
        return "detalhes";
    }

    @GetMapping("/escolas")
    public String escolas(Model model) {
        model.addAttribute("schools", libraryService.listSchools());
        model.addAttribute("activePage", "escolas");
        return "escolas";
    }

    @GetMapping("/periodos")
    public String periodos(Model model) {
        model.addAttribute("periods", libraryService.listPeriods());
        model.addAttribute("activePage", "periodos");
        return "periodos";
    }

    private void addSharedData(Model model) {
        model.addAttribute("philosophers", libraryService.listPhilosophers());
        model.addAttribute("schools", libraryService.listSchools());
        model.addAttribute("periods", libraryService.listPeriods());
        model.addAttribute("philosopherCount", libraryService.listPhilosophers().size());
        model.addAttribute("schoolCount", libraryService.listSchools().size());
        model.addAttribute("periodCount", libraryService.listPeriods().size());
    }
}
