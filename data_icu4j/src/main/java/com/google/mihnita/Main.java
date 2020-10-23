package com.google.mihnita;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.mihnita.msgfmt.MessageFormat2;
import com.google.mihnita.msgfmt.datamodel.Case;
import com.google.mihnita.msgfmt.datamodel.Cases;
import com.google.mihnita.msgfmt.datamodel.MFDM;
import com.google.mihnita.msgfmt.datamodel.Part;
import com.google.mihnita.msgfmt.datamodel.Placeholder;
import com.google.mihnita.msgfmt.datamodel.SelectorMessage;
import com.google.mihnita.msgfmt.datamodel.SimpleMessage;
import com.google.mihnita.msgfmt.datamodel.Switch;
import com.ibm.icu.text.MessageFormat;

public class Main {

	public static Part text(String text) {
		return new Part(text);
	}

	public static Part ph(String name) {
		return ph(name, null, null);
	}

	public static Part ph(String name, String type) {
		return ph(name, type, null);
	}

	public static Part ph(String name, String type, Map<String, String> knobs) {
		return new Part(new Placeholder(name, type, knobs));
	}

	public static boolean messageWithPlaceholdersNoSelect() {

		// This is the ICU pattern
		String icuMessage = "Hello {user}, in {locale} today is ''{today,time,::yMMMdjmszzzz}''.\n"
				+ "A big number would be {bigCount, number, currency}.\n"
				+ "An ordinal is {count, spellout}.\n";

		Map<String, String> timeKnobs = new HashMap<>();
		timeKnobs.put("skeleton", "yMMMdjmszzzz");
		Map<String, String> bigCountKnobs = new HashMap<>();
		bigCountKnobs.put("type", "currency");
		Part[] parts = {
				text("Hello "),
				ph("user"),
				text(", in "),
				ph("locale"),
				text(" today is '"),
				ph("today", "time", timeKnobs),
				text("'.\nA big number would be "),
				ph("bigCount", "number", bigCountKnobs),
				text(".\nAn ordinal is "),
				ph("count", "spellout"),
				text(".\n"),
		};
		SimpleMessage simpleMessage = SimpleMessage.of(parts);

		MFDM protoMessage = new MFDM(simpleMessage);

		// Prepare locale for formatters
		Locale locale = Locale.forLanguageTag("de-DE");

		// Prepare arguments for formatting
		Map<String, Object> msgArgs = new HashMap<>();
		msgArgs.put("user", "Mihai");
		msgArgs.put("count", 142);
		msgArgs.put("locale", locale.getDisplayName(locale));
		msgArgs.put("count1", 1);
		msgArgs.put("bigCount", 1234567890.97531);
		msgArgs.put("today", new Date());

		System.out.println("===== ICU Message:\n" + icuMessage);
		// Disable, this is too verbose. Keep it for debugging
		// System.out.println("Proto Message:\n" + protoMessage);

		// Format using the ICU MessageFormat
		MessageFormat mfIcu = new MessageFormat(icuMessage, locale);
		String resultIcu = mfIcu.format(msgArgs);
		System.out.println("# ICU result:\n" + resultIcu);

		// Format using the new Proto MessageFormat
		MessageFormat2 mfProto = new MessageFormat2(protoMessage, locale);
		String resultProto = mfProto.format(msgArgs);
		System.out.println("# Proto result:\n" + resultProto);

		return resultIcu.equals(resultProto);
	}

