

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.mihnita.msgfmt.MessageFormat2;
import com.google.mihnita.msgfmt.datamodel.Case;
import com.google.mihnita.msgfmt.datamodel.MFDM;
import com.google.mihnita.msgfmt.datamodel.Part;
import com.google.mihnita.msgfmt.datamodel.Placeholder;
import com.google.mihnita.msgfmt.datamodel.SelectorMessage;
import com.google.mihnita.msgfmt.datamodel.SimpleMessage;
import com.google.mihnita.msgfmt.datamodel.Switch;
import com.ibm.icu.text.MessageFormat;

@RunWith(JUnit4.class)
@SuppressWarnings("static-method")
public class MessageFormatTest {
	final private static Locale LOCALE = Locale.forLanguageTag("de-DE");

	// Testing proper

	@Test
	public void testMessageWithPlaceholdersDateSkeleton() {
		String icuMessage = "Hello {user}, in {locale} today is ''{today,time,::yMMMdjmszzzz}''.";

		Map<String, String> timeKnobs = new HashMap<>();
		timeKnobs.put("skeleton", "yMMMdjmszzzz");
		Part[] parts = {
				txt("Hello "),
				ph("user"),
				txt(", in "),
				ph("locale"),
				txt(" today is '"),
				ph("today", "time", timeKnobs),
				txt("'."),
		};

		Map<String, Object> msgArgs = Arguments.of(
				"user", "Mihai",
				"locale", LOCALE.getDisplayName(LOCALE),
				"today", new Date());

		formatAndCompare(LOCALE, icuMessage, parts, msgArgs);
	}

	@Test
	public void testMessageWithPlaceholdersNumberAsCurrency() {
		String icuMessage = "A big number would be {bigCount, number, currency}.";

		Map<String, String> bigCountKnobs = new HashMap<>();
		bigCountKnobs.put("type", "currency");
		Part[] parts = {
				txt("A big number would be "),
				ph("bigCount", "number", bigCountKnobs),
				txt("."),
		};

		Map<String, Object> msgArgs = Arguments.of("bigCount", 1234567890.97531);

		formatAndCompare(LOCALE, icuMessage, parts, msgArgs);
	}

	@Test
	public void testMessageWithPlaceholdersSpellout() {
		String icuMessage = "An ordinal is {count, spellout}.";

		Part[] parts = {
				txt("An ordinal is "),
				ph("count", "spellout"),
				txt("."),
		};

		Map<String, Object> msgArgs = Arguments.of("count", 142);

		formatAndCompare(LOCALE, icuMessage, parts, msgArgs);
	}

