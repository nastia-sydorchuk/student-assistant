package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.Cohort;
import io.reflectoring.demo.dto.InductionEvent;
import io.reflectoring.demo.dto.StepDTO;
import io.reflectoring.demo.dto.WelcomeDTO;
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
    public ResponseEntity<List<StepDTO>> getSteps() {
        String defaultSource = "https://www.hw.ac.uk/students/new-students/uk/welcome-to-heriot-watt-university";
        return ResponseEntity.ok(service.getRecordBySource(defaultSource));
    }

    @GetMapping("/{level}")
    public ResponseEntity<Cohort> getInductionForLevel(@PathVariable String level) {
        return ResponseEntity.ok(service.getPageForLevel(level));
    }

    @GetMapping("/induction-events")
    public ResponseEntity<List<InductionEvent>> getInductionEvents() {
        return ResponseEntity.ok(service.getInductionEvents());
    }
}