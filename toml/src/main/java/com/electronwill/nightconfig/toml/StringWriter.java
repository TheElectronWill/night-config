package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.io.CharacterOutput;

/**
 * @author TheElectronWill
 */
final class StringWriter {
	private static final char[] ESCAPED_B = {'\\', 'b'},
								ESCAPED_F = {'\\', 'f'},
								ESCAPED_N = {'\\', 'n'},
								ESCAPED_R = {'\\', 'r'},
								ESCAPED_T = {'\\', 't'},
								ESCAPED_BACKSLASH = {'\\', '\\'},
								ESCAPED_QUOTE = {'\\', '\"'};

	static void writeBasic(CharSequence csq, CharacterOutput output) {
		output.write('\"');
		final int l = csq.length();
		for (int i = 0; i < l; i++) {
			writeBasicChar(csq.charAt(i), output);
		}
		output.write('\"');
	}

	static void writeLiteral(String str, CharacterOutput output) {
		output.write('\'');
		output.write(str);
		output.write('\'');
	}

	private static void writeBasicChar(char c, CharacterOutput output) {
		switch (c) {
			case '\\':
				output.write(ESCAPED_BACKSLASH);
				break;
			case '\"':
				output.write(ESCAPED_QUOTE);
				break;
			case '\b':
				output.write(ESCAPED_B);
				break;
			case '\f':
				output.write(ESCAPED_F);
				break;
			case '\n':
				output.write(ESCAPED_N);
				break;
			case '\r':
				output.write(ESCAPED_R);
				break;
			case '\t':
				output.write(ESCAPED_T);
				break;
			default:
				output.write(c);
		}
	}

	private StringWriter() {}
}