package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceRepository extends MongoRepository<ResourceCategory, String> {
    Optional<ResourceCategory> findByCategoryIdAndLanguage(String categoryId, String language);

}