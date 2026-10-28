package io.github.nastiasydorchuk.studentportal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Dummy values stand in for the env vars; neither Mongo nor Translate connects at startup
// (seeding is off because it would query Mongo)
@SpringBootTest(properties = {
		"spring.mongodb.uri=mongodb://localhost:27017/test",
		"google.translate.api-key=test-key",
		"app.seed.enabled=false"
})
class StudentPortalApplicationTests {

	@Test
	void contextLoads() {
	}

}
