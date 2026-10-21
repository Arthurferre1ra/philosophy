package com.philosophy.philosophy.controller;

import com.philosophy.philosophy.service.PhilosophyLibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogController {
    private final PhilosophyLibraryService libraryService;

    public CatalogController(PhilosophyLibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/escolas")
    public String schools(Model model) {
        model.addAttribute("schools", libraryService.listSchools());
        model.addAttribute("activePage", "escolas");
        return "escolas";
    }

    @GetMapping("/periodos")
    public String periods(Model model) {
        model.addAttribute("periods", libraryService.listPeriods());
        model.addAttribute("activePage", "periodos");
        return "periodos";
    }
}
