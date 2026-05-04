package com.internship.tool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:tool-application-tests;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=none"
})
class ToolApplicationTests {

	@Test
	void contextLoads() {
	}

}
