package io.github.nastiasydorchuk.studentportal;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import io.github.nastiasydorchuk.studentportal.portal.api.PortalCardResponse;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.resources.api.ResourceCategoryResponse;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainMappingTest {

    @Test
    void cohortDocumentPassesMissingDatesThroughAsNull() {
        CohortDocument document = new CohortDocument("id", CohortLevel.undergraduate, "en", "Title", null, null, null);

        assertThat(document.toCohort().importantDates()).isNull();
    }

    @Test
    void cohortDocumentExposesOnlyContentAsCohort() {
        EventDetails event = new EventDetails("Induction", "Desc", List.of(), List.of(), null, null);
        List<ImportantDate> dates = List.of(new ImportantDate("Enrol", "1 Sept", DateType.action));
        CohortDocument document = new CohortDocument("pg_es", CohortLevel.postgraduate, "es", "Title", dates, event, "hash");

        assertThat(document.toCohort()).isEqualTo(new Cohort(CohortLevel.postgraduate, "Title", dates, event));
    }

    @Test
    void portalCardResponseDropsStorageFields() {
        PortalCard card = new PortalCard("academic_es", "Title", "/route", "icon", "Desc", "academic", "es", "hash");

        assertThat(PortalCardResponse.from(card))
                .isEqualTo(new PortalCardResponse("academic", "Title", "/route", "icon", "Desc"));
    }

    @Test
    void resourceCategoryResponseDropsStorageFields() {
        List<ResourceItem> items = List.of(new ResourceItem("Library", "book", null, null, null, null, null, null, null));
        ResourceCategory category = new ResourceCategory();
        category.setId("academic_es");
        category.setLanguage("es");
        category.setSourceHash("hash");
        category.setCategoryId("academic");
        category.setCategoryTitle("Title");
        category.setDescription("Desc");
        category.setItems(items);

        assertThat(ResourceCategoryResponse.from(category))
                .isEqualTo(new ResourceCategoryResponse("academic", "Title", "Desc", items));
    }
}
