package com.rental.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RentalToolManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalToolManagementApplication.class, args);
	}

}
