package com.google.mihnita.msgfmt.datamodel;

public class Part {
	// oneof
	final public String plainText;
	final public Placeholder placeholder;

	public Part(String plainText) {
		this.plainText = plainText;
		this.placeholder = null;
	}

	public Part(Placeholder placeholder) {
		this.plainText = null;
		this.placeholder = placeholder;
	}
}