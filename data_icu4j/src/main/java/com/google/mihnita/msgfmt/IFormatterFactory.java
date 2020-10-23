package com.google.mihnita.msgfmt;

import java.util.Locale;
import java.util.Map;

public interface IFormatterFactory {
	IFormatter2 formatterInstance(Locale locale, String name, String type, Map<String, String> knobs);
}
