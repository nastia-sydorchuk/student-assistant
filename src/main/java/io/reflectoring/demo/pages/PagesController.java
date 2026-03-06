package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.Cohort;
import io.reflectoring.demo.dto.InductionEvent;
import io.reflectoring.demo.dto.WelcomeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
public class PagesController {

    private final PagesService service;

    public PagesController(PagesService service) {
        this.service = service;
    }

    @GetMapping("/welcome")
    public ResponseEntity<WelcomeDTO> getSteps() {
        String defaultSource = "https://www.hw.ac.uk/students/new-students/uk/welcome-to-heriot-watt-university";
        return ResponseEntity.ok(service.getRecordBySource(defaultSource));
    }

    @GetMapping("/welcome/{level}")
    public ResponseEntity<Cohort> getInductionForLevel(@PathVariable String level) {
        return ResponseEntity.ok(service.getPageForLevel(level));
    }

    @GetMapping("/welcome/induction-events")
    public ResponseEntity<List<InductionEvent>> getInductionEvents() {
        return ResponseEntity.ok(service.getInductionEvents());
    }
}