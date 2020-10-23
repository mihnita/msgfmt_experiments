package com.google.mihnita.msgfmt.datamodel;

import java.util.ArrayList;
import java.util.List;

public class Cases {
	final public List<Case> cases = new ArrayList<>();
	
	public Cases addCase(Case c) {
		cases.add(c);
		return this;
	}
}