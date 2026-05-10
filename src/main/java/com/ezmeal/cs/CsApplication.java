package com.ezmeal.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.ezmeal.cs", "com.ezmeal.common"})
@EntityScan(basePackages = {"com.ezmeal.cs", "com.ezmeal.common"})
@EnableJpaRepositories(basePackages = {"com.ezmeal.cs", "com.ezmeal.common"})
public class CsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsApplication.class, args);
	}

}
