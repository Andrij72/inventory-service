package com.akul.microservices.invertory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class InvertoryServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
