package com.google.mihnita.msgfmt.datamodel;

public class Case {
	// oneof
	final public String alpha;
	final public Double numeric;

	public Case(String alpha) {
		this.alpha = alpha;
		this.numeric = null;
	}

	public Case(Double numeric) {
		this.alpha = null;
		this.numeric = numeric;
	}

	public Case(double numeric) {
		this.alpha = null;
		this.numeric = numeric;
	}
}
