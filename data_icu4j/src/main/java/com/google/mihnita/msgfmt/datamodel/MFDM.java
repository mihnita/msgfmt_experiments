package com.google.mihnita.msgfmt.datamodel;

public class MFDM { // For `MessageFormat Data Model`, but need a better name
	// oneof
	final public SimpleMessage simpleMessage;
	final public SelectorMessage selectorMessage;

	public MFDM(SimpleMessage simpleMessage) {
		this.simpleMessage = simpleMessage;
		this.selectorMessage = null;
	}

	public MFDM(SelectorMessage selectorMessage) {
		this.simpleMessage = null;
		this.selectorMessage = selectorMessage;
	}
}
