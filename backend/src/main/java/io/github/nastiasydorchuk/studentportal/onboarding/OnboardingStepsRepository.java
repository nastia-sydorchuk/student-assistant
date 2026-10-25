package io.github.nastiasydorchuk.studentportal.onboarding;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.OnboardingSteps;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingStepsRepository extends MongoRepository<OnboardingSteps, String> {
    Optional<OnboardingSteps> findByLanguage(String language);
}
