package io.github.nastiasydorchuk.studentportal.translation;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TranslationServiceTest {

    private Translate translate;
    private TranslationService service;

    @BeforeEach
    void setUp() {
        translate = mock(Translate.class);
        // Fake translation: "Hello" -> "es:Hello", so translated and untouched fields are easy to tell apart
        when(translate.translate(anyString(), any(), any())).thenAnswer(invocation -> {
            Translation translation = mock(Translation.class);
            when(translation.getTranslatedText()).thenReturn("es:" + invocation.getArgument(0));
            return translation;
        });
        service = new TranslationService(translate);
    }

    @Test
    void translatesFromEnglishToTheTargetLanguage() {
        service.translateInductionEvents(new InductionEvents("events_en", "en",
                List.of(new InductionEvent("Title", null, null, null, null)), null), "es", "hash");

        verify(translate).translate(eq("Title"),
                eq(Translate.TranslateOption.sourceLanguage("en")),
                eq(Translate.TranslateOption.targetLanguage("es")));
    }

    @Test
    void translateCategoryTranslatesTextAndKeepsLinksAndIcons() {
        ResourceCategory english = new ResourceCategory();
        english.setId("academic_en");
        english.setCategoryId("academic");
        english.setLanguage("en");
        english.setCategoryTitle("Academic");
        english.setDescription("Study help");
        english.setSourceHash(null);
        english.setItems(List.of(new ResourceItem("Library", "book", "Borrow books", "Visit",
                "https://lib.example.com", "New", "badge-new", "Main campus", "lib@example.com")));

        ResourceCategory result = service.translateCategory(english, "es");

        assertThat(result.getId()).isEqualTo("academic_es");
        assertThat(result.getCategoryId()).isEqualTo("academic");
        assertThat(result.getLanguage()).isEqualTo("es");
        assertThat(result.getCategoryTitle()).isEqualTo("es:Academic");
        assertThat(result.getDescription()).isEqualTo("es:Study help");
        assertThat(result.getSourceHash()).isNull();
        assertThat(result.getItems()).containsExactly(new ResourceItem("es:Library", "book", "es:Borrow books",
                "es:Visit", "https://lib.example.com", "es:New", "badge-new", "Main campus", "lib@example.com"));
    }

    @Test
    void translateCategoryLeavesMissingItemsMissing() {
        ResourceCategory english = new ResourceCategory();
        english.setCategoryId("academic");

        assertThat(service.translateCategory(english, "es").getItems()).isNull();
    }

    @Test
    void nullAndEmptyTextIsNotSentToTheApi() {
        ResourceCategory english = new ResourceCategory();
        english.setCategoryId("academic");
        english.setCategoryTitle("");
        english.setDescription(null);

        ResourceCategory result = service.translateCategory(english, "es");

        assertThat(result.getCategoryTitle()).isEmpty();
        assertThat(result.getDescription()).isNull();
        verifyNoInteractions(translate);
    }

    @Test
    void translatePortalCardsKeepsRoutesAndIcons() {
        List<PortalCard> english = List.of(
                new PortalCard("academic_en", "Academic", "/academic", "school", "Study help", "academic", "en", null));

        List<PortalCard> result = service.translatePortalCards(english, "es", "hash");

        assertThat(result).containsExactly(
                new PortalCard("academic_es", "es:Academic", "/academic", "school", "es:Study help", "academic", "es", "hash"));
    }

    @Test
    void translatePortalCardsHandlesNull() {
        assertThat(service.translatePortalCards(null, "es", "hash")).isEmpty();
    }

    @Test
    void translateStepsKeepsOrderAndUrls() {
        OnboardingSteps english = new OnboardingSteps("steps_en", "en",
                List.of(new Step(2, "Enrol", "Enrol online", "Start", "https://enrol.example.com")), null);

        OnboardingSteps result = service.translateSteps(english, "es", "hash");

        assertThat(result).isEqualTo(new OnboardingSteps("steps_es", "es",
                List.of(new Step(2, "es:Enrol", "es:Enrol online", "es:Start", "https://enrol.example.com")), "hash"));
    }

    @Test
    void translateStepsHandlesNullSteps() {
        OnboardingSteps result = service.translateSteps(new OnboardingSteps("steps_en", "en", null, null), "es", "hash");

        assertThat(result.steps()).isNull();
        assertThat(result.id()).isEqualTo("steps_es");
    }

    @Test
    void translateCohortTranslatesEverythingButLinksAndTypes() {
        CohortDocument english = new CohortDocument("undergraduate_en", CohortLevel.undergraduate, "en", "Welcome",
                List.of(new ImportantDate("Enrol", "1 Sept", DateType.deadline)),
                new EventDetails("Induction", "Meet us", List.of("Talks"), List.of("New students"), "Ask us",
                        new RegistrationLink("Register", "https://reg.example.com")),
                null);

        CohortDocument result = service.translateCohort(english, "es", "hash");

        assertThat(result).isEqualTo(new CohortDocument("undergraduate_es", CohortLevel.undergraduate, "es", "es:Welcome",
                List.of(new ImportantDate("es:Enrol", "es:1 Sept", DateType.deadline)),
                new EventDetails("es:Induction", "es:Meet us", List.of("es:Talks"), List.of("es:New students"), "es:Ask us",
                        new RegistrationLink("es:Register", "https://reg.example.com")),
                "hash"));
    }

    @Test
    void translateCohortHandlesMissingEventParts() {
        CohortDocument noEvent = new CohortDocument("postgraduate_en", CohortLevel.postgraduate, "en", "Welcome",
                null, null, null);
        CohortDocument partialEvent = new CohortDocument("postgraduate_en", CohortLevel.postgraduate, "en", "Welcome",
                List.of(), new EventDetails("Induction", null, null, null, null, null), null);

        assertThat(service.translateCohort(noEvent, "fr", "hash").event()).isNull();
        assertThat(service.translateCohort(noEvent, "fr", "hash").importantDates()).isNull();
        assertThat(service.translateCohort(partialEvent, "fr", "hash").event())
                .isEqualTo(new EventDetails("es:Induction", null, null, null, null, null));
    }

    @Test
    void translateInductionEventsKeepsIconsAndUrls() {
        InductionEvents english = new InductionEvents("events_en", "en",
                List.of(new InductionEvent("Fair", "star", "Clubs", "See more", "https://fair.example.com")), null);

        InductionEvents result = service.translateInductionEvents(english, "es", "hash");

        assertThat(result).isEqualTo(new InductionEvents("events_es", "es",
                List.of(new InductionEvent("es:Fair", "star", "es:Clubs", "es:See more", "https://fair.example.com")), "hash"));
    }

    @Test
    void translateInductionEventsHandlesNullEvents() {
        assertThat(service.translateInductionEvents(new InductionEvents("events_en", "en", null, null), "es", "hash").events())
                .isNull();
    }
}
