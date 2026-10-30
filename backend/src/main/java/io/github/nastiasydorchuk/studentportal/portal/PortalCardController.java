package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
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
    public List<PortalCard> getAllCards(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return service.getAllCards(languageResolver.resolve(language));
    }
}
