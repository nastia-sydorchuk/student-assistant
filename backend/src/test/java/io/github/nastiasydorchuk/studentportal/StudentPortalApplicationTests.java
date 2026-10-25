package io.github.nastiasydorchuk.studentportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Dummy values stand in for the env vars; neither Mongo nor Translate connects at startup
@SpringBootTest(properties = {
		"spring.mongodb.uri=mongodb://localhost:27017/test",
		"google.translate.api-key=test-key"
})
class StudentPortalApplicationTests {

	@Test
	void contextLoads() {
	}

}
