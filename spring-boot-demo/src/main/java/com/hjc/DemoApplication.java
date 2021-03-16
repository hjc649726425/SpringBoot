package com.hjc;

import com.hjc.event.MyEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
		Object person = context.getBean("person");
		System.out.println(person);
		context.publishEvent(new MyEvent("test event 123"));
	}
}
