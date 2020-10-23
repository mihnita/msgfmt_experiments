package com.google.mihnita.msgfmt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.google.mihnita.msgfmt.DateTimeFormatter2.DateTimeFormatterFactory;
import com.google.mihnita.msgfmt.datamodel.Case;
import com.google.mihnita.msgfmt.datamodel.MFDM;
import com.google.mihnita.msgfmt.datamodel.Part;
import com.google.mihnita.msgfmt.datamodel.Placeholder;
import com.google.mihnita.msgfmt.datamodel.SelectorMessage;
import com.google.mihnita.msgfmt.datamodel.SimpleMessage;
import com.google.mihnita.msgfmt.datamodel.Switch;
import com.google.mihnita.msgfmt.NumberFormatter2.NumberFormatterFactory;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.text.PluralRules.PluralType;

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
//		DEFAULT_KNOWN_FORMATTERS.put("duration", nff);
	}

	public MessageFormat2(MFDM mfdm, Locale locale) {
		this.mfdm = mfdm;
		this.locale = locale;
	}

	public String format(Map<String, Object> args) {
		if (mfdm.simpleMessage != null)
			return formatSimpleMessage(args);
		else
			return formatSelectorMessage(args);
	}

	public String formatSimpleMessage(Map<String, Object> args) {
		return formatSimpleMessageImpl(mfdm.simpleMessage, args);
	}
	public String formatSimpleMessageImpl(SimpleMessage simpleMessage, Map<String, Object> args) {
		StringBuilder result = new StringBuilder();
		for (Part part : simpleMessage.parts) {
			if (part.plainText != null)
				result.append(part.plainText);
			else if (part.placeholder != null) {
				Placeholder ph = part.placeholder;
				String name = ph.name;
				String type = ph.type;
				Object value = args.get(name); // the runtime value of the placeholder
				if (type == null || type.isEmpty()) {
					if (value instanceof Date)
						type = "date";
					else if (value instanceof Number)
						type = "number";
				}

				IFormatterFactory ffactory = DEFAULT_KNOWN_FORMATTERS.get(type);
				if (ffactory != null) {
					IFormatter2 formatter = ffactory.formatterInstance(locale, name, type, ph.flags);
					result.append(formatter.format(value));
				} else {
					result.append(value);
				}
			}
		}
		return result.toString();
	}

	private static class CaseBoth {
		final String str;
		final Double nr;
		CaseBoth(String str, Double nr) {
			this.str = str;
			this.nr = nr;
		}
		@Override
		public String toString() {
			return "[\"" + str + "\", =" + nr + "]";
		}
	}

	public String formatSelectorMessage(Map<String, Object> args) {
		SelectorMessage m = mfdm.selectorMessage;
//		List<String> currentCases = new ArrayList<>();
		List<CaseBoth> ccase = new ArrayList<>();
		for (Switch sw : m.switches) {
			String name = sw.name;
			String type = sw.type;
			Object nameValue = args.get(name);
//			System.out.println(" switch: " + name + ":" + type + " => " + nameValue);
			switch (type) {
				case "plural":
				case "ordinal":
					PluralRules pr = PluralRules.forLocale(locale,
							"ordinal".equals(type) ? PluralType.ORDINAL : PluralType.CARDINAL);
					String sel;
					if (nameValue instanceof Number) {
						double dblValue = ((Number) nameValue).doubleValue();
						sel = pr.select(dblValue);
						ccase.add(new CaseBoth(sel, dblValue));
					} else {
						sel = "other";
						ccase.add(new CaseBoth(sel, null));
					}
//					currentCases.add(sel);
					break;
				case "gender": // fall-through
					// we should in fact check for the known values
				case "select": // fall-through
				default: // treat all unknown selectors as "select"
					ccase.add(new CaseBoth(nameValue.toString(), null));
			}
		}

//		System.out.println(ccase);
		SimpleMessage simpleM = null;
		SimpleMessage defaultSimpleM = null;
		for ( Entry<List<Case>, SimpleMessage> e : m.msgMap.entrySet()) {
			List<Case> currentCase = e.getKey();
//			System.out.println("Compare: " + ccase + " to " + toString(currentCase));
			if (currentCase.size() != ccase.size()) { // error
				throw new RuntimeException("TBD how we recover: selector.count != switch.count");
			}
			int score = 0;
			for (int j = 0; j < ccase.size(); j++) {
				Case c1 = currentCase.get(j);
				CaseBoth c2 = ccase.get(j);
				if (c2.nr != null && c2.nr.equals(c1.numeric)) {
					score++;
				} else if (c1.alpha != null && c1.alpha.equals(c2.str)) {
					score++;
				}
			}
			if (score == currentCase.size()) {
				simpleM = e.getValue();
			} else {
				score = 0;
				for (int j = 0; j < ccase.size(); j++) {
					Case c1 = currentCase.get(j);
					if ("other".equals(c1.alpha)) {
						score++;
					}
				}
				if (score == currentCase.size()) {
					defaultSimpleM = e.getValue();
				}
			}
		}
//		System.out.println(simpleM);
		if (simpleM == null)
			simpleM = defaultSimpleM;
		if (simpleM != null)
			return formatSimpleMessageImpl(simpleM, args);
		throw new RuntimeException("TBD how we recover: simple message not found");
	}

	static String toString(List<Case> cases) {
		StringJoiner joiner = new StringJoiner(", ", "[", "]");
		joiner.setEmptyValue("[]");
		for (Case c : cases) {
			if (c.alpha != null)
				joiner.add("\"" + c.alpha + "\"");
			else
				joiner.add("=" + c.numeric);
		}
		return joiner.toString();
	}

}
