package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.portal.api.PortalCardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal-cards")
public class PortalCardController {

    private final PortalCardService service;
    private final LanguageResolver languageResolver;

    public PortalCardController(PortalCardService service, LanguageResolver languageResolver) {
        this.service = service;
        this.languageResolver = languageResolver;
    }

    @GetMapping
    public ResponseEntity<List<PortalCardResponse>> getAllCards(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getAllCards(languageResolver.resolve(language)).stream()
                .map(PortalCardResponse::from)
                .toList());
    }
}
