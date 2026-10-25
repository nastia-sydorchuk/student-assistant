package io.github.nastiasydorchuk.studentportal.common.language;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// "xx" has no JDK display name, so it falls back to the code itself
@WebMvcTest(controllers = LanguageController.class, properties = "app.supported-languages=en,es,fr,de,uk,xx")
@Import(LanguageResolver.class)
class LanguageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSupportedLanguagesWithCapitalizedNativeNames() throws Exception {
        mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"code": "en", "name": "English"},
                          {"code": "es", "name": "Español"},
                          {"code": "fr", "name": "Français"},
                          {"code": "de", "name": "Deutsch"},
                          {"code": "uk", "name": "Українська"},
                          {"code": "xx", "name": "xx"}
                        ]
                        """, true));
    }
}
