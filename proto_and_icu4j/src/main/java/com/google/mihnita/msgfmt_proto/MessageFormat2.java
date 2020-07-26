package com.google.mihnita.msgfmt_proto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import com.google.mihnita.msgfmt_proto.DateTimeFormatter2.DateTimeFormatterFactory;
import com.google.mihnita.msgfmt_proto.MessageFormat.Cases;
import com.google.mihnita.msgfmt_proto.MessageFormat.Cases.Case;
import com.google.mihnita.msgfmt_proto.MessageFormat.CasesOrBuilder;
import com.google.mihnita.msgfmt_proto.MessageFormat.MFDM;
import com.google.mihnita.msgfmt_proto.MessageFormat.Part;
import com.google.mihnita.msgfmt_proto.MessageFormat.Placeholder;
import com.google.mihnita.msgfmt_proto.MessageFormat.SelectorMessage;
import com.google.mihnita.msgfmt_proto.MessageFormat.SimpleMessage;
import com.google.mihnita.msgfmt_proto.MessageFormat.Switch;
import com.google.mihnita.msgfmt_proto.NumberFormatter2.NumberFormatterFactory;
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
		if (mfdm.hasSimpleMessage())
			return formatSimpleMessage(args);
		else
			return formatSelectorMessage(args);
	}

	public String formatSimpleMessage(Map<String, Object> args) {
		return formatSimpleMessageImpl(mfdm.getSimpleMessage(), args);
	}
	public String formatSimpleMessageImpl(SimpleMessage simpleMessage, Map<String, Object> args) {
		StringBuilder result = new StringBuilder();
		for (Part part : simpleMessage.getPartsList()) {
			if (part.hasPlainText())
				result.append(part.getPlainText().getValue());
			else if (part.hasPlaceholder()) {
				Placeholder ph = part.getPlaceholder();
				String name = ph.getName();
				String type = ph.getType();
				Object value = args.get(name); // the runtime value of the placeholder
				if (type == null || type.isEmpty()) {
					if (value instanceof Date)
						type = "date";
					else if (value instanceof Number)
						type = "number";
				}

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
		SelectorMessage m = mfdm.getSelectorMessage();
//		List<String> currentCases = new ArrayList<>();
		List<CaseBoth> ccase = new ArrayList<>();
		for (Switch sw : m.getSwitchList()) {
			String name = sw.getName();
			String type = sw.getType();
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
		if (m.getCasesCount() != m.getMessagesCount()) { // error
			throw new RuntimeException("TBD how we recover: selector.count != message.count");
		}
		for (int i = 0; i < m.getCasesCount(); i++) {
			Cases currentCase = m.getCases(i);
//			System.out.println("Compare: " + ccase + " to " + toString(currentCase));
			if (currentCase.getCaseCount() != ccase.size()) { // error
				throw new RuntimeException("TBD how we recover: selector.count != switch.count");
			}
			int score = 0;
			for (int j = 0; j < ccase.size(); j++) {
				Case c1 = currentCase.getCase(j);
				CaseBoth c2 = ccase.get(j);
				if (c2.nr != null && c1.getNumeric() == c2.nr) {
					score++;
				} else if (c1.getAlpha().equals(c2.str)) {
					score++;
				}
			}
			if (score == currentCase.getCaseCount()) {
				simpleM = m.getMessages(i);
			} else {
				score = 0;
				for (int j = 0; j < ccase.size(); j++) {
					Case c1 = currentCase.getCase(j);
					if ("other".equals(c1.getAlpha())) {
						score++;
					}
				}
				if (score == currentCase.getCaseCount()) {
					defaultSimpleM = m.getMessages(i);
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

	static String toString(CasesOrBuilder value) {
		StringJoiner joiner = new StringJoiner(", ", "[", "]");
		joiner.setEmptyValue("[]");
		for (Case c : value.getCaseList()) {
			if (c.getOneofCaseCase() == Case.OneofCaseCase.ALPHA)
				joiner.add("\"" + c.getAlpha() + "\"");
			else
				joiner.add("=" + c.getNumeric());
		}
		return joiner.toString();
	}

}
