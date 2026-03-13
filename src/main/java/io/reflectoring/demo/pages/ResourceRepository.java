package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.ResourceCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends MongoRepository<ResourceCategory, String> {
    //ResourceCategory findAll();

}