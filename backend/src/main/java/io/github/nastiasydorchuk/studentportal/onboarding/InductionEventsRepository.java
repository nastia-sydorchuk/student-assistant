package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.InductionEvents;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InductionEventsRepository extends MongoRepository<InductionEvents, String> {
    Optional<InductionEvents> findByLanguage(String language);
}