	public static boolean messageWithSeveralSelectors() {

		// This is the ICU pattern
		String icuMessage = ""
				+ "{host_gender, select,\n"
				+ "  female {{guest_count, plural,\n"
				+ "    =1    {{host_name} invited {guest_count} person to her party.}\n"
				+ "    other {{host_name} invited {guest_count} people to her party.}\n"
				+ "  }}\n"
				+ "  male   {{guest_count, plural,\n"
				+ "    =1    {{host_name} invited {guest_count} person to his party.}\n"
				+ "    other {{host_name} invited {guest_count} people to his party.}\n"
				+ "  }}\n"
				+ "  other  {{guest_count, plural,\n"
				+ "    =1    {{host_name} invited {guest_count} person to their party.}\n"
				+ "    other {{host_name} invited {guest_count} people to their party.}\n"
				+ "  }}\n"
				+ "}";

		// Here we build the proto message
		Part[] parts_female_singular = {
				ph("host_name"), text(" invited "), ph("guest_count"), text(" person to her party.")
		};
		Part[] parts_female_plural = {
				ph("host_name"), text(" invited "), ph("guest_count"), text(" people to her party.")
		};
		Part[] parts_male_singular = {
				ph("host_name"), text(" invited "), ph("guest_count"), text(" person to his party.")
		};
		Part[] parts_male_plural = {
				ph("host_name"), text(" invited "), ph("guest_count"), text(" people to his party.")
		};
		Part[] parts_other_singular = {
				ph("host_name"), text(" invited "), ph("guest_count"), text(" person to their party.")
		};
		Part[] parts_other_plural = {
				ph("host_name"), text(" invited "), ph("guest_count"), text(" people to their party.")
		};
		Cases cases_fs = new Cases().addCase(new Case("female")).addCase(new Case(1));
		Cases cases_fp = new Cases().addCase(new Case("female")).addCase(new Case("other"));
		Cases cases_ms = new Cases().addCase(new Case("male")).addCase(new Case(1));
		Cases cases_mp = new Cases().addCase(new Case("male")).addCase(new Case("other"));
		Cases cases_os = new Cases().addCase(new Case("other")).addCase(new Case(1));
		Cases cases_op = new Cases().addCase(new Case("other")).addCase(new Case("other"));
		SelectorMessage sm = new SelectorMessage();
		sm.switches.add(new Switch("host_gender", "select"));
		sm.switches.add(new Switch("guest_count", "plural"));
		sm.msgMap.put(cases_fs, SimpleMessage.of(parts_female_singular));
		sm.msgMap.put(cases_fp, SimpleMessage.of(parts_female_plural));
		sm.msgMap.put(cases_ms, SimpleMessage.of(parts_male_singular));
		sm.msgMap.put(cases_mp, SimpleMessage.of(parts_male_plural));
		sm.msgMap.put(cases_os, SimpleMessage.of(parts_other_singular));
		sm.msgMap.put(cases_op, SimpleMessage.of(parts_other_plural));

		MFDM protoMessage = new MFDM(sm);

		// Prepare locale for formatters
		Locale locale = Locale.forLanguageTag("de-DE");


		System.out.println("===== ICU Message:\n" + icuMessage + "\n");
		// Disable, this is too verbose. Keep it for debugging
		// System.out.println("Proto Message:\n" + protoMessage);

		// Format using the ICU MessageFormat
		MessageFormat mfIcu = new MessageFormat(icuMessage, locale);

		// Format using the new Proto MessageFormat
		MessageFormat2 mfProto = new MessageFormat2(protoMessage, locale);

		boolean success = true;
		success &= formatAndCompare(mfIcu, mfProto, "Maria", "female", 1);
		success &= formatAndCompare(mfIcu, mfProto, "Maria", "female", 12);
		success &= formatAndCompare(mfIcu, mfProto, "John",   "male",   1);
		success &= formatAndCompare(mfIcu, mfProto, "John",   "male",  12);
		success &= formatAndCompare(mfIcu, mfProto, "XyZvw", "other",   1);
		success &= formatAndCompare(mfIcu, mfProto, "XyZvw", "undef",  12);
		return success;
	}

	static boolean formatAndCompare(MessageFormat mfIcu, MessageFormat2 mfProto,
			String host_name, String host_gender, double guest_count) {
		// Prepare arguments for formatting
		Map<String, Object> msgArgs = new HashMap<>();
		msgArgs.put("host_name", host_name);
		msgArgs.put("host_gender", host_gender);
		msgArgs.put("guest_count", guest_count);

		String resultIcu = mfIcu.format(msgArgs);
		System.out.println("ICU result   : " + resultIcu);
		String resultProto = mfProto.format(msgArgs);
		System.out.println("Proto result : " + resultProto);
		System.out.println();

		return resultIcu.equals(resultProto);
	}

	public static void main(String[] args) {
		if (!messageWithPlaceholdersNoSelect())
			System.out.println("RESULT DIFFERENCES!");
		if (!messageWithSeveralSelectors())
			System.out.println("RESULT DIFFERENCES!");
	}
}
