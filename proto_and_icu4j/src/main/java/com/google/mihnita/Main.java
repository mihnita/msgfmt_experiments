package com.google.mihnita;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.mihnita.msgfmt_proto.MessageFormat.MFDM;
import com.google.mihnita.msgfmt_proto.MessageFormat.Part;
import com.google.mihnita.msgfmt_proto.MessageFormat.Placeholder;
import com.google.mihnita.msgfmt_proto.MessageFormat.PlainText;
import com.google.mihnita.msgfmt_proto.MessageFormat2;
import com.ibm.icu.text.MessageFormat;

public class Main {

	public static Part.Builder text(String text) {
		return Part.newBuilder().setPlainText(PlainText.newBuilder().setValue(text));
	}

	public static Part.Builder ph(String name) {
		return ph(name, null, null);
	}

	public static Part.Builder ph(String name, String type) {
		return ph(name, type, null);
	}

	public static Part.Builder ph(String name, String type, Map<String, String> knobs) {
		Placeholder.Builder ph = Placeholder.newBuilder();
		ph.setName(name);
		if (type != null) ph.setType(type);
		if (knobs != null) ph.putAllKnobs(knobs);
		return Part.newBuilder().setPlaceholder(ph);
	}

	public static void main(String[] args) {
		

		String icuPattern = "Hello {user}, in {locale} today is ''{today,time,::yMMMdjmszzzz}''.\n"
				+ "A big number would be {bigCount, number, currency}.\n"
				+ "An ordinal is {count, spellout}.\n";

		MFDM.Builder protoMessage = MFDM.newBuilder();
		protoMessage.addParts(text("Hello "));
		protoMessage.addParts(ph("user"));
		protoMessage.addParts(text(", in "));
		protoMessage.addParts(ph("locale"));
		protoMessage.addParts(text(" today is '"));
		protoMessage.addParts(ph("today", "time", ImmutableMap.of("skeleton", "yMMMdjmszzzz")));
		protoMessage.addParts(text("'.\nA big number would be "));
		protoMessage.addParts(ph("bigCount", "number", ImmutableMap.of("type", "currency")));
		protoMessage.addParts(text(".\nAn ordinal is "));
		protoMessage.addParts(ph("count", "spellout"));
		protoMessage.addParts(text(".\n"));
		
//		System.out.println(mfdm);

		Locale locale = Locale.forLanguageTag("de-DE");

		Map<String, Object> msgArgs = new HashMap<>();
		msgArgs.put("user", "Mihai");
		msgArgs.put("count", 142);
		msgArgs.put("locale", locale.getDisplayName(locale));
		msgArgs.put("count1", 1);
		msgArgs.put("bigCount", 1234567890.97531);
		msgArgs.put("today", new Date());

		System.out.println("ICU pattern:\n" + icuPattern);
		
		MessageFormat mf = new MessageFormat(icuPattern, locale);
		String result = mf.format(msgArgs);
		System.out.println("ICU result:\n" + result);

		MessageFormat2 mf2 = new MessageFormat2(protoMessage.build(), locale);
		String result2 = mf2.format(msgArgs);
		System.out.println("Proto result:\n" + result2);
		
		if (!result.equals(result2))
			System.out.println("RESULT DIFFERENCES!");
	}
}
