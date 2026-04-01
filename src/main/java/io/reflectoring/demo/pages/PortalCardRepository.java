package io.reflectoring.demo.pages;

import io.reflectoring.demo.dto.PortalCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalCardRepository extends MongoRepository<PortalCard, String> {
    List<PortalCard> findByLanguage(String language);
}