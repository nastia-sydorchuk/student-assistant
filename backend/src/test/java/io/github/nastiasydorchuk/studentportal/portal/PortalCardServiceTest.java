package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCards;
import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationCache;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// The caching rule itself is covered by TranslationCacheTest; these check the portal cards are wired into it
class PortalCardServiceTest {

    private final List<PortalCard> englishCards = List.of(
            new PortalCard("academic", "Academic", "/academic", "school", "Study"),
            new PortalCard("wellbeing", "Wellbeing", "/wellbeing", "heart", "Support"));
    private final PortalCards english = new PortalCards("cards_en", "en", englishCards, null);
    private final List<PortalCard> spanishCards = List.of(
            new PortalCard("academic", "Académico", "/academic", "school", "Estudio"),
            new PortalCard("wellbeing", "Bienestar", "/wellbeing", "heart", "Apoyo"));

    private PortalCardsRepository repository;
    private TranslationService translationService;
    private PortalCardService service;
    private String hash;

    @BeforeEach
    void setUp() {
        repository = mock(PortalCardsRepository.class);
        translationService = mock(TranslationService.class);
        when(translationService.isEnabled()).thenReturn(true);
        SourceHasher hasher = new SourceHasher(JsonMapper.builder().build());
        service = new PortalCardService(repository, translationService, new TranslationCache(translationService, hasher));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        hash = hasher.hash(englishCards);
    }

    @Test
    void returnsEnglishWithoutTranslating() {
        when(repository.findByLanguage("en")).thenReturn(Optional.of(english));

        assertThat(service.getAllCards("en")).isEqualTo(englishCards);
        verify(translationService, never()).translatePortalCards(any(), any(), any());
    }

    @Test
    void throwsWhenEnglishIsMissing() {
        when(repository.findByLanguage("en")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAllCards("es"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Portal cards not found");
    }

    @Test
    void returnsCachedTranslationWhenUpToDate() {
        when(repository.findByLanguage("en")).thenReturn(Optional.of(english));
        when(repository.findByLanguage("es")).thenReturn(Optional.of(new PortalCards("cards_es", "es", spanishCards, hash)));

        assertThat(service.getAllCards("es")).isEqualTo(spanishCards);
        verify(translationService, never()).translatePortalCards(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void translatesAndSavesWhenOutdated() {
        PortalCards fresh = new PortalCards("cards_es", "es", spanishCards, hash);
        when(repository.findByLanguage("en")).thenReturn(Optional.of(english));
        when(repository.findByLanguage("es")).thenReturn(Optional.of(new PortalCards("cards_es", "es", List.of(), "old-hash")));
        when(translationService.translatePortalCards(english, "es", hash)).thenReturn(fresh);

        assertThat(service.getAllCards("es")).isEqualTo(spanishCards);
        verify(repository).save(fresh);
    }

    @Test
    void returnsEmptyListForNullCards() {
        when(repository.findByLanguage("en")).thenReturn(Optional.of(new PortalCards("cards_en", "en", null, null)));

        assertThat(service.getAllCards("en")).isEmpty();
    }
}
