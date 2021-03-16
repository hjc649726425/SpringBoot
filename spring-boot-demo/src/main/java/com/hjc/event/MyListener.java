package com.hjc.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyListener {

	@EventListener
	public void listen(MyEvent event){
		System.out.println(event.getSource());
	}

}
