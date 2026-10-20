package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.CohortProjection;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.InductionEventProjection;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.Onboarding;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.StepProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingRepository extends MongoRepository<Onboarding, String> {
    @Query(value = "{ 'language': ?0 }", fields = "{ 'steps': 1, '_id': 0 }")
    Optional<StepProjection> findStepsByLanguage(String language);

    @Query(value = "{ 'cohorts.level': ?0, 'language': ?1 }", fields = "{ 'cohorts.$': 1, '_id': 0 }")
    Optional<CohortProjection> findByCohortLevelAndLanguage(String level, String language);

    @Query(value = "{ 'inductionEvents': { $exists: true }, 'language': ?0 }", fields = "{ 'inductionEvents': 1, '_id': 0 }")
    Optional<InductionEventProjection> findInductionEventsByLanguage(String language);

    Optional<Onboarding> findByLanguage(String language);
}
