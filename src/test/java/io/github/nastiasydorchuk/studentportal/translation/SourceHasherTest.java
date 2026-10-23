package io.github.nastiasydorchuk.studentportal.translation;

import io.github.nastiasydorchuk.studentportal.onboarding.domain.Step;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SourceHasherTest {

    private final SourceHasher hasher = new SourceHasher(JsonMapper.builder().build());

    @Test
    void producesSha256Hex() {
        assertThat(hasher.hash(List.of("a"))).matches("[0-9a-f]{64}");
    }

    @Test
    void equalContentHashesTheSame() {
        List<Step> first = List.of(new Step(1, "Summary", "Desc", "Go", "https://example.com"));
        List<Step> second = List.of(new Step(1, "Summary", "Desc", "Go", "https://example.com"));

        assertThat(hasher.hash(first)).isEqualTo(hasher.hash(second));
    }

    @Test
    void changedContentHashesDifferently() {
        List<Step> original = List.of(new Step(1, "Summary", "Desc", "Go", "https://example.com"));
        List<Step> edited = List.of(new Step(1, "Summary", "Edited desc", "Go", "https://example.com"));

        assertThat(hasher.hash(original)).isNotEqualTo(hasher.hash(edited));
    }

    @Test
    void orderMatters() {
        assertThat(hasher.hash(List.of("a", "b"))).isNotEqualTo(hasher.hash(List.of("b", "a")));
    }
}
