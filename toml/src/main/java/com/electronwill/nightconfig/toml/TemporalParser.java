package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.serialization.CharsWrapper;
import com.electronwill.nightconfig.core.serialization.ParsingException;
import com.electronwill.nightconfig.core.serialization.Utils;
import java.time.*;
import java.time.temporal.Temporal;

/**
 * @author TheElectronWill
 */
public final class TemporalParser {

	private static final char[] ALLOWED_DT_SEPARATORS = {'T', 't', ' '};
	private static final char[] OFFSET_INDICATORS = {'Z', '+', '-'};

	static boolean shouldBeTemporal(CharsWrapper valueChars) {
		return valueChars.get(2) == ':' || (valueChars.get(4) == '-' && valueChars.get(7) == '-');
	}

	/**
	 * Parses a Temporal value, to either a LocalTime, a LocalDate, a LocalDateTime or OffsetDateTime.
	 *
	 * @param chars the CharsWrapper to parse, <b>must be trimmed</b>
	 * @return a Temporal value
	 */
	static Temporal parseTemporal(CharsWrapper chars) {//the CharsWrapper must be already trimmed
		if (chars.get(2) == ':') {// LocalTime
			return parseTime(chars);
		}

		LocalDate date = parseDate(chars);
		if (chars.length() == 10) {// LocalDate
			return date;
		}

		char dateTimeSeparator = chars.get(10);
		if (!Utils.arrayContains(ALLOWED_DT_SEPARATORS, dateTimeSeparator)) {
			throw new ParsingException("Invalid separator between date and time: '" + dateTimeSeparator +
				"'.");
		}
		int offsetIndicatorIndex = chars.subView(11).indexOfFirst(OFFSET_INDICATORS);
		CharsWrapper timeChars = chars.subView(11, offsetIndicatorIndex);
		LocalTime time = parseTime(timeChars);
		if (offsetIndicatorIndex == -1) {// LocalDateTime
			return LocalDateTime.of(date, time);
		}

		ZoneOffset offset = ZoneOffset.of(chars.subView(offsetIndicatorIndex).toString());
		return OffsetDateTime.of(date, time, offset);// OffsetDateTime
	}

	private static LocalDate parseDate(CharsWrapper chars) {
		CharsWrapper yearChars = chars.subView(0, 4);
		CharsWrapper monthChars = chars.subView(5, 7);
		CharsWrapper dayChars = chars.subView(8, 10);
		int year = Utils.parseInt(yearChars, 10);
		int month = Utils.parseInt(monthChars, 10);
		int day = Utils.parseInt(dayChars, 10);
		return LocalDate.of(year, month, day);
	}

	private static LocalTime parseTime(CharsWrapper chars) {
		CharsWrapper hourChars = chars.subView(0, 2);
		CharsWrapper minuteChars = chars.subView(3, 5);
		CharsWrapper secondChars = chars.subView(6, 8);
		int hour = Utils.parseInt(hourChars, 10);
		int minutes = Utils.parseInt(minuteChars, 10);
		int seconds = Utils.parseInt(secondChars, 10);
		int nanos;

		if (chars.length() > 8) {
			CharsWrapper fractionChars = new CharsWrapper(chars.subView(9));
			if (fractionChars.length() > 9) {
				fractionChars = fractionChars.subView(0, 9);//truncate if there are too many digits
			}
			nanos = Utils.parseInt(fractionChars, 10) * (int)Math.pow(10, fractionChars.length());
		} else {
			nanos = 0;
		}
		return LocalTime.of(hour, minutes, seconds, nanos);

	}

	private TemporalParser() {}
}
