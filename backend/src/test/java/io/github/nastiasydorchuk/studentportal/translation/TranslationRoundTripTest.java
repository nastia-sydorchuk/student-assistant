package io.github.nastiasydorchuk.studentportal.translation;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import io.github.nastiasydorchuk.studentportal.onboarding.domain.*;
import io.github.nastiasydorchuk.studentportal.portal.domain.PortalCard;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceCategory;
import io.github.nastiasydorchuk.studentportal.resources.domain.ResourceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Fills every record component by reflection, translates, and checks each one came back either
// translated or untouched. Fields are never listed by hand, so a component added later is covered
// automatically and a translator that forgets to copy it fails here.
class TranslationRoundTripTest {

    private static final String PREFIX = "es:";
    // Storage keys the translator rewrites on purpose (e.g. "academic_es")
    private static final Set<String> STORAGE_FIELDS = Set.of("id", "language", "sourceHash");

    private TranslationService service;

    @BeforeEach
    void setUp() {
        Translate translate = mock(Translate.class);
        when(translate.translate(anyString(), any(), any(), any())).thenAnswer(invocation -> {
            Translation translation = mock(Translation.class);
            when(translation.getTranslatedText()).thenReturn(PREFIX + invocation.getArgument(0));
            return translation;
        });
        service = new TranslationService(Optional.of(translate));
    }

    @Test
    void resourceItemsKeepEveryField() {
        ResourceItem item = sample(ResourceItem.class, "item");
        ResourceCategory english = new ResourceCategory();
        english.setCategoryId("academic");
        english.setCategoryTitle("Academic");
        english.setDescription("Study help");
        english.setItems(List.of(item));

        ResourceCategory translated = service.translateCategory(english, "es");

        assertThat(translated.getCategoryTitle()).isEqualTo(PREFIX + "Academic");
        assertThat(translated.getDescription()).isEqualTo(PREFIX + "Study help");
        assertPreserved(english.getItems(), translated.getItems(), "items");
    }

    @Test
    void portalCardsKeepEveryField() {
        List<PortalCard> english = List.of(sample(PortalCard.class, "card"));

        assertPreserved(english, service.translatePortalCards(english, "es", "hash"), "cards");
    }

    @Test
    void stepsKeepEveryField() {
        OnboardingSteps english = sample(OnboardingSteps.class, "steps");

        assertPreserved(english, service.translateSteps(english, "es", "hash"), "steps");
    }

    @Test
    void cohortsKeepEveryNestedField() {
        Cohort english = sample(Cohort.class, "cohort");

        assertPreserved(english, service.translateCohort(english, "es", "hash"), "cohort");
    }

    @Test
    void inductionEventsKeepEveryField() {
        InductionEvents english = sample(InductionEvents.class, "events");

        assertPreserved(english, service.translateInductionEvents(english, "es", "hash"), "events");
    }

    // Builds a value with every component populated and distinct, recursing into records and lists
    @SuppressWarnings("unchecked")
    private static <T> T sample(Class<T> type, String name) {
        try {
            Class<?>[] parameterTypes = Arrays.stream(type.getRecordComponents())
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);
            Object[] args = Arrays.stream(type.getRecordComponents())
                    .map(component -> sampleValue(component.getType(), component.getGenericType(),
                            name + "." + component.getName()))
                    .toArray();
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot build sample " + type, e);
        }
    }

    private static Object sampleValue(Class<?> type, Type genericType, String name) {
        if (type == String.class) return name;
        if (type == int.class || type == Integer.class) return 7;
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants[constants.length - 1];
        }
        if (type == List.class) {
            Type element = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            return List.of(sampleValue((Class<?>) element, element, name + "[0]"));
        }
        if (type.isRecord()) return sample(type, name);
        throw new IllegalArgumentException("Add a sample for " + type + " at " + name);
    }

    // Every non-storage field must be the original or its translation: never null, dropped or swapped
    private static void assertPreserved(Object original, Object translated, String path) {
        if (original instanceof List<?> originals) {
            assertThat(translated).as(path).isInstanceOf(List.class);
            List<?> translations = (List<?>) translated;
            assertThat(translations).as(path).hasSameSizeAs(originals);
            for (int i = 0; i < originals.size(); i++) {
                assertPreserved(originals.get(i), translations.get(i), path + "[" + i + "]");
            }
        } else if (original instanceof Record) {
            assertThat(translated).as(path).isInstanceOf(original.getClass());
            for (RecordComponent component : original.getClass().getRecordComponents()) {
                if (STORAGE_FIELDS.contains(component.getName())) continue;
                assertPreserved(read(component, original), read(component, translated),
                        path + "." + component.getName());
            }
        } else if (original instanceof String text) {
            assertThat(translated).as(path).isIn(text, PREFIX + text);
        } else {
            assertThat(translated).as(path).isEqualTo(original);
        }
    }

    private static Object read(RecordComponent component, Object record) {
        try {
            return component.getAccessor().invoke(record);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
