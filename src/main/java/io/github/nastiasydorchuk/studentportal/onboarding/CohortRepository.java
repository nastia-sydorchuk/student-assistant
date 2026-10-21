package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.CohortDocument;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.CohortLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CohortRepository extends MongoRepository<CohortDocument, String> {
    Optional<CohortDocument> findByLevelAndLanguage(CohortLevel level, String language);
}
