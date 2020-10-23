package com.google.mihnita.msgfmt.datamodel;

public class Switch {
	final public String name; // the variable to switch on
	final public String type; // plural, ordinal, gender, select, ...

	public Switch(String name, String type) {
		this.name = name;
		this.type = type;
	}
}