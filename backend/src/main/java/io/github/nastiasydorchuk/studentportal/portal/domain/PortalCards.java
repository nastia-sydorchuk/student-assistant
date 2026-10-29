package io.github.nastiasydorchuk.studentportal.portal.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

// All cards for one language in a single document, so a translation is replaced atomically
// and the cards keep the order they are listed in
@Document(collection = "portal_card_sets")
public record PortalCards(
        @Id String id,             // e.g., "cards_es"
        String language,
        List<PortalCard> cards,
        String sourceHash // hash of the English document this was translated from; null on English
) {}
