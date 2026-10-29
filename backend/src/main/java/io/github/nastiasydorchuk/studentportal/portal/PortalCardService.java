package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.translation.TranslationCache;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCards;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalCardService {
    private final PortalCardsRepository repository;
    private final TranslationService translationService;
    private final TranslationCache translations;

    public PortalCardService(PortalCardsRepository repository, TranslationService translationService, TranslationCache translations) {
        this.repository = repository;
        this.translationService = translationService;
        this.translations = translations;
    }

    public List<PortalCard> getAllCards(String language) {
        PortalCards english = repository.findByLanguage("en")
                .orElseThrow(() -> new RecordNotFoundException("Portal cards not found"));

        PortalCards result = translations.localize(language, english, english.cards(),
                repository::findByLanguage, PortalCards::sourceHash,
                (en, hash) -> repository.save(translationService.translatePortalCards(en, language, hash)));
        return result.cards() == null ? List.of() : result.cards();
    }
}
