package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.portal.api.PortalCardResponse;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PortalCardServiceTest {

    private final PortalCard englishAcademic = new PortalCard("academic_en", "Academic", "/academic", "school", "Study", "academic", "en", null);
    private final PortalCard englishWellbeing = new PortalCard("wellbeing_en", "Wellbeing", "/wellbeing", "heart", "Support", "wellbeing", "en", null);
    private final List<PortalCard> english = List.of(englishAcademic, englishWellbeing);

    private PortalCardRepository repository;
    private TranslationService translationService;
    private SourceHasher hasher;
    private PortalCardService service;
    private String englishHash;

    @BeforeEach
    void setUp() {
        repository = mock(PortalCardRepository.class);
        translationService = mock(TranslationService.class);
        when(translationService.isEnabled()).thenReturn(true);
        hasher = new SourceHasher(JsonMapper.builder().build());
        service = new PortalCardService(repository, translationService, hasher);
        englishHash = hasher.hash(english.stream().map(PortalCardResponse::from).toList());
    }

    @Test
    void servesEnglishWhenTranslationIsDisabled() {
        when(translationService.isEnabled()).thenReturn(false);
        when(repository.findByLanguageOrderByCardIdAsc("en")).thenReturn(english);
        when(repository.findByLanguage("es")).thenReturn(List.of());
        when(repository.findByLanguage("en")).thenReturn(english);

        assertThat(service.getAllCards("es")).isEqualTo(english);
        verify(translationService, never()).translatePortalCards(any(), any(), any());
        verify(repository, never()).saveAll(any());
    }

    private PortalCard spanish(PortalCard card, String hash) {
        return new PortalCard(card.cardId() + "_es", "es:" + card.title(), card.route(), card.icon(),
                "es:" + card.description(), card.cardId(), "es", hash);
    }

    @Test
    void returnsEnglishWithoutTranslating() {
        when(repository.findByLanguage("en")).thenReturn(english);

        assertThat(service.getAllCards("en")).isEqualTo(english);
        verifyNoInteractions(translationService);
        verify(repository, never()).findByLanguageOrderByCardIdAsc(any());
    }

    @Test
    void throwsWhenEnglishCardsAreMissingForEnglish() {
        when(repository.findByLanguage("en")).thenReturn(List.of());

        assertThatThrownBy(() -> service.getAllCards("en"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Portal cards not found");
    }

    @Test
    void throwsWhenEnglishSourceIsMissingForTranslation() {
        when(repository.findByLanguageOrderByCardIdAsc("en")).thenReturn(List.of());

        assertThatThrownBy(() -> service.getAllCards("es"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("English portal cards not found");
        verifyNoInteractions(translationService);
    }

    @Test
    void returnsCachedTranslationWhenAllCardsAreUpToDate() {
        List<PortalCard> cached = List.of(spanish(englishAcademic, englishHash), spanish(englishWellbeing, englishHash));
        when(repository.findByLanguageOrderByCardIdAsc("en")).thenReturn(english);
        when(repository.findByLanguage("es")).thenReturn(cached);

        assertThat(service.getAllCards("es")).isEqualTo(cached);
        verifyNoInteractions(translationService);
        verify(repository, never()).deleteAll(any());
        verify(repository, never()).saveAll(any());
    }

    @Test
    void replacesTranslationWhenAnyCardIsStale() {
        List<PortalCard> stale = List.of(spanish(englishAcademic, englishHash), spanish(englishWellbeing, "old-hash"));
        List<PortalCard> fresh = List.of(spanish(englishAcademic, englishHash), spanish(englishWellbeing, englishHash));
        when(repository.findByLanguageOrderByCardIdAsc("en")).thenReturn(english);
        when(repository.findByLanguage("es")).thenReturn(stale, fresh);
        when(translationService.translatePortalCards(english, "es", englishHash)).thenReturn(fresh);

        assertThat(service.getAllCards("es")).isEqualTo(fresh);
        var inOrder = inOrder(repository);
        inOrder.verify(repository).deleteAll(stale);
        inOrder.verify(repository).saveAll(fresh);
    }

    @Test
    void translatesWhenTranslationIsMissing() {
        List<PortalCard> fresh = List.of(spanish(englishAcademic, englishHash), spanish(englishWellbeing, englishHash));
        when(repository.findByLanguageOrderByCardIdAsc("en")).thenReturn(english);
        when(repository.findByLanguage("es")).thenReturn(List.of(), fresh);
        when(translationService.translatePortalCards(english, "es", englishHash)).thenReturn(fresh);

        assertThat(service.getAllCards("es")).isEqualTo(fresh);
        verify(repository).saveAll(fresh);
    }

    @Test
    void storageFieldsDoNotAffectTheHash() {
        List<PortalCard> sameContent = List.of(
                new PortalCard("other", "Academic", "/academic", "school", "Study", "academic", "xx", "h"),
                new PortalCard("other2", "Wellbeing", "/wellbeing", "heart", "Support", "wellbeing", "xx", "h"));

        assertThat(hasher.hash(sameContent.stream().map(PortalCardResponse::from).toList())).isEqualTo(englishHash);
    }
}
