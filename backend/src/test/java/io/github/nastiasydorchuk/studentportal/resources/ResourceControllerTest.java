package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.resources.api.ResourceCategoryResponse;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResourceController.class, properties = "app.supported-languages=en,es,fr,de")
@Import(LanguageResolver.class)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResourceService service;

    @Test
    void listsCategoryIds() throws Exception {
        when(service.listCategoryIds()).thenReturn(List.of("academic", "wellbeing"));

        mockMvc.perform(get("/api/resource-categories"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"academic\", \"wellbeing\"]"));
    }

    @Test
    void returnsCategoryInTheResolvedLanguage() throws Exception {
        ResourceCategoryResponse category = new ResourceCategoryResponse("academic", "Académique", "Aide",
                List.of(new ResourceItem("Bibliothèque", "book", null, null, "https://lib.example.com",
                        null, null, null, null)));
        when(service.getCategory("academic", "fr")).thenReturn(category);

        mockMvc.perform(get("/api/resource-categories/academic").header("Accept-Language", "fr-FR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value("academic"))
                .andExpect(jsonPath("$.categoryTitle").value("Académique"))
                .andExpect(jsonPath("$.items[0].title").value("Bibliothèque"))
                .andExpect(jsonPath("$.items[0].url").value("https://lib.example.com"))
                .andExpect(jsonPath("$.items[0].label").doesNotExist());
    }

    @Test
    void missingCategoryIsNotFound() throws Exception {
        when(service.getCategory("nope", "en")).thenThrow(new RecordNotFoundException("Resource category not found: nope"));

        mockMvc.perform(get("/api/resource-categories/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource category not found: nope"));
    }

    @Test
    void unsupportedLanguageIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/resource-categories/academic").header("Accept-Language", "zh"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
