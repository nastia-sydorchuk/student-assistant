package io.github.nastiasydorchuk.studentportal.portal;

import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCards;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortalCardsRepository extends MongoRepository<PortalCards, String> {
    Optional<PortalCards> findByLanguage(String language);
}
