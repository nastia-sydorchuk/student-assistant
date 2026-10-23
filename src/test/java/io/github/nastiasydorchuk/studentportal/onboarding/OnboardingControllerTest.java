package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OnboardingController.class, properties = "app.supported-languages=en,es,fr,de")
@Import(LanguageResolver.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnboardingService service;

    @Test
    void getStepsDefaultsToEnglish() throws Exception {
        when(service.getStepsBySource("en"))
                .thenReturn(List.of(new Step(1, "Enrol", "Enrol online", "Start", "https://enrol.example.com")));

        mockMvc.perform(get("/api/onboarding/steps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].order").value(1))
                .andExpect(jsonPath("$[0].summary").value("Enrol"))
                .andExpect(jsonPath("$[0].url").value("https://enrol.example.com"));
    }

    @Test
    void getStepsResolvesTheAcceptLanguageHeader() throws Exception {
        when(service.getStepsBySource("es")).thenReturn(List.of());

        mockMvc.perform(get("/api/onboarding/steps").header("Accept-Language", "es-ES,es;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(service).getStepsBySource("es");
    }

    @Test
    void unsupportedLanguageIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/onboarding/steps").header("Accept-Language", "uk"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Unsupported language: uk"))
                .andExpect(jsonPath("$.path").value("/api/onboarding/steps"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(service);
    }

    @Test
    void getCohortForLevel() throws Exception {
        when(service.getCohortByLevel(CohortLevel.postgraduate, "fr")).thenReturn(new Cohort(CohortLevel.postgraduate, "Bienvenue",
                List.of(new ImportantDate("Inscription", "1 sept", DateType.deadline)), null));

        mockMvc.perform(get("/api/onboarding/levels/postgraduate").header("Accept-Language", "fr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("postgraduate"))
                .andExpect(jsonPath("$.title").value("Bienvenue"))
                .andExpect(jsonPath("$.importantDates[0].type").value("deadline"))
                // spring.jackson.default-property-inclusion=non_null
                .andExpect(jsonPath("$.event").doesNotExist());
    }

    @Test
    void unknownLevelIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/onboarding/levels/foo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'foo' for parameter 'level'"));

        verify(service, never()).getCohortByLevel(any(), anyString());
    }

    @Test
    void missingCohortIsNotFound() throws Exception {
        when(service.getCohortByLevel(CohortLevel.undergraduate, "en"))
                .thenThrow(new RecordNotFoundException("Cohort not found for level: undergraduate"));

        mockMvc.perform(get("/api/onboarding/levels/undergraduate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cohort not found for level: undergraduate"));
    }

    @Test
    void getInductionEvents() throws Exception {
        when(service.getInductionEvents("de"))
                .thenReturn(List.of(new InductionEvent("Messe", "star", "Klubs", "Los", "https://fair.example.com")));

        mockMvc.perform(get("/api/onboarding/induction-events").header("Accept-Language", "de"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Messe"))
                .andExpect(jsonPath("$[0].icon").value("star"));
    }
}
