package com.philosophy.philosophy.controller;

import com.philosophy.philosophy.service.PhilosophyLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final PhilosophyLibraryService libraryService;

    @GetMapping("/")
    public String index(Model model) {
        var philosophers = libraryService.listPhilosophers();
        var schools = libraryService.listSchools();
        var periods = libraryService.listPeriods();
        model.addAttribute("philosophers", philosophers);
        model.addAttribute("schools", schools);
        model.addAttribute("periods", periods);
        model.addAttribute("philosopherCount", philosophers.size());
        model.addAttribute("schoolCount", schools.size());
        model.addAttribute("periodCount", periods.size());
        model.addAttribute("activePage", "home");
        return "index";
    }
}
