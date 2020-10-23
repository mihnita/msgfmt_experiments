package com.google.mihnita.msgfmt.datamodel;

import java.util.Map;

public class Placeholder {
	final public String name;
	final public String type;
	final public Map<String, String> knobs;
	final public Map<String, MFDM> selectors;

	public Placeholder(String name) {
		this(name, null, null, null);
	}

	public Placeholder(String name, String type) {
		this(name, type, null, null);
	}

	public Placeholder(String name, String type, Map<String, String> knobs) {
		this(name, type, knobs, null);
	}

	public Placeholder(String name, String type, Map<String, String> knobs, Map<String, MFDM> selectors) {
		this.name = name;
		this.type = type;
		this.knobs = knobs;
		this.selectors = selectors;
	}
}