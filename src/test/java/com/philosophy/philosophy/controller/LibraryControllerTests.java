package com.philosophy.philosophy.controller;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.exception.PhilosopherNotFoundException;
import com.philosophy.philosophy.model.Period;
import com.philosophy.philosophy.model.Philosopher;
import com.philosophy.philosophy.service.PhilosophyLibraryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean PhilosophyLibraryService libraryService;

    @Test
    void dashboardLoadsEachCollectionOnlyOnce() throws Exception {
        when(libraryService.listPhilosophers()).thenReturn(List.of(philosopher()));
        when(libraryService.listSchools()).thenReturn(List.of());
        when(libraryService.listPeriods()).thenReturn(List.of(period()));
        mvc.perform(get("/"))
                .andExpect(status().isOk()).andExpect(view().name("index"))
                .andExpect(model().attribute("philosopherCount", 1));
        verify(libraryService).listPhilosophers();
        verify(libraryService).listSchools();
        verify(libraryService).listPeriods();
        verifyNoMoreInteractions(libraryService);
    }

    @Test
    void listOnlyLoadsPhilosophers() throws Exception {
        when(libraryService.listPhilosophers()).thenReturn(List.of(philosopher()));
        mvc.perform(get("/filosofos"))
                .andExpect(status().isOk()).andExpect(view().name("filosofos"))
                .andExpect(model().attribute("philosopherCount", 1));
        verify(libraryService).listPhilosophers();
        verifyNoMoreInteractions(libraryService);
    }

    @Test
    void formUsesPeriodsFromDatabase() throws Exception {
        when(libraryService.listPeriods()).thenReturn(List.of(period()));
        mvc.perform(get("/filosofos/novo"))
                .andExpect(status().isOk()).andExpect(view().name("cadastro"))
                .andExpect(content().string(containsString("Período Medieval")));
    }

    @Test
    void validFormNormalizesInputAndIgnoresClientId() throws Exception {
        when(libraryService.addPhilosopher(any())).thenReturn(philosopher());
        mvc.perform(post("/filosofos")
                        .param("id", "999").param("name", "  Hannah Arendt  ")
                        .param("period", "Contemporânea").param("school", "Filosofia política")
                        .param("years", "1906 - 1975").param("quote", "Pensar.")
                        .param("color", "blue").param("biography", "Resumo"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/filosofos"))
                .andExpect(flash().attributeExists("message"));
        verify(libraryService).addPhilosopher(new PhilosopherForm("Hannah Arendt", "Contemporânea",
                "Filosofia política", "1906 - 1975", "Pensar.", "blue", "Resumo"));
    }

    @Test
    void invalidFormIsRenderedWithErrorsWithoutSaving() throws Exception {
        when(libraryService.listPeriods()).thenReturn(List.of(period()));
        mvc.perform(post("/filosofos").param("name", "   ").param("school", "x".repeat(151))
                        .param("color", "invalid"))
                .andExpect(status().isBadRequest()).andExpect(view().name("cadastro"))
                .andExpect(model().attributeHasFieldErrors("philosopher", "name", "period", "school", "quote", "color"))
                .andExpect(content().string(containsString("Informe o nome.")));
        verify(libraryService, never()).addPhilosopher(any());
    }

    @Test
    void detailsAndMissingPhilosopherHaveCorrectStatus() throws Exception {
        when(libraryService.findPhilosopher(1L)).thenReturn(philosopher());
        when(libraryService.findPhilosopher(999L)).thenThrow(new PhilosopherNotFoundException(999L));
        mvc.perform(get("/filosofos/1")).andExpect(status().isOk()).andExpect(view().name("detalhes"));
        mvc.perform(get("/filosofos/999")).andExpect(status().isNotFound());
    }

    @Test
    void invalidIdentifiersAreBadRequests() throws Exception {
        mvc.perform(get("/filosofos/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/filosofos/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/filosofos/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(libraryService);
    }

    @Test
    void catalogRoutesRenderAndUnsupportedMethodsAreRejected() throws Exception {
        when(libraryService.listSchools()).thenReturn(List.of());
        when(libraryService.listPeriods()).thenReturn(List.of(period()));
        mvc.perform(get("/escolas")).andExpect(status().isOk()).andExpect(view().name("escolas"));
        mvc.perform(get("/periodos")).andExpect(status().isOk()).andExpect(view().name("periodos"));
        mvc.perform(delete("/filosofos/1")).andExpect(status().isMethodNotAllowed());
    }

    private static Period period() {
        return new Period("01", "Período Medieval", "Descrição", "476 - 1453", "terracotta");
    }

    private static Philosopher philosopher() {
        return new Philosopher(1L, "Hannah Arendt", "Contemporânea", "1906 - 1975",
                "Filosofia política", "Pensar.", "blue", "Resumo");
    }
}
