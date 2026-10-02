package io.reflectoring.demo.pages;

import io.reflectoring.demo.common.language.LanguageResolver;
import io.reflectoring.demo.dto.Cohort;
import io.reflectoring.demo.dto.InductionEvent;
import io.reflectoring.demo.dto.StepDTO;
import io.reflectoring.demo.exceptions.RecordNotFoundException;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<StepDTO>> getSteps(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getStepsBySource(languageResolver.resolve(language)));
    }

    @GetMapping("/{level}")
    public ResponseEntity<Cohort> getInductionForLevel(@PathVariable String level, @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getCohortByLevel(level, languageResolver.resolve(language)));
    }

    @GetMapping("/induction-events")
    public ResponseEntity<List<InductionEvent>> getInductionEvents(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getInductionEvents(languageResolver.resolve(language)));
    }
}