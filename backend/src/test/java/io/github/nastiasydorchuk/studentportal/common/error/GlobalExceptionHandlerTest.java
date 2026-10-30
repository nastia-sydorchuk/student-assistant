package io.github.nastiasydorchuk.studentportal.common.error;

import io.github.nastiasydorchuk.studentportal.chat.api.ChatRequest;
import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.portal.PortalCardController;
import io.github.nastiasydorchuk.studentportal.portal.PortalCardService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Drives the handler through a real endpoint whose service is made to throw
@WebMvcTest(controllers = PortalCardController.class, properties = "app.supported-languages=en")
@Import(LanguageResolver.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortalCardService service;

    @Test
    void upstreamFailureIsServiceUnavailable() throws Exception {
        when(service.getAllCards("en")).thenThrow(new UpstreamUnavailableException("Translate is down"));

        mockMvc.perform(get("/api/portal-cards"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Translate is down"));
    }

    @Test
    void constraintViolationsAreListedPerProperty() throws Exception {
        var violations = Validation.buildDefaultValidatorFactory().getValidator().validate(new ChatRequest(null, null, null));
        when(service.getAllCards("en")).thenThrow(new ConstraintViolationException(violations));

        mockMvc.perform(get("/api/portal-cards"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.query").exists());
    }

    @Test
    void unexpectedErrorsDoNotLeakDetails() throws Exception {
        when(service.getAllCards("en")).thenThrow(new IllegalStateException("secret connection string"));

        mockMvc.perform(get("/api/portal-cards"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.path").value("/api/portal-cards"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void unknownUrlIsNotFound() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/does-not-exist"));
    }

    @Test
    void wrongHttpMethodIsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/portal-cards"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "GET"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.path").value("/api/portal-cards"));
    }
}
