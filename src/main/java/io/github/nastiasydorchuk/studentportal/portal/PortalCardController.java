package io.github.nastiasydorchuk.studentportal.portal;


import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal-cards")
public class PortalCardController {

    @Autowired
    private PortalCardRepository repository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private LanguageResolver languageResolver;

    @GetMapping
    public ResponseEntity<List<PortalCard>> getAllCards(
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {

        String resolvedLanguage = languageResolver.resolve(language);

        List<PortalCard> cachedCards = repository.findByLanguage(resolvedLanguage);
        if (!cachedCards.isEmpty()) {
            return ResponseEntity.ok(cachedCards);
        }

        List<PortalCard> englishCards = repository.findByLanguage("en");
        if (englishCards.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<PortalCard> translatedCards = translationService.translatePortalCards(englishCards, resolvedLanguage);

        repository.saveAll(translatedCards);

        return ResponseEntity.ok(translatedCards);
    }
}