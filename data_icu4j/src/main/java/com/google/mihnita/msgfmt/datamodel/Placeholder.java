package com.google.mihnita.msgfmt.datamodel;

import java.util.Map;

public class Placeholder {
	final public String name;
	final public String type;
	final public Map<String, String> flags;

	public Placeholder(String name) {
		this(name, null, null);
	}

	public Placeholder(String name, String type) {
		this(name, type, null);
	}

	public Placeholder(String name, String type, Map<String, String> knobs) {
		this.name = name;
		this.type = type;
		this.flags = knobs;
	}
}