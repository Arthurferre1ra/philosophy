package com.philosophy.philosophy.controller;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.dto.Philosopher;
import com.philosophy.philosophy.exception.DuplicatePhilosopherException;
import com.philosophy.philosophy.service.PhilosophyLibraryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/filosofos")
public class PhilosopherController {
    private final PhilosophyLibraryService libraryService;

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
        Philosopher saved;
        try {
            saved = libraryService.addPhilosopher(philosopher);
        } catch (DuplicatePhilosopherException exception) {
            bindingResult.rejectValue("name", "duplicate", exception.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return form(model);
        }
        redirectAttributes.addFlashAttribute("message", saved.getName() + " foi adicionado ao atlas.");
        return "redirect:/filosofos";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("philosopher", libraryService.findPhilosopher(id));
        return "detalhes";
    }

    private String form(Model model) {
        model.addAttribute("periods", libraryService.listPeriods());
        return "cadastro";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("philosopher", libraryService.findPhilosopherForm(id));
        model.addAttribute("editingId", id);
        return form(model);
    }

    @PostMapping("/{id}/editar")
    public String update(@PathVariable("id") UUID id,
                         @Valid @ModelAttribute("philosopher") PhilosopherForm philosopher,
                         BindingResult bindingResult, Model model, HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        libraryService.findPhilosopher(id);
        model.addAttribute("editingId", id);
        if (!bindingResult.hasErrors()) {
            try {
                var saved = libraryService.updatePhilosopher(id, philosopher);
                redirectAttributes.addFlashAttribute("message", saved.getName() + " foi atualizado.");
                return "redirect:/filosofos";
            } catch (DuplicatePhilosopherException exception) {
                bindingResult.rejectValue("name", "duplicate", exception.getMessage());
            }
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return form(model);
    }

    @GetMapping("/{id}/excluir")
    public String deleteForm(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("philosopher", libraryService.findPhilosopher(id));
        model.addAttribute("confirmingDelete", true);
        return "detalhes";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        libraryService.deletePhilosopher(id);
        redirectAttributes.addFlashAttribute("message", "Filósofo excluído do atlas.");
        return "redirect:/filosofos";
    }
}
