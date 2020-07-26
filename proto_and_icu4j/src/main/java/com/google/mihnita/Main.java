package com.google.mihnita;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.mihnita.msgfmt_proto.MessageFormat.Cases;
import com.google.mihnita.msgfmt_proto.MessageFormat.MFDM;
import com.google.mihnita.msgfmt_proto.MessageFormat.Part;
import com.google.mihnita.msgfmt_proto.MessageFormat.Placeholder;
import com.google.mihnita.msgfmt_proto.MessageFormat.PlainText;
import com.google.mihnita.msgfmt_proto.MessageFormat.SelectorMessage;
import com.google.mihnita.msgfmt_proto.MessageFormat.SimpleMessage;
import com.google.mihnita.msgfmt_proto.MessageFormat.Switch;
import com.google.mihnita.msgfmt_proto.MessageFormat.Cases.Case;
import com.google.mihnita.msgfmt_proto.MessageFormat2;
import com.ibm.icu.text.MessageFormat;

public class Main {

	public static Part text(String text) {
		return Part.newBuilder().setPlainText(PlainText.newBuilder().setValue(text)).build();
	}

	public static Part ph(String name) {
		return ph(name, null, null);
	}

	public static Part ph(String name, String type) {
		return ph(name, type, null);
	}

	public static Part ph(String name, String type, Map<String, String> knobs) {
		Placeholder.Builder ph = Placeholder.newBuilder();
		ph.setName(name);
		if (type != null) ph.setType(type);
		if (knobs != null) ph.putAllKnobs(knobs);
		return Part.newBuilder().setPlaceholder(ph).build();
	}

	public static boolean messageWithPlaceholdersNoSelect() {

		// This is the ICU pattern
		String icuMessage = "Hello {user}, in {locale} today is ''{today,time,::yMMMdjmszzzz}''.\n"
				+ "A big number would be {bigCount, number, currency}.\n"
				+ "An ordinal is {count, spellout}.\n";

		// Here we build the proto message
		SimpleMessage simpleMessage = SimpleMessage.newBuilder()
			.addParts(text("Hello "))
			.addParts(ph("user"))
			.addParts(text(", in "))
			.addParts(ph("locale"))
			.addParts(text(" today is '"))
			.addParts(ph("today", "time", ImmutableMap.of("skeleton", "yMMMdjmszzzz")))
			.addParts(text("'.\nA big number would be "))
			.addParts(ph("bigCount", "number", ImmutableMap.of("type", "currency")))
			.addParts(text(".\nAn ordinal is "))
			.addParts(ph("count", "spellout"))
			.addParts(text(".\n"))
			.build();

		MFDM protoMessage = MFDM.newBuilder().setSimpleMessage(simpleMessage).build();

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
		Cases.Builder cases_fs = Cases.newBuilder()
				.addCase(Case.newBuilder().setAlpha("female")).addCase(Case.newBuilder().setNumeric(1));
		Cases.Builder cases_fp = Cases.newBuilder()
				.addCase(Case.newBuilder().setAlpha("female")).addCase(Case.newBuilder().setAlpha("other"));
		Cases.Builder cases_ms = Cases.newBuilder()
				.addCase(Case.newBuilder().setAlpha("male")).addCase(Case.newBuilder().setNumeric(1));
		Cases.Builder cases_mp = Cases.newBuilder()
				.addCase(Case.newBuilder().setAlpha("male")).addCase(Case.newBuilder().setAlpha("other"));
		Cases.Builder cases_os = Cases.newBuilder()
				.addCase(Case.newBuilder().setAlpha("other")).addCase(Case.newBuilder().setNumeric(1));
		Cases.Builder cases_op = Cases.newBuilder()
				.addCase(Case.newBuilder().setAlpha("other")).addCase(Case.newBuilder().setAlpha("other"));
		SelectorMessage sm = SelectorMessage.newBuilder()
				.addSwitch(Switch.newBuilder().setName("host_gender").setType("select"))
				.addSwitch(Switch.newBuilder().setName("guest_count").setType("plural"))
				.addCases(cases_fs)
				.addMessages(SimpleMessage.newBuilder().addAllParts(Arrays.asList(parts_female_singular)))
				.addCases(cases_fp)
				.addMessages(SimpleMessage.newBuilder().addAllParts(Arrays.asList(parts_female_plural)))
				.addCases(cases_ms)
				.addMessages(SimpleMessage.newBuilder().addAllParts(Arrays.asList(parts_male_singular)))
				.addCases(cases_mp)
				.addMessages(SimpleMessage.newBuilder().addAllParts(Arrays.asList(parts_male_plural)))
				.addCases(cases_os)
				.addMessages(SimpleMessage.newBuilder().addAllParts(Arrays.asList(parts_other_singular)))
				.addCases(cases_op)
				.addMessages(SimpleMessage.newBuilder().addAllParts(Arrays.asList(parts_other_plural)))
				.build();

		MFDM protoMessage = MFDM.newBuilder().setSelectorMessage(sm).build();

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
