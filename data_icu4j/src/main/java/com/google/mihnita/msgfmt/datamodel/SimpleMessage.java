package com.google.mihnita.msgfmt.datamodel;

import java.util.ArrayList;
import java.util.List;

public class SimpleMessage {
	final public List<Part> parts = new ArrayList<>();

	private SimpleMessage() {
	}

	public SimpleMessage(List<Part> list) {
		parts.addAll(list);
	}

	static public SimpleMessage of(Part ... list) {
		SimpleMessage result = new SimpleMessage();
		for (Part p : list) {
			result.parts.add(p);
		}
		return result;
	}

}