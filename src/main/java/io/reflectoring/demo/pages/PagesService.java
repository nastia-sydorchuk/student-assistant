package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.Cohort;
import io.reflectoring.demo.dto.InductionEvent;
import io.reflectoring.demo.dto.WelcomeDTO;
import io.reflectoring.demo.exceptions.RecordNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PagesService {

    private final PagesRepository repository;

    public PagesService(PagesRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public WelcomeDTO getRecordBySource(String source) {
        return Optional.ofNullable(repository.findBySource(source))
                .orElseThrow(() -> new RecordNotFoundException("Source not found: " + source));
    }

    public Cohort getPageForLevel(String level) {
        WelcomeDTO master = repository.findByCohortLevel(level)
                .orElseThrow(() -> new RuntimeException("Welcome guide not found for level: " + level));

        if (master.cohorts() == null || master.cohorts().isEmpty()) {
            throw new RuntimeException("Cohort data missing for: " + level);
        }
        Cohort specificCohort = master.cohorts().get(0);

        return new Cohort(
                specificCohort.level(),
                specificCohort.title(),
                specificCohort.importantDates(),
                specificCohort.event()
        );
    }

    public List<InductionEvent> getInductionEvents() {
        WelcomeDTO partialDoc = repository.findInductionEventsOnly()
                .orElseThrow(() -> new RuntimeException("Induction events not found in database"));

        return partialDoc.inductionEvents();
    }
}