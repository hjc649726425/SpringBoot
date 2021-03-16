package com.hjc.controller;

import com.hjc.model.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
public class TestController {

	@GetMapping("test")
	public String test(){
		return "test";
	}

	@GetMapping("person")
	public Person person(){
		Person p = new Person();
		p.setAge(19);
		p.setName("张三");
		return p;
	}

}
