package com.google.mihnita.msgfmt;

import java.text.Format;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;

public class NumberFormatter2 implements IFormatter2 {

	private final Format nf;

	NumberFormatter2(Format nf) {
		this.nf = nf;
	}

	@Override
	public String format(Object objToFormat) {
		return nf == null ? Objects.toString(objToFormat) : nf.format(objToFormat).toString();
	}

	public static class NumberFormatterFactory implements IFormatterFactory {

		private static Format numberFormatterInstance(Locale locale, String type, Map<String, String> knobs) {
			int rbnfStyle = -1;
			switch (type) {
				case "spellout": rbnfStyle = RuleBasedNumberFormat.SPELLOUT; break;
				case "ordinal": rbnfStyle = RuleBasedNumberFormat.ORDINAL; break;
				case "duration": rbnfStyle = RuleBasedNumberFormat.DURATION; break;
				/* Nothing. The rbnfStyle will stay -1, and we will go on to handle number */
				case "number": break;
				default: return null;
			}

			if (rbnfStyle != -1) { // spellout, ordinal, or duration
				RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(locale, rbnfStyle);
				String style = knobs != null ? knobs.get("style") : null;
				if (style != null)
					rbnf.setDefaultRuleSet(style);
				return rbnf;
			}

			// was `number`
			String skeleton = knobs != null ? knobs.get("skeleton") : null;
			if (skeleton != null)
				return NumberFormatter.forSkeleton(skeleton).locale(locale).toFormat();

			String pattern = knobs != null ? knobs.get("pattern") : null;
			if (pattern != null) {
				return new DecimalFormat(pattern, new DecimalFormatSymbols(locale));
			}

			String subtype = knobs != null ? knobs.get("type") : null;
			if ("currency".equals(subtype)) {
				return NumberFormat.getCurrencyInstance(locale);
			}
			if ("percent".equals(subtype)) {
				return NumberFormat.getPercentInstance(locale);
			}
			if ("integer".equals(subtype)) {
				return NumberFormat.getIntegerInstance(locale);
			}

			return NumberFormat.getInstance(locale);
		}

		@Override
		public IFormatter2 formatterInstance(Locale locale, String name, String type, Map<String, String> knobs) {
			return new NumberFormatter2(numberFormatterInstance(locale, type, knobs));
		}
	}
}
