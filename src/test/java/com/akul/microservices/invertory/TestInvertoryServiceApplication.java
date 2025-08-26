package com.akul.microservices.invertory;

import org.springframework.boot.SpringApplication;

public class TestInvertoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(InvertoryServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
