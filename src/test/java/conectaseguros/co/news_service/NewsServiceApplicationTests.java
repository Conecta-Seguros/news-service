package conectaseguros.co.news_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NewsServiceApplicationTests {

	@Test
	void contextLoads() {
		// This test will load the Spring Boot context
		// but should not fail with test profile
	}

}