	@Test
	public void testMessageWithSeveralSelectors() {
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

		// Here we build the data model message
		Case[] cases_fs = { new Case("female"), new Case(1) };
		Part[] parts_female_singular = {
				ph("host_name"), txt(" invited "), ph("guest_count"), txt(" person to her party.")
		};
		Case[] cases_fp = { new Case("female"), new Case("other") };
		Part[] parts_female_plural = {
				ph("host_name"), txt(" invited "), ph("guest_count"), txt(" people to her party.")
		};
		Case[] cases_ms = { new Case("male"), new Case(1) };
		Part[] parts_male_singular = {
				ph("host_name"), txt(" invited "), ph("guest_count"), txt(" person to his party.")
		};
		Case[] cases_mp = { new Case("male"), new Case("other") };
		Part[] parts_male_plural = {
				ph("host_name"), txt(" invited "), ph("guest_count"), txt(" people to his party.")
		};
		Case[] cases_os = { new Case("other"), new Case(1) };
		Part[] parts_other_singular = {
				ph("host_name"), txt(" invited "), ph("guest_count"), txt(" person to their party.")
		};
		Case[] cases_op = { new Case("other"), new Case("other") };
		Part[] parts_other_plural = {
				ph("host_name"), txt(" invited "), ph("guest_count"), txt(" people to their party.")
		};

		SelectorMessage sm = new SelectorMessage();
		sm.switches.add(new Switch("host_gender", "select"));
		sm.switches.add(new Switch("guest_count", "plural"));
		sm.msgMap.put(Arrays.asList(cases_fs), SimpleMessage.of(parts_female_singular));
		sm.msgMap.put(Arrays.asList(cases_fp), SimpleMessage.of(parts_female_plural));
		sm.msgMap.put(Arrays.asList(cases_ms), SimpleMessage.of(parts_male_singular));
		sm.msgMap.put(Arrays.asList(cases_mp), SimpleMessage.of(parts_male_plural));
		sm.msgMap.put(Arrays.asList(cases_os), SimpleMessage.of(parts_other_singular));
		sm.msgMap.put(Arrays.asList(cases_op), SimpleMessage.of(parts_other_plural));

		MFDM dataModelMessage = new MFDM(sm);

		System.out.println("===== ICU Message:\n" + icuMessage + "\n");

		// Format using the ICU MessageFormat
		MessageFormat mfIcu = new MessageFormat(icuMessage, LOCALE);

		// Format using the new DataModel MessageFormat
		MessageFormat2 mfDataModel = new MessageFormat2(dataModelMessage, LOCALE);

		formatAndCompareMultiSelect(mfIcu, mfDataModel, "Maria", "female", 1);
		formatAndCompareMultiSelect(mfIcu, mfDataModel, "Maria", "female", 12);
		formatAndCompareMultiSelect(mfIcu, mfDataModel, "John",   "male",   1);
		formatAndCompareMultiSelect(mfIcu, mfDataModel, "John",   "male",  12);
		formatAndCompareMultiSelect(mfIcu, mfDataModel, "XyZvw", "other",   1);
		formatAndCompareMultiSelect(mfIcu, mfDataModel, "XyZvw", "undef",  12);
	}

	// Helper methods to format and compare

	static void formatAndCompareMultiSelect(MessageFormat mfIcu, MessageFormat2 mfDataModel,
			String host_name, String host_gender, double guest_count) {

		Map<String, Object> msgArgs = Arguments.of(
				"host_name", host_name,
				"host_gender", host_gender,
				"guest_count", guest_count);

		formatAndCompare(mfIcu, mfDataModel, msgArgs);
	}

	static void formatAndCompare(Locale locale, String icuMessage, Part[] parts, Map<String, Object> msgArgs) {
		MessageFormat mfIcu = new MessageFormat(icuMessage, locale);

		MFDM dataModelMessage = new MFDM(SimpleMessage.of(parts));
		MessageFormat2 mfDataModel = new MessageFormat2(dataModelMessage, locale);

		System.out.println("===== ICU Message: " + mfIcu.toPattern() + "\n");
		formatAndCompare(mfIcu, mfDataModel, msgArgs);
	}

	static void formatAndCompare(MessageFormat mfIcu, MessageFormat2 mfDataModel, Map<String, Object> msgArgs) {
		// Format using the ICU MessageFormat
		String resultIcu = mfIcu.format(msgArgs);
		System.out.println("ICU result       : " + resultIcu);

		// Format using the new DataModel MessageFormat
		String resultDataModel = mfDataModel.format(msgArgs);
		System.out.println("DataModel result : " + resultDataModel);

		System.out.println();

		assertEquals(resultIcu, resultDataModel);
	}

	// Helper methods to make data model construction easier

	static Part txt(String text) {
		return new Part(text);
	}

	static Part ph(String name) {
		return ph(name, null, null);
	}

	static Part ph(String name, String type) {
		return ph(name, type, null);
	}

	static Part ph(String name, String type, Map<String, String> knobs) {
		return new Part(new Placeholder(name, type, knobs));
	}

	static class Arguments {
		static Map<String, Object> of(Object ... obj) {
			assertNotNull(obj);
			assertEquals(0, obj.length % 2);

			Map<String, Object> result = new HashMap<>();
			for (int i = 0; i < obj.length; i += 2) {
				assertTrue(obj[i] instanceof String);
				String key = (String) obj[i];
				result.put(key, obj[i + 1]);
			}
			return result;
		}
	}
}
