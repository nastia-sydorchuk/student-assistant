package io.github.nastiasydorchuk.studentportal.resources;

import io.github.nastiasydorchuk.studentportal.common.error.RecordNotFoundException;
import io.github.nastiasydorchuk.studentportal.resources.api.ResourceCategoryResponse;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import io.github.nastiasydorchuk.studentportal.translation.SourceHasher;
import io.github.nastiasydorchuk.studentportal.translation.TranslationCache;
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

// The caching rule itself is covered by TranslationCacheTest; these check resource categories are wired into it
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
        when(translationService.isEnabled()).thenReturn(true);
        hasher = new SourceHasher(JsonMapper.builder().build());
        service = new ResourceService(repository, translationService, new TranslationCache(translationService, hasher));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        english = category("academic", "en", "Academic", null);
    }

    private static ResourceCategory category(String categoryId, String language, String title, String sourceHash) {
        return new ResourceCategory(categoryId + "_" + language, categoryId, language, title, "Description",
                List.of(new ResourceItem("Library", "book", null, null, "https://x", null, null, null, null)), sourceHash);
    }

    @Test
    void returnsEnglishWithoutTranslating() {
        when(repository.findByCategoryIdAndLanguage("academic", "en")).thenReturn(Optional.of(english));

        assertThat(service.getCategory("academic", "en")).isSameAs(english);
        verify(translationService, never()).translateCategory(any(), any(), any());
    }

    @Test
    void throwsWhenEnglishIsMissing() {
        when(repository.findByCategoryIdAndLanguage("missing", "en")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCategory("missing", "es"))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Resource category not found: missing");
    }

    @Test
    void returnsCachedTranslationWhenUpToDate() {
        String hash = hasher.hash(ResourceCategoryResponse.from(english));
        ResourceCategory spanish = category("academic", "es", "Académico", hash);
        when(repository.findByCategoryIdAndLanguage("academic", "en")).thenReturn(Optional.of(english));
        when(repository.findByCategoryIdAndLanguage("academic", "es")).thenReturn(Optional.of(spanish));

        assertThat(service.getCategory("academic", "es")).isSameAs(spanish);
        verify(translationService, never()).translateCategory(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void translatesAndSavesWhenOutdated() {
        String hash = hasher.hash(ResourceCategoryResponse.from(english));
        ResourceCategory fresh = category("academic", "es", "Académico", hash);
        when(repository.findByCategoryIdAndLanguage("academic", "en")).thenReturn(Optional.of(english));
        when(repository.findByCategoryIdAndLanguage("academic", "es"))
                .thenReturn(Optional.of(category("academic", "es", "Viejo", "old-hash")));
        when(translationService.translateCategory(english, "es", hash)).thenReturn(fresh);

        assertThat(service.getCategory("academic", "es")).isSameAs(fresh);
        verify(repository).save(fresh);
    }

    @Test
    void storageFieldsDoNotAffectTheHash() {
        ResourceCategory sameContent = category("academic", "xx", "Academic", "whatever");

        assertThat(hasher.hash(ResourceCategoryResponse.from(sameContent)))
                .isEqualTo(hasher.hash(ResourceCategoryResponse.from(english)));
    }

    @Test
    void listsCategoryIdsFromEnglishDocuments() {
        when(repository.findByLanguage("en")).thenReturn(List.of(english, category("wellbeing", "en", "Wellbeing", null)));

        assertThat(service.listCategoryIds()).containsExactly("academic", "wellbeing");
    }
}
