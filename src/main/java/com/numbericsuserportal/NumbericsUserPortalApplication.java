package com.numbericsuserportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NumbericsUserPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(NumbericsUserPortalApplication.class, args);
	}

}
