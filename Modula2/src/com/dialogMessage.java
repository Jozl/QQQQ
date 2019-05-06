package com;

import java.util.ArrayList;
import java.util.List;

public class dialogMessage {
	private List<Object> messages;
	
	public dialogMessage() {
		this.messages = new ArrayList<>();
	}
	
	public void insert(Object object) {
		messages.add(object);
	}
}
