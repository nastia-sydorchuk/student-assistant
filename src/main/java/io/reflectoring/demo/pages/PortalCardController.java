package io.reflectoring.demo.pages;


import io.reflectoring.demo.TranslationService;
import io.reflectoring.demo.dto.PortalCard;
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

    @GetMapping
    public ResponseEntity<List<PortalCard>> getAllCards(
            @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {

        List<PortalCard> cachedCards = repository.findByLanguage(language);
        if (!cachedCards.isEmpty()) {
            return ResponseEntity.ok(cachedCards);
        }

        List<PortalCard> englishCards = repository.findByLanguage("en");
        if (englishCards.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<PortalCard> translatedCards = translationService.translatePortalCards(englishCards, language);

        repository.saveAll(translatedCards);

        return ResponseEntity.ok(translatedCards);
    }
}