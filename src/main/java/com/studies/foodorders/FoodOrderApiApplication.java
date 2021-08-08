package com.studies.foodorders;

import com.studies.foodorders.infrastructure.repositories.springcustom.CustomJpaRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = CustomJpaRepositoryImpl.class)
public class FoodOrderApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodOrderApiApplication.class, args);
	}

}
