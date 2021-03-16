package com.hjc.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableConfigurationProperties
public class MyAutoConfiguration {
	@Bean
	public Object object() {
		System.out.println("create object");
		return new Object();
	}
}
