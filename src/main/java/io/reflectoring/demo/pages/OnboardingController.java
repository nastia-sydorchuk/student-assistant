package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.Cohort;
import io.reflectoring.demo.dto.InductionEvent;
import io.reflectoring.demo.dto.PortalCard;
import io.reflectoring.demo.dto.StepDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService service;

    public OnboardingController(OnboardingService service) {
        this.service = service;
    }

    @GetMapping("/steps")
    public ResponseEntity<List<StepDTO>> getSteps(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getStepsBySource(language));
    }

    @GetMapping("/{level}")
    public ResponseEntity<Cohort> getInductionForLevel(@PathVariable String level, @RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getCohortByLevel(level, language));
    }

    @GetMapping("/induction-events")
    public ResponseEntity<List<InductionEvent>> getInductionEvents(@RequestHeader(name = "Accept-Language", defaultValue = "en") String language) {
        return ResponseEntity.ok(service.getInductionEvents(language));
    }
}