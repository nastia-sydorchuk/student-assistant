package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.portal.api.PortalCardResponse;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalCardService {
    private final PortalCardRepository repository;
    private final TranslationService translationService;
    private final SourceHasher hasher;

    public PortalCardService(PortalCardRepository repository, TranslationService translationService, SourceHasher hasher) {
        this.repository = repository;
        this.translationService = translationService;
        this.hasher = hasher;
    }

    // Translates when the translations are missing or were made from an older version of the English cards.
    // Returns the language to serve: English if a translation is needed but translation is disabled.
    private String languageToServe(String language) {
        if ("en".equals(language)) return language;

        List<PortalCard> english = repository.findByLanguageOrderByCardIdAsc("en");
        if (english.isEmpty()) {
            throw new RecordNotFoundException("English portal cards not found");
        }
        // Hash the client-visible content only, so storage fields never invalidate translations
        String hash = hasher.hash(english.stream().map(PortalCardResponse::from).toList());

        List<PortalCard> existing = repository.findByLanguage(language);
        boolean upToDate = !existing.isEmpty()
                && existing.stream().allMatch(card -> hash.equals(card.sourceHash()));

        if (!upToDate) {
            if (!translationService.isEnabled()) return "en";
            // Delete first so cards removed from English don't linger in the translation
            repository.deleteAll(existing);
            repository.saveAll(translationService.translatePortalCards(english, language, hash));
        }
        return language;
    }

    public List<PortalCard> getAllCards(String language) {
        List<PortalCard> cards = repository.findByLanguage(languageToServe(language));
        if (cards.isEmpty()) {
            throw new RecordNotFoundException("Portal cards not found");
        }
        return cards;
    }
}
