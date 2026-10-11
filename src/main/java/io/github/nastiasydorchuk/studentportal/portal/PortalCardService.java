package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalCardService {
    private final PortalCardRepository repository;
    private final TranslationService translationService;

    public PortalCardService(PortalCardRepository repository, TranslationService translationService) {
        this.repository = repository;
        this.translationService = translationService;
    }

    private void ensureTranslationExists(String language) {
        if (repository.findByLanguage(language).isEmpty()) {
            List<PortalCard> english = repository.findByLanguage("en");
            if (english.isEmpty()) {
                throw new RecordNotFoundException("English portal cards not found");
            }

            List<PortalCard> translated = translationService.translatePortalCards(english, language);
            repository.saveAll(translated);
        }
    }

    public List<PortalCard> getAllCards(String language) {
        ensureTranslationExists(language);
        List<PortalCard> cards = repository.findByLanguage(language);
        if (cards.isEmpty()) {
            throw new RecordNotFoundException("Portal cards not found");
        }
        return cards;
    }
}
