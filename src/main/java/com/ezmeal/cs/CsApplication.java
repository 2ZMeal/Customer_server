package com.ezmeal.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// Feign Client 적용
@EnableFeignClients
// 공통 모듈도 스캔 대상으로 추가
@SpringBootApplication(scanBasePackages = {"com.ezmeal.cs", "com.ezmeal.common"})
public class CsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsApplication.class, args);
	}

}
