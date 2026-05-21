package com.tosin.xpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XpayApplication {

	public static void main(String[] args) {
		SpringApplication.run(XpayApplication.class, args);
	}

}
