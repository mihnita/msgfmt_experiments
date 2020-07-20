package com.google.mihnita.msgfmt_proto;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.mihnita.msgfmt_proto.DateTimeFormatter2.DateTimeFormatterFactory;
import com.google.mihnita.msgfmt_proto.MessageFormat.MFDM;
import com.google.mihnita.msgfmt_proto.MessageFormat.Part;
import com.google.mihnita.msgfmt_proto.MessageFormat.Placeholder;
import com.google.mihnita.msgfmt_proto.NumberFormatter2.NumberFormatterFactory;

public class MessageFormat2 {
	private final MFDM mfdm;
	private final Locale locale;
	
	private static final Map<String, IFormatterFactory> DEFAULT_KNOWN_FORMATTERS = new HashMap<>();
	static {
		DateTimeFormatterFactory dff = new DateTimeFormatter2.DateTimeFormatterFactory();
		DEFAULT_KNOWN_FORMATTERS.put("date", dff);
		DEFAULT_KNOWN_FORMATTERS.put("time", dff);
		NumberFormatterFactory nff = new NumberFormatter2.NumberFormatterFactory();
		DEFAULT_KNOWN_FORMATTERS.put("number", nff);
		DEFAULT_KNOWN_FORMATTERS.put("spellout", nff);
		DEFAULT_KNOWN_FORMATTERS.put("ordinal", nff);
		DEFAULT_KNOWN_FORMATTERS.put("duration", nff);
	}

	public MessageFormat2(MFDM mfdm, Locale locale) {
		this.mfdm = mfdm;
		this.locale = locale;
	}

	public String format(Map<String, Object> args) {
		StringBuilder result = new StringBuilder();
		for (Part part : mfdm.getPartsList()) {
			if (part.hasPlainText())
				result.append(part.getPlainText().getValue());
			else if (part.hasPlaceholder()) {
				Placeholder ph = part.getPlaceholder();
				String name = ph.getName();
				String type = ph.getType();
				Object value = args.get(name); // the runtime value of the placeholder

				IFormatterFactory ffactory = DEFAULT_KNOWN_FORMATTERS.get(type);
				if (ffactory != null) {
					IFormatter2 formatter = ffactory.formatterInstance(locale, name, type, ph.getKnobsMap());
					result.append(formatter.format(value));
				} else {
					result.append(value);
				}
			}
		}
		return result.toString();
	}
}
