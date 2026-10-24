package com.philosophy.philosophy.controller;

import com.philosophy.philosophy.dto.PhilosopherForm;
import com.philosophy.philosophy.exception.PhilosopherNotFoundException;
import com.philosophy.philosophy.exception.DuplicatePhilosopherException;
import com.philosophy.philosophy.dto.Period;
import com.philosophy.philosophy.dto.Philosopher;
import com.philosophy.philosophy.service.PhilosophyLibraryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryControllerTests {
    private static final UUID PHILOSOPHER_ID = UUID.fromString("23534c10-a77a-4e76-9900-29746ed0639d");
    private static final UUID MISSING_ID = UUID.fromString("3cc89c97-8c95-4866-995d-60068b4c7acb");
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
    void emptyDatabaseStillAllowsEnteringTheFirstPeriod() throws Exception {
        when(libraryService.listPeriods()).thenReturn(List.of());
        mvc.perform(get("/filosofos/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<input(?=[^>]*name=\"period\")(?=[^>]*list=\"period-options\")(?=[^>]*required)[^>]*>.*")))
                .andExpect(content().string(containsString("<datalist id=\"period-options\"></datalist>")));
    }

    @Test
    void validFormNormalizesInputAndIgnoresClientId() throws Exception {
        when(libraryService.addPhilosopher(any())).thenReturn(philosopher());
        mvc.perform(post("/filosofos")
                        .param("id", MISSING_ID.toString()).param("name", "  Hannah Arendt  ")
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
        when(libraryService.findPhilosopher(PHILOSOPHER_ID)).thenReturn(philosopher());
        when(libraryService.findPhilosopher(MISSING_ID)).thenThrow(new PhilosopherNotFoundException(MISSING_ID));
        mvc.perform(get("/filosofos/{id}", PHILOSOPHER_ID)).andExpect(status().isOk()).andExpect(view().name("detalhes"));
        mvc.perform(get("/filosofos/{id}", MISSING_ID)).andExpect(status().isNotFound());
    }

    @Test
    void invalidIdentifiersAreBadRequests() throws Exception {
        mvc.perform(get("/filosofos/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/filosofos/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/filosofos/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/filosofos/23534c10-a77a-4e76-9900-29746ed0639z")).andExpect(status().isBadRequest());
        verifyNoInteractions(libraryService);
    }

    @Test
    void catalogRoutesRenderAndUnsupportedMethodsAreRejected() throws Exception {
        when(libraryService.listSchools()).thenReturn(List.of());
        when(libraryService.listPeriods()).thenReturn(List.of(period()));
        mvc.perform(get("/escolas")).andExpect(status().isOk()).andExpect(view().name("escolas"));
        mvc.perform(get("/periodos")).andExpect(status().isOk()).andExpect(view().name("periodos"));
        mvc.perform(delete("/filosofos/{id}", PHILOSOPHER_ID)).andExpect(status().isMethodNotAllowed());
    }

    private static Period period() {
        return new Period(UUID.fromString("5a1a4293-3934-443e-a9e3-76e74bf37d31"),
                "01", "Período Medieval", "Descrição", "476 - 1453", "terracotta");
    }

    @Test
    void editFormContainsRawValuesAndCorrectPostAction() throws Exception {
        when(libraryService.findPhilosopherForm(PHILOSOPHER_ID)).thenReturn(new PhilosopherForm(
                "Hannah Arendt", "Contemporânea", "Filosofia política", null, "Pensar.", "blue", null));
        mvc.perform(get("/filosofos/{id}/editar", PHILOSOPHER_ID))
                .andExpect(status().isOk()).andExpect(view().name("cadastro"))
                .andExpect(content().string(containsString("value=\"Hannah Arendt\"")))
                .andExpect(content().string(containsString("action=\"/filosofos/" + PHILOSOPHER_ID + "/editar\"")))
                .andExpect(content().string(containsString("Salvar alterações")));
    }

    @Test
    void validEditUsesPathIdAndRedirectsWithMessage() throws Exception {
        when(libraryService.updatePhilosopher(any(), any())).thenReturn(philosopher());
        mvc.perform(post("/filosofos/{id}/editar", PHILOSOPHER_ID)
                        .param("id", MISSING_ID.toString()).param("name", "Hannah Arendt")
                        .param("period", "Contemporânea").param("school", "Filosofia política")
                        .param("quote", "Pensar."))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/filosofos"))
                .andExpect(flash().attributeExists("message"));
        verify(libraryService).updatePhilosopher(eq(PHILOSOPHER_ID), any());
    }

    @Test
    void invalidEditKeepsEditingActionAndDoesNotSave() throws Exception {
        mvc.perform(post("/filosofos/{id}/editar", PHILOSOPHER_ID).param("name", ""))
                .andExpect(status().isBadRequest()).andExpect(view().name("cadastro"))
                .andExpect(model().attributeHasFieldErrors("philosopher", "name", "period", "school", "quote"))
                .andExpect(content().string(containsString("action=\"/filosofos/" + PHILOSOPHER_ID + "/editar\"")));
        verify(libraryService, never()).updatePhilosopher(any(), any());
    }

    @Test
    void duplicateBusinessRuleIsShownOnCreateAndEditForms() throws Exception {
        when(libraryService.addPhilosopher(any())).thenThrow(new DuplicatePhilosopherException());
        when(libraryService.updatePhilosopher(any(), any())).thenThrow(new DuplicatePhilosopherException());
        for (String path : new String[]{"/filosofos", "/filosofos/" + PHILOSOPHER_ID + "/editar"}) {
            mvc.perform(post(path).param("name", "Hannah Arendt").param("period", "Contemporânea")
                            .param("school", "Filosofia política").param("quote", "Pensar."))
                    .andExpect(status().isBadRequest())
                    .andExpect(model().attributeHasFieldErrors("philosopher", "name"))
                    .andExpect(content().string(containsString("Já existe um filósofo")));
        }
    }

    @Test
    void deleteRequiresPostAndConfirmationGetOnlyReads() throws Exception {
        when(libraryService.findPhilosopher(PHILOSOPHER_ID)).thenReturn(philosopher());
        mvc.perform(get("/filosofos/{id}/excluir", PHILOSOPHER_ID))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Confirmar exclusão")))
                .andExpect(content().string(containsString("method=\"post\"")));
        verify(libraryService, never()).deletePhilosopher(any());
        mvc.perform(post("/filosofos/{id}/excluir", PHILOSOPHER_ID))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/filosofos"))
                .andExpect(flash().attributeExists("message"));
        verify(libraryService).deletePhilosopher(PHILOSOPHER_ID);
    }

    @Test
    void missingRecordsAre404ForEditAndDelete() throws Exception {
        when(libraryService.findPhilosopherForm(MISSING_ID)).thenThrow(new PhilosopherNotFoundException(MISSING_ID));
        when(libraryService.findPhilosopher(MISSING_ID)).thenThrow(new PhilosopherNotFoundException(MISSING_ID));
        doThrow(new PhilosopherNotFoundException(MISSING_ID)).when(libraryService).deletePhilosopher(MISSING_ID);
        mvc.perform(get("/filosofos/{id}/editar", MISSING_ID)).andExpect(status().isNotFound());
        mvc.perform(post("/filosofos/{id}/editar", MISSING_ID)).andExpect(status().isNotFound());
        mvc.perform(get("/filosofos/{id}/excluir", MISSING_ID)).andExpect(status().isNotFound());
        mvc.perform(post("/filosofos/{id}/excluir", MISSING_ID)).andExpect(status().isNotFound());
    }

    private static Philosopher philosopher() {
        return new Philosopher(PHILOSOPHER_ID, "Hannah Arendt", "Contemporânea", "1906 - 1975",
                "Filosofia política", "Pensar.", "blue", "Resumo");
    }
}
