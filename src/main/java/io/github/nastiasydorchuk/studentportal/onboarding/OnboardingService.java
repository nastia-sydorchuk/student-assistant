package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OnboardingService {
    private final OnboardingRepository repository;
    private final TranslationService translationService;

    public OnboardingService(OnboardingRepository repository, TranslationService translationService) {
        this.repository = repository;
        this.translationService = translationService;
    }

    private void ensureTranslationExists(String language) {
        if (repository.findByLanguage(language).isEmpty()) {
            Onboarding english = repository.findByLanguage("en")
                    .orElseThrow(() -> new RecordNotFoundException("English onboarding not found"));

            Onboarding translated = translationService.translateOnboarding(english, language);
            repository.save(translated);
        }
    }

    public List<Step> getStepsBySource(String language) {
        ensureTranslationExists(language);
        StepProjection result = repository.findStepsByLanguage(language)
                .orElseThrow(() -> new RecordNotFoundException("Steps not found."));
        return result.getSteps() == null ? List.of() : result.getSteps();
    }

    public Cohort getCohortByLevel(String level, String language) {
        ensureTranslationExists(language);
        return repository.findByCohortLevelAndLanguage(level, language)
                .map(CohortProjection::getCohorts)
                .filter(cohorts -> cohorts != null && !cohorts.isEmpty())
                .map(cohorts -> cohorts.get(0))
                .orElseThrow(() -> new RecordNotFoundException("Cohort not found for level: " + level));
    }

    public List<InductionEvent> getInductionEvents(String language) {
        ensureTranslationExists(language);
        return repository.findInductionEventsByLanguage(language)
                .map(InductionEventProjection::getInductionEvents)
                .orElseThrow(() -> new RecordNotFoundException("Induction events not found"));
    }
}