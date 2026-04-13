package io.reflectoring.demo.pages;

import io.reflectoring.demo.TranslationService;
import io.reflectoring.demo.dto.*;
import io.reflectoring.demo.exceptions.RecordNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {
    private final OnboardingRepository repository;
    private final TranslationService translationService;

    private void ensureTranslationExists(String language) {
        if (repository.findByLanguage(language).isEmpty()) {
            Onboarding english = repository.findByLanguage("en")
                    .orElseThrow(() -> new RecordNotFoundException("English onboarding not found"));

            Onboarding translated = translationService.translateOnboarding(english, language);
            repository.save(translated);
        }
    }

    public List<StepDTO> getStepsBySource(String language) {
        ensureTranslationExists(language);
        List<StepProjection> results = repository.findStepsByLanguage(language);
        if (results.isEmpty()) {
            throw new RecordNotFoundException("Steps not found.");
        }
        return results.stream().flatMap(p -> p.getSteps().stream()).toList();
    }

    public Cohort getCohortByLevel(String level, String language) {
        ensureTranslationExists(language);
        return repository.findByCohortLevelAndLanguage(level, language)
                .map(p -> p.getCohorts().get(0))
                .orElseThrow(() -> new RuntimeException("Cohort not found for level: " + level));
    }

    public List<InductionEvent> getInductionEvents(String language) {
        ensureTranslationExists(language);
        return repository.findInductionEventsByLanguage(language)
                .map(InductionEventProjection::getInductionEvents)
                .orElseThrow(() -> new RuntimeException("Induction events not found"));
    }
}