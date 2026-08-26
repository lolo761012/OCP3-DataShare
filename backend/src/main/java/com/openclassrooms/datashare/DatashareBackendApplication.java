package com.openclassrooms.datashare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatashareBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatashareBackendApplication.class, args);
	}

}
