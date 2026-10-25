package io.github.nastiasydorchuk.studentportal.config;

import io.github.nastiasydorchuk.studentportal.common.language.LanguageController;
import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LanguageController.class, properties = {
        "app.supported-languages=en",
        "app.cors.allowed-origins=http://localhost:4200,https://portal.example.com"
})
@Import({LanguageResolver.class, CorsConfig.class})
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsConfiguredOrigins() throws Exception {
        mockMvc.perform(get("/api/languages").header("Origin", "https://portal.example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://portal.example.com"));
    }

    @Test
    void answersPreflightForAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/languages")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void rejectsUnknownOrigins() throws Exception {
        mockMvc.perform(get("/api/languages").header("Origin", "https://evil.example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsDisallowedMethodsInPreflight() throws Exception {
        mockMvc.perform(options("/api/languages")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isForbidden());
    }
}
