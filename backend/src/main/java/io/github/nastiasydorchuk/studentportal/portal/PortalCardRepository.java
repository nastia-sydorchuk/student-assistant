package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalCardRepository extends MongoRepository<PortalCard, String> {
    List<PortalCard> findByLanguage(String language);

    // Stable order so the English cards always hash the same
    List<PortalCard> findByLanguageOrderByCardIdAsc(String language);
}