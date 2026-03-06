package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.WelcomeDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagesRepository extends MongoRepository<WelcomeDTO, String> {
    WelcomeDTO findBySource(String source);

    @Query(value = "{ 'cohorts.level': ?0 }", fields = "{ 'commonSteps': 1, 'cohorts.$': 1, 'inductionEvents': 1 }")
    Optional<WelcomeDTO> findByCohortLevel(String level);

    @Query(
            value = "{ 'inductionEvents': { $exists: true } }",
            fields = "{ 'inductionEvents': 1, '_id': 0 }"
    )
    Optional<WelcomeDTO> findInductionEventsOnly();
}
