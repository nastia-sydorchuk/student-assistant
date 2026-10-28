package io.github.nastiasydorchuk.studentportal;

import io.github.nastiasydorchuk.studentportal.translation.TranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// A reviewer without a Google Cloud billing account must still be able to run the app
@SpringBootTest(properties = {
		"spring.mongodb.uri=mongodb://localhost:27017/test",
		"google.translate.api-key=",
		"app.seed.enabled=false"
})
class StartsWithoutTranslateKeyTests {

	@Autowired
	private TranslationService translationService;

	@Test
	void startsWithTranslationDisabled() {
		assertThat(translationService.isEnabled()).isFalse();
	}

}
