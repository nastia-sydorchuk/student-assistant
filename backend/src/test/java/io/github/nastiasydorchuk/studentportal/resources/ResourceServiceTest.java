package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.resources.api.ResourceCategoryResponse;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResourceServiceTest {

    private ResourceRepository repository;
    private TranslationService translationService;
    private SourceHasher hasher;
    private ResourceService service;
    private ResourceCategory english;

    @BeforeEach
    void setUp() {
        repository = mock(ResourceRepository.class);
        translationService = mock(TranslationService.class);
        hasher = new SourceHasher(JsonMapper.builder().build());
        service = new ResourceService(repository, translationService, hasher);
        english = category("academic", "en", "Academic", null);
    }

    private static ResourceCategory category(String categoryId, String language, String title, String sourceHash) {
        ResourceCategory category = new ResourceCategory();
        category.setId(categoryId + "_" + language);
        category.setCategoryId(categoryId);
        category.setLanguage(language);
        category.setCategoryTitle(title);
        category.setDescription("Description");
        category.setItems(List.of(new ResourceItem("Library", "book", null, null, "https://x", null, null, null, null)));
        category.setSourceHash(sourceHash);
        return category;
    }

    @Test
    void returnsEnglishWithoutTranslating() {
        when(repository.findByCategoryIdAndLanguage("academic", "en")).thenReturn(Optional.of(english));

        assertThat(service.getCategory("academic", "en")).isSameAs(english);
        verifyNoInteractions(translationService);
    }

    @Test
    void throwsWhenEnglishCategoryIsMissing() {
        when(repository.findByCategoryIdAndLanguage("missing", "en")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCategory("missing", "en"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Resource category not found: missing");
    }

    @Test
    void throwsWhenEnglishSourceIsMissingForTranslation() {
        when(repository.findByCategoryIdAndLanguage("missing", "en")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCategory("missing", "es"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Resource category not found: missing");
        verifyNoInteractions(translationService);
    }

    @Test
    void returnsCachedTranslationWhenUpToDate() {
        String hash = hasher.hash(ResourceCategoryResponse.from(english));
        ResourceCategory spanish = category("academic", "es", "Académico", hash);
        when(repository.findByCategoryIdAndLanguage("academic", "en")).thenReturn(Optional.of(english));
        when(repository.findByCategoryIdAndLanguage("academic", "es")).thenReturn(Optional.of(spanish));

        assertThat(service.getCategory("academic", "es")).isSameAs(spanish);
        verifyNoInteractions(translationService);
        verify(repository, never()).save(any());
    }

    @Test
    void retranslatesWhenTranslationIsStale() {
        String hash = hasher.hash(ResourceCategoryResponse.from(english));
        ResourceCategory stale = category("academic", "es", "Viejo", "old-hash");
        ResourceCategory fresh = category("academic", "es", "Académico", null);
        when(repository.findByCategoryIdAndLanguage("academic", "en")).thenReturn(Optional.of(english));
        when(repository.findByCategoryIdAndLanguage("academic", "es")).thenReturn(Optional.of(stale), Optional.of(fresh));
        when(translationService.translateCategory(english, "es")).thenReturn(fresh);

        assertThat(service.getCategory("academic", "es")).isSameAs(fresh);
        assertThat(fresh.getSourceHash()).isEqualTo(hash);
        verify(repository).save(fresh);
    }

    @Test
    void translatesWhenTranslationIsMissing() {
        ResourceCategory fresh = category("academic", "es", "Académico", null);
        when(repository.findByCategoryIdAndLanguage("academic", "en")).thenReturn(Optional.of(english));
        when(repository.findByCategoryIdAndLanguage("academic", "es")).thenReturn(Optional.empty(), Optional.of(fresh));
        when(translationService.translateCategory(english, "es")).thenReturn(fresh);

        assertThat(service.getCategory("academic", "es")).isSameAs(fresh);
        verify(repository).save(fresh);
    }

    @Test
    void storageFieldsDoNotAffectTheHash() {
        ResourceCategory sameContent = category("academic", "xx", "Academic", "whatever");
        sameContent.setId("other-id");

        assertThat(hasher.hash(ResourceCategoryResponse.from(sameContent)))
                .isEqualTo(hasher.hash(ResourceCategoryResponse.from(english)));
    }

    @Test
    void listsCategoryIdsFromEnglishDocuments() {
        when(repository.findByLanguage("en")).thenReturn(List.of(english, category("wellbeing", "en", "Wellbeing", null)));

        assertThat(service.listCategoryIds()).containsExactly("academic", "wellbeing");
    }
}
