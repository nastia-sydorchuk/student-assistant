package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.translation.TranslationCache;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.onboarding.api.CohortResponse;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

// Each section is translated on its own, so editing one doesn't re-translate the others
@Service
public class OnboardingService {
    private final OnboardingStepsRepository stepsRepository;
    private final CohortRepository cohortRepository;
    private final InductionEventsRepository eventsRepository;
    private final TranslationService translationService;
    private final TranslationCache translations;

    public OnboardingService(OnboardingStepsRepository stepsRepository,
                             CohortRepository cohortRepository,
                             InductionEventsRepository eventsRepository,
                             TranslationService translationService,
                             TranslationCache translations) {
        this.stepsRepository = stepsRepository;
        this.cohortRepository = cohortRepository;
        this.eventsRepository = eventsRepository;
        this.translationService = translationService;
        this.translations = translations;
    }

    public List<OnboardingStep> getSteps(String language) {
        OnboardingSteps english = stepsRepository.findByLanguage("en")
                .orElseThrow(() -> new RecordNotFoundException("Steps not found."));

        OnboardingSteps result = translations.localize(language, english, english.steps(),
                stepsRepository::findByLanguage, OnboardingSteps::sourceHash,
                (en, hash) -> stepsRepository.save(translationService.translateSteps(en, language, hash)));
        return result.steps() == null ? List.of() : result.steps();
    }

    public CohortResponse getCohort(CohortLevel level, String language) {
        Cohort english = cohortRepository.findByLevelAndLanguage(level, "en")
                .orElseThrow(() -> new RecordNotFoundException("Cohort not found for level: " + level));

        Cohort result = translations.localize(language, english, CohortResponse.from(english),
                lang -> cohortRepository.findByLevelAndLanguage(level, lang), Cohort::sourceHash,
                (en, hash) -> cohortRepository.save(translationService.translateCohort(en, language, hash)));
        return CohortResponse.from(result);
    }

    public List<InductionEvent> getInductionEvents(String language) {
        InductionEvents english = eventsRepository.findByLanguage("en")
                .orElseThrow(() -> new RecordNotFoundException("Induction events not found"));

        InductionEvents result = translations.localize(language, english, english.events(),
                eventsRepository::findByLanguage, InductionEvents::sourceHash,
                (en, hash) -> eventsRepository.save(translationService.translateInductionEvents(en, language, hash)));
        return result.events() == null ? List.of() : result.events();
    }
}
