package com.hjc.config;

import com.hjc.model.Person;
import com.hjc.model.TestProperties;
import com.hjc.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "person", matchIfMissing = false, value = {"name", "age"})
@AutoConfigureBefore(MyAutoConfiguration.class)
public class Appconfig {

	@Autowired
	TestProperties properties;

	@Bean
	@ConditionalOnClass(value = {Person.class})
	public Person person(){
		Person person = new Person();
		person.setName(properties.getName());
		person.setAge(properties.getAge());
		return person;
	}
}
