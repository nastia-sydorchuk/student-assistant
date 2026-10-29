package io.github.nastiasydorchuk.studentportal;

import io.github.nastiasydorchuk.studentportal.onboarding.api.CohortResponse;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import io.github.nastiasydorchuk.studentportal.resources.api.ResourceCategoryResponse;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainMappingTest {

    @Test
    void cohortResponsePassesMissingDatesThroughAsNull() {
        Cohort cohort = new Cohort("id", CohortLevel.undergraduate, "en", "Title", null, null, null);

        assertThat(CohortResponse.from(cohort).importantDates()).isNull();
    }

    @Test
    void cohortResponseDropsStorageFields() {
        InductionProgramme event = new InductionProgramme("Induction", "Desc", List.of(), List.of(), null, null);
        List<ImportantDate> dates = List.of(new ImportantDate("Enrol", "1 Sept", DateType.action));
        Cohort cohort = new Cohort("pg_es", CohortLevel.postgraduate, "es", "Title", dates, event, "hash");

        assertThat(CohortResponse.from(cohort)).isEqualTo(new CohortResponse(CohortLevel.postgraduate, "Title", dates, event));
    }

    @Test
    void resourceCategoryResponseDropsStorageFields() {
        List<ResourceItem> items = List.of(new ResourceItem("Library", "book", null, null, null, null, null, null, null));
        ResourceCategory category = new ResourceCategory("academic_es", "academic", "es", "Title", "Desc", items, "hash");

        assertThat(ResourceCategoryResponse.from(category))
                .isEqualTo(new ResourceCategoryResponse("academic", "Title", "Desc", items));
    }
}
