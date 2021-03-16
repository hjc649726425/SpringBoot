package com.hjc.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ToString
@Component
@ConfigurationProperties(prefix = "person")
public class TestProperties {
	private String name;
	private Integer age;
}
