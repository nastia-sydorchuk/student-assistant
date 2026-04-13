package io.reflectoring.demo.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "onboarding")
public class Onboarding {
    @Id
    private String id;           // e.g., "default_es"

    private String configId;     // e.g., "default"
    private String language;     // e.g., "es"

    private List<StepDTO> steps;
    private List<Cohort> cohorts;
    private List<InductionEvent> inductionEvents;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<StepDTO> getSteps() { return steps; }
    public void setSteps(List<StepDTO> steps) { this.steps = steps; }

    public List<Cohort> getCohorts() { return cohorts; }
    public void setCohorts(List<Cohort> cohorts) { this.cohorts = cohorts; }

    public List<InductionEvent> getInductionEvents() { return inductionEvents; }
    public void setInductionEvents(List<InductionEvent> inductionEvents) { this.inductionEvents = inductionEvents; }
}
