package com.divyansh.aitourplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AitourplannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AitourplannerApplication.class, args);
	}

}
