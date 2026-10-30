package io.github.nastiasydorchuk.studentportal.seed;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Runs against the real seed files in src/main/resources/seed, so a broken export fails the build
class SeedDataLoaderTest {

    private static final List<String> COLLECTIONS =
            List.of("cohorts", "induction_events", "onboarding_steps", "portal_card_sets", "resource_categories");

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final SeedDataLoader loader = new SeedDataLoader(mongoTemplate);

    @Test
    void seedsEveryEmptyCollection() throws Exception {
        when(mongoTemplate.count(any(Query.class), anyString())).thenReturn(0L);

        loader.run(null);

        for (String collection : COLLECTIONS) {
            verify(mongoTemplate).insert(argThat((List<Document> docs) -> !docs.isEmpty()), eq(collection));
        }
    }

    @Test
    void neverTouchesACollectionThatHasData() throws Exception {
        when(mongoTemplate.count(any(Query.class), anyString())).thenReturn(0L);
        when(mongoTemplate.count(any(Query.class), eq("portal_card_sets"))).thenReturn(1L);

        loader.run(null);

        verify(mongoTemplate, never()).insert(anyList(), eq("portal_card_sets"));
        verify(mongoTemplate).insert(anyList(), eq("cohorts"));
    }

    @Test
    void seedFilesContainOnlyEnglishSourceContent() throws Exception {
        for (String collection : COLLECTIONS) {
            List<Document> documents = SeedDataLoader.parse(new ClassPathResource("seed/" + collection + ".json"));

            assertThat(documents).as(collection).isNotEmpty()
                    .allSatisfy(doc -> assertThat(doc.getString("language")).isEqualTo("en"));
        }
    }
}
