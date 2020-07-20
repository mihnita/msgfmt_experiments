package com.google.mihnita.msgfmt_proto;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

public class DateTimeFormatter2 implements IFormatter2 {

	private final DateFormat df;

	private DateTimeFormatter2(DateFormat df) {
		this.df = df;
	}

	@Override
	public String format(Object objToFormat) {
		return df == null ? Objects.toString(objToFormat) : df.format(objToFormat);
	}

	public static class DateTimeFormatterFactory implements IFormatterFactory {

		private static int styleToInt(String style) {
			if (style == null) return DateFormat.DEFAULT;
			switch (style) {
				case "short": return DateFormat.SHORT;
				case "medium": return DateFormat.MEDIUM;
				case "long": return DateFormat.LONG;
				case "full": return DateFormat.FULL;
				default: return DateFormat.DEFAULT;
			}
		}

		private static DateFormat dateFormatterInstance(Locale locale, String type, Map<String, String> knobs) {
			boolean isTime;
			if ("date".equals(type)) {
				isTime = false;
			} else if ("time".equals(type)) {
				isTime = true;
			} else {
				return null;
			}

			String skeleton = knobs != null ? knobs.get("skeleton") : null;
			if (skeleton != null) {
				return DateFormat.getInstanceForSkeleton(skeleton, locale);
			}

			String pattern = knobs != null ? knobs.get("pattern") : null;
			if (pattern != null) {
				return new SimpleDateFormat(pattern, locale);
			}

			String style = knobs != null ? knobs.get("style") : null;
			int numStyle = styleToInt(style);
			if (isTime) {
				return DateFormat.getTimeInstance(numStyle, locale);
			} else {
				return DateFormat.getDateInstance(numStyle, locale);
			}
		}

		@Override
		public IFormatter2 formatterInstance(Locale locale, String name, String type, Map<String, String> knobs) {
			return new DateTimeFormatter2(dateFormatterInstance(locale, type, knobs));
		}
	}
}
