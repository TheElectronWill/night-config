package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharsWrapper;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.Utils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

/**
 * @author TheElectronWill
 * @see <a href="https://github.com/toml-lang/toml#user-content-offset-date-time">TOML specification - OffsetDateTime</a>
 * @see <a href="https://github.com/toml-lang/toml#user-content-local-date-time">TOML specification - LocalDateTime</a>
 * @see <a href="https://github.com/toml-lang/toml#user-content-local-date">TOML specification - LocalDate</a>
 * @see <a href="https://github.com/toml-lang/toml#user-content-local-time">TOML specification - LocalTime</a>
 */
final class TemporalParser {

	private static final char[] ALLOWED_DT_SEPARATORS = {'T', 't'};
	private static final char[] OFFSET_INDICATORS = {'Z', '+', '-'};

	/**
	 * Parses a Temporal value, to either a LocalTime, a LocalDate, a LocalDateTime or
	 * OffsetDateTime.
	 *
	 * @param chars the CharsWrapper to parse, <b>must be trimmed</b>
	 * @return a Temporal value
	 */
	static Temporal parse(CharsWrapper chars) {
		if (chars.get(2) == ':') {// LocalTime
			return parseTime(chars);
		}
		LocalDate date = parseDate(chars);
		if (chars.length() == 10) {// LocalDate
			return date;
		}
		char dateTimeSeparator = chars.get(10);
		if (!Utils.arrayContains(ALLOWED_DT_SEPARATORS, dateTimeSeparator)) {
			throw new ParsingException(
					"Invalid separator between date and time: '" + dateTimeSeparator + "'.");
		}
		CharsWrapper afterDate = chars.subView(11);
		int offsetIndicatorIndex = afterDate.indexOfFirst(OFFSET_INDICATORS);
		if (offsetIndicatorIndex == -1) {// LocalDateTime
			LocalTime time = parseTime(afterDate);
			return LocalDateTime.of(date, time);
		}
		LocalTime time = parseTime(afterDate.subView(0, offsetIndicatorIndex));
		ZoneOffset offset = ZoneOffset.of(afterDate.subView(offsetIndicatorIndex).toString());
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
				fractionChars = fractionChars.subView(0, 9);// truncates if too many digits
			}
			int value = Utils.parseInt(fractionChars, 10);
			int coeff = (int)Math.pow(10, 9 - fractionChars.length());
			nanos = value * coeff;
		} else {
			nanos = 0;
		}
		return LocalTime.of(hour, minutes, seconds, nanos);

	}

	private TemporalParser() {}
}