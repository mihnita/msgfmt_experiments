package com.google.mihnita.msgfmt.datamodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SelectorMessage {
	final public List<Switch> switches = new ArrayList<>();
	final public LinkedHashMap<Cases, SimpleMessage> msgMap = new LinkedHashMap<>(); // the order matters
}
