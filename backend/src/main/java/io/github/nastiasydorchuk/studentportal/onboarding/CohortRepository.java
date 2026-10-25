package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.Cohort;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.CohortLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CohortRepository extends MongoRepository<Cohort, String> {
    Optional<Cohort> findByLevelAndLanguage(CohortLevel level, String language);
}
