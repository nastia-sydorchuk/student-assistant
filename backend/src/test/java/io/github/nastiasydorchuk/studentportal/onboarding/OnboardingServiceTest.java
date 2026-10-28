package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.onboarding.api.CohortResponse;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OnboardingServiceTest {

    private OnboardingStepsRepository stepsRepository;
    private CohortRepository cohortRepository;
    private InductionEventsRepository eventsRepository;
    private TranslationService translationService;
    private SourceHasher hasher;
    private OnboardingService service;

    @BeforeEach
    void setUp() {
        stepsRepository = mock(OnboardingStepsRepository.class);
        cohortRepository = mock(CohortRepository.class);
        eventsRepository = mock(InductionEventsRepository.class);
        translationService = mock(TranslationService.class);
        when(translationService.isEnabled()).thenReturn(true);
        hasher = new SourceHasher(JsonMapper.builder().build());
        service = new OnboardingService(stepsRepository, cohortRepository, eventsRepository, translationService, hasher);

        when(stepsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cohortRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    class Steps {
        private final List<OnboardingStep> englishSteps = List.of(new OnboardingStep(1, "Enrol", "Enrol online", "Start", "https://x"));
        private final OnboardingSteps english = new OnboardingSteps("steps_en", "en", englishSteps, null);
        private final List<OnboardingStep> spanishSteps = List.of(new OnboardingStep(1, "Matricúlate", "En línea", "Empezar", "https://x"));

        @Test
        void returnsEnglishWithoutTranslating() {
            when(stepsRepository.findByLanguage("en")).thenReturn(Optional.of(english));

            assertThat(service.getSteps("en")).isEqualTo(englishSteps);
            verifyNoInteractions(translationService);
        }

        @Test
        void throwsWhenEnglishIsMissing() {
            when(stepsRepository.findByLanguage("en")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getSteps("es"))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Steps not found.");
        }

        @Test
        void returnsCachedTranslationWhenUpToDate() {
            String hash = hasher.hash(englishSteps);
            when(stepsRepository.findByLanguage("en")).thenReturn(Optional.of(english));
            when(stepsRepository.findByLanguage("es"))
                    .thenReturn(Optional.of(new OnboardingSteps("steps_es", "es", spanishSteps, hash)));

            assertThat(service.getSteps("es")).isEqualTo(spanishSteps);
            verifyNoInteractions(translationService);
            verify(stepsRepository, never()).save(any());
        }

        @Test
        void retranslatesWhenTranslationIsStale() {
            String hash = hasher.hash(englishSteps);
            when(stepsRepository.findByLanguage("en")).thenReturn(Optional.of(english));
            when(stepsRepository.findByLanguage("es"))
                    .thenReturn(Optional.of(new OnboardingSteps("steps_es", "es", List.of(), "old-hash")));
            OnboardingSteps fresh = new OnboardingSteps("steps_es", "es", spanishSteps, hash);
            when(translationService.translateSteps(english, "es", hash)).thenReturn(fresh);

            assertThat(service.getSteps("es")).isEqualTo(spanishSteps);
            verify(stepsRepository).save(fresh);
        }

        @Test
        void translatesWhenTranslationIsMissing() {
            String hash = hasher.hash(englishSteps);
            when(stepsRepository.findByLanguage("en")).thenReturn(Optional.of(english));
            when(stepsRepository.findByLanguage("es")).thenReturn(Optional.empty());
            OnboardingSteps fresh = new OnboardingSteps("steps_es", "es", spanishSteps, hash);
            when(translationService.translateSteps(english, "es", hash)).thenReturn(fresh);

            assertThat(service.getSteps("es")).isEqualTo(spanishSteps);
            verify(stepsRepository).save(fresh);
        }

        @Test
        void servesEnglishWhenTranslationIsDisabled() {
            when(translationService.isEnabled()).thenReturn(false);
            when(stepsRepository.findByLanguage("en")).thenReturn(Optional.of(english));
            when(stepsRepository.findByLanguage("es")).thenReturn(Optional.empty());

            assertThat(service.getSteps("es")).isEqualTo(englishSteps);
            verify(translationService, never()).translateSteps(any(), any(), any());
            verify(stepsRepository, never()).save(any());
        }

        @Test
        void returnsEmptyListForNullSteps() {
            when(stepsRepository.findByLanguage("en")).thenReturn(Optional.of(new OnboardingSteps("steps_en", "en", null, null)));

            assertThat(service.getSteps("en")).isEmpty();
        }
    }

    @Nested
    class Cohorts {
        private final Cohort english = new Cohort("undergraduate_en", CohortLevel.undergraduate, "en",
                "Welcome", List.of(new ImportantDate("Enrol", "1 Sept", DateType.deadline)), null, null);

        @Test
        void returnsEnglishWithoutTranslating() {
            when(cohortRepository.findByLevelAndLanguage(CohortLevel.undergraduate, "en")).thenReturn(Optional.of(english));

            assertThat(service.getCohort(CohortLevel.undergraduate, "en")).isEqualTo(CohortResponse.from(english));
            verifyNoInteractions(translationService);
        }

        @Test
        void throwsWhenEnglishIsMissing() {
            when(cohortRepository.findByLevelAndLanguage(CohortLevel.postgraduate, "en")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getCohort(CohortLevel.postgraduate, "fr"))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Cohort not found for level: postgraduate");
        }

        @Test
        void returnsCachedTranslationWhenUpToDate() {
            String hash = hasher.hash(CohortResponse.from(english));
            Cohort spanish = new Cohort("undergraduate_es", CohortLevel.undergraduate, "es",
                    "Bienvenida", List.of(), null, hash);
            when(cohortRepository.findByLevelAndLanguage(CohortLevel.undergraduate, "en")).thenReturn(Optional.of(english));
            when(cohortRepository.findByLevelAndLanguage(CohortLevel.undergraduate, "es")).thenReturn(Optional.of(spanish));

            assertThat(service.getCohort(CohortLevel.undergraduate, "es")).isEqualTo(CohortResponse.from(spanish));
            verifyNoInteractions(translationService);
        }

        @Test
        void retranslatesWhenTranslationIsStale() {
            String hash = hasher.hash(CohortResponse.from(english));
            Cohort stale = new Cohort("undergraduate_es", CohortLevel.undergraduate, "es",
                    "Vieja", List.of(), null, "old-hash");
            Cohort fresh = new Cohort("undergraduate_es", CohortLevel.undergraduate, "es",
                    "Bienvenida", List.of(), null, hash);
            when(cohortRepository.findByLevelAndLanguage(CohortLevel.undergraduate, "en")).thenReturn(Optional.of(english));
            when(cohortRepository.findByLevelAndLanguage(CohortLevel.undergraduate, "es")).thenReturn(Optional.of(stale));
            when(translationService.translateCohort(english, "es", hash)).thenReturn(fresh);

            assertThat(service.getCohort(CohortLevel.undergraduate, "es")).isEqualTo(CohortResponse.from(fresh));
            verify(cohortRepository).save(fresh);
        }

        @Test
        void storageFieldsDoNotAffectTheHash() {
            Cohort sameContentDifferentStorage = new Cohort("other-id", CohortLevel.undergraduate, "xx",
                    english.title(), english.importantDates(), english.event(), "anything");

            assertThat(hasher.hash(CohortResponse.from(sameContentDifferentStorage))).isEqualTo(hasher.hash(CohortResponse.from(english)));
        }
    }

    @Nested
    class InductionEventsSection {
        private final List<InductionEvent> englishEvents = List.of(new InductionEvent("Fair", "star", "Clubs", "Go", "https://x"));
        private final InductionEvents english = new InductionEvents("events_en", "en", englishEvents, null);

        @Test
        void returnsEnglishWithoutTranslating() {
            when(eventsRepository.findByLanguage("en")).thenReturn(Optional.of(english));

            assertThat(service.getInductionEvents("en")).isEqualTo(englishEvents);
            verifyNoInteractions(translationService);
        }

        @Test
        void throwsWhenEnglishIsMissing() {
            when(eventsRepository.findByLanguage("en")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getInductionEvents("de"))
                    .isInstanceOf(RecordNotFoundException.class)
                    .hasMessage("Induction events not found");
        }

        @Test
        void returnsCachedTranslationWhenUpToDate() {
            String hash = hasher.hash(englishEvents);
            List<InductionEvent> german = List.of(new InductionEvent("Messe", "star", "Klubs", "Los", "https://x"));
            when(eventsRepository.findByLanguage("en")).thenReturn(Optional.of(english));
            when(eventsRepository.findByLanguage("de")).thenReturn(Optional.of(new InductionEvents("events_de", "de", german, hash)));

            assertThat(service.getInductionEvents("de")).isEqualTo(german);
            verifyNoInteractions(translationService);
        }

        @Test
        void translatesWhenTranslationIsMissing() {
            String hash = hasher.hash(englishEvents);
            List<InductionEvent> german = List.of(new InductionEvent("Messe", "star", "Klubs", "Los", "https://x"));
            InductionEvents fresh = new InductionEvents("events_de", "de", german, hash);
            when(eventsRepository.findByLanguage("en")).thenReturn(Optional.of(english));
            when(eventsRepository.findByLanguage("de")).thenReturn(Optional.empty());
            when(translationService.translateInductionEvents(english, "de", hash)).thenReturn(fresh);

            assertThat(service.getInductionEvents("de")).isEqualTo(german);
            verify(eventsRepository).save(fresh);
        }

        @Test
        void returnsEmptyListForNullEvents() {
            when(eventsRepository.findByLanguage("en")).thenReturn(Optional.of(new InductionEvents("events_en", "en", null, null)));

            assertThat(service.getInductionEvents("en")).isEmpty();
            verify(translationService, never()).translateInductionEvents(any(), anyString(), anyString());
        }
    }
}
