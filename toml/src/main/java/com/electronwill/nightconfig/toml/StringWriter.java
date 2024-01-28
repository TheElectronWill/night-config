package com.electronwill.nightconfig.toml;

import static com.electronwill.nightconfig.core.utils.StringUtils.splitLines;

import java.util.Iterator;

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

	static void writeBasic(String str, CharacterOutput output) {
		output.write('\"');
		for (char c : str.toCharArray()) {
			writeBasicChar(c, output);
		}
		output.write('\"');
	}

	static void writeBasicMultiline(String str, CharacterOutput output, TomlWriter writer) {
		output.write("\"\"\""); // """\
		for (Iterator<String> it = splitLines(str).iterator(); it.hasNext();) {
			String line = it.next();
			writer.writeNewline(output);
			char[] chars = line.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				switch (c) {
					case '\"': {
						if ((i+1 == chars.length && !it.hasNext()) || ((i+1 < chars.length && chars[i+1] == '\"') && (i+2 < chars.length && chars[i+2] == '\"') && (i+3 < chars.length))) {
							output.write(ESCAPED_QUOTE);
						} else {
							output.write(c);
						}
						break;
					}
					case '\b':
						output.write(ESCAPED_B);
						break;
					case '\f':
						output.write(ESCAPED_F);
						break;
					case '\\':
						output.write(ESCAPED_BACKSLASH);
						break;
					default:
						output.write(c);
						break;
				}
			}
		}
		output.write("\"\"\"");
	}

	static void writeLiteral(String str, CharacterOutput output) {
		output.write('\'');
		output.write(str);
		output.write('\'');
	}

	static void writeLiteralMultiline(String str, CharacterOutput output) {
		output.write("'''\n");
		output.write(str);
		output.write("''''");
	}

	private static void writeBasicMultilineChar(char c, CharacterOutput output) {

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