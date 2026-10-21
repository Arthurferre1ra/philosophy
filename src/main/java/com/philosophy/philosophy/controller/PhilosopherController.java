package com.philosophy.philosophy.controller;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.service.PhilosophyLibraryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/filosofos")
public class PhilosopherController {
    private final PhilosophyLibraryService libraryService;

    public PhilosopherController(PhilosophyLibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @ModelAttribute("activePage")
    public String activePage() {
        return "filosofos";
    }

    @GetMapping
    public String list(Model model) {
        var philosophers = libraryService.listPhilosophers();
        model.addAttribute("philosophers", philosophers);
        model.addAttribute("philosopherCount", philosophers.size());
        return "filosofos";
    }

    @GetMapping("/novo")
    public String createForm(Model model) {
        model.addAttribute("philosopher", PhilosopherForm.empty());
        return form(model);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("philosopher") PhilosopherForm philosopher,
                         BindingResult bindingResult, Model model, HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return form(model);
        }
        var saved = libraryService.addPhilosopher(philosopher);
        redirectAttributes.addFlashAttribute("message", saved.getName() + " foi adicionado ao atlas.");
        return "redirect:/filosofos";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable("id") @Positive long id, Model model) {
        model.addAttribute("philosopher", libraryService.findPhilosopher(id));
        return "detalhes";
    }

    private String form(Model model) {
        model.addAttribute("periods", libraryService.listPeriods());
        return "cadastro";
    }
}
