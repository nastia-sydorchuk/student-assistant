package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.onboarding.api.CohortResponse;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

// Each section is translated on its own, so editing one doesn't re-translate the others.
// A translation is redone when it is missing or was made from an older version of the English.
// Only the client-visible content is hashed, so storage fields never invalidate translations.
// With translation disabled (no API key), a missing or outdated translation is served in English.
@Service
public class OnboardingService {
    private final OnboardingStepsRepository stepsRepository;
    private final CohortRepository cohortRepository;
    private final InductionEventsRepository eventsRepository;
    private final TranslationService translationService;
    private final SourceHasher hasher;

    public OnboardingService(OnboardingStepsRepository stepsRepository,
                             CohortRepository cohortRepository,
                             InductionEventsRepository eventsRepository,
                             TranslationService translationService,
                             SourceHasher hasher) {
        this.stepsRepository = stepsRepository;
        this.cohortRepository = cohortRepository;
        this.eventsRepository = eventsRepository;
        this.translationService = translationService;
        this.hasher = hasher;
    }

    public List<OnboardingStep> getSteps(String language) {
        OnboardingSteps english = stepsRepository.findByLanguage("en")
                .orElseThrow(() -> new RecordNotFoundException("Steps not found."));

        OnboardingSteps result = english;
        if (!"en".equals(language)) {
            String hash = hasher.hash(english.steps());
            result = stepsRepository.findByLanguage(language)
                    .filter(existing -> hash.equals(existing.sourceHash()))
                    .orElseGet(() -> translationService.isEnabled()
                            ? stepsRepository.save(translationService.translateSteps(english, language, hash))
                            : english);
        }
        return result.steps() == null ? List.of() : result.steps();
    }

    public CohortResponse getCohort(CohortLevel level, String language) {
        Cohort english = cohortRepository.findByLevelAndLanguage(level, "en")
                .orElseThrow(() -> new RecordNotFoundException("Cohort not found for level: " + level));

        Cohort result = english;
        if (!"en".equals(language)) {
            String hash = hasher.hash(CohortResponse.from(english));
            result = cohortRepository.findByLevelAndLanguage(level, language)
                    .filter(existing -> hash.equals(existing.sourceHash()))
                    .orElseGet(() -> translationService.isEnabled()
                            ? cohortRepository.save(translationService.translateCohort(english, language, hash))
                            : english);
        }
        return CohortResponse.from(result);
    }

    public List<InductionEvent> getInductionEvents(String language) {
        InductionEvents english = eventsRepository.findByLanguage("en")
                .orElseThrow(() -> new RecordNotFoundException("Induction events not found"));

        InductionEvents result = english;
        if (!"en".equals(language)) {
            String hash = hasher.hash(english.events());
            result = eventsRepository.findByLanguage(language)
                    .filter(existing -> hash.equals(existing.sourceHash()))
                    .orElseGet(() -> translationService.isEnabled()
                            ? eventsRepository.save(translationService.translateInductionEvents(english, language, hash))
                            : english);
        }
        return result.events() == null ? List.of() : result.events();
    }
}
