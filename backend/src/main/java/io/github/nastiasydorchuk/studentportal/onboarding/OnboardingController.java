package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.common.language.LanguageResolver;
import io.github.nastiasydorchuk.studentportal.onboarding.api.CohortResponse;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.CohortLevel;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.InductionEvent;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.OnboardingStep;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService service;
    private final LanguageResolver languageResolver;

    public OnboardingController(OnboardingService service, LanguageResolver languageResolver) {
        this.service = service;
        this.languageResolver = languageResolver;
    }

    @GetMapping("/steps")
    public List<OnboardingStep> getSteps(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return service.getSteps(languageResolver.resolve(language));
    }

    @GetMapping("/levels/{level}")
    public CohortResponse getCohort(@PathVariable CohortLevel level, @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return service.getCohort(level, languageResolver.resolve(language));
    }

    @GetMapping("/induction-events")
    public List<InductionEvent> getInductionEvents(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return service.getInductionEvents(languageResolver.resolve(language));
    }
}