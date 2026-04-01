package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.StepDTO;
import io.reflectoring.demo.dto.WelcomeDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingRepository extends MongoRepository<WelcomeDTO, String> {
    @Query(value = "{ 'steps': { $exists: true } }", fields = "{ 'steps': 1, '_id': 0 }")
    List<StepDTO> findBySource(String source);

    @Query(value = "{ 'cohorts.level': ?0 }", fields = "{ 'cohorts.$': 1, '_id': 0 }")
    Optional<WelcomeDTO> findByCohortLevel(String level);

    @Query(
            value = "{ 'inductionEvents': { $exists: true } }",
            fields = "{ 'inductionEvents': 1, '_id': 0 }"
    )
    Optional<WelcomeDTO> findInductionEventsOnly();
}
