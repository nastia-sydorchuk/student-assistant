package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PortalCardController.class, properties = "app.supported-languages=en,es,fr,de")
@Import(LanguageResolver.class)
class PortalCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortalCardService service;

    @Test
    void returnsCardsInTheResolvedLanguage() throws Exception {
        when(service.getAllCards("es")).thenReturn(List.of(
                new PortalCard("academic", "Académico", "/academic", "school", "Ayuda")));

        mockMvc.perform(get("/api/portal-cards").header("Accept-Language", "es"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardId").value("academic"))
                .andExpect(jsonPath("$[0].title").value("Académico"))
                .andExpect(jsonPath("$[0].route").value("/academic"))
                .andExpect(jsonPath("$[0].icon").value("school"))
                .andExpect(jsonPath("$[0].description").value("Ayuda"));
    }

    @Test
    void missingCardsAreNotFound() throws Exception {
        when(service.getAllCards("en")).thenThrow(new RecordNotFoundException("Portal cards not found"));

        mockMvc.perform(get("/api/portal-cards"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Portal cards not found"));
    }
}
