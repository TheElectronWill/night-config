package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.Utils;
import com.electronwill.nightconfig.core.io.WriterOutput;
import com.electronwill.nightconfig.core.io.WritingException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A configurable HOCON writer.
 *
 * @author TheElectronWill
 */
public final class HoconWriter implements ConfigWriter<UnmodifiableConfig> {
	// --- Constant char arrays ---
	private static final char[] NULL_CHARS = {'n', 'u', 'l', 'l'};
	private static final char[] TRUE_CHARS = {'t', 'r', 'u', 'e'};
	private static final char[] FALSE_CHARS = {'f', 'a', 'l', 's', 'e'};
	private static final char[] EMPTY_OBJECT = {'{', '}'}, EMPTY_ARRAY = {'[', ']'};

	private static final char[] TO_ESCAPE = {'"', '\n', '\r', '\t', '\\'};
	private static final char[] ESCAPED = {'"', 'n', 'r', 't', '\\'};

	private static final char[] VALUE_SEPARATOR = {',', ' '};
	private static final char[] FORBIDDEN_IN_UNQUOTED = {'$', '"', '{', '}', '[', ']', ':', '=',
														 ',', '+', '#', '`', '^', '?', '!', '@',
														 '*', '&', '\\'};

	// --- Writer's settings ---
	private Predicate<UnmodifiableConfig> indentObjectElementsPredicate = c -> true;
	private Predicate<Collection<?>> indentArrayElementsPredicate = c -> true;
	private boolean newlineAfterObjectStart, newlineAfterArrayStart;
	private char[] newline = System.getProperty("line.separator").toCharArray();
	private char[] entrySeparator = {':', ' '};
	private char[] indent = {'\t'};
	private int currentIndentLevel;

	// --- Writer's methods ---
	@Override
	public void write(UnmodifiableConfig config, Writer writer) {
		currentIndentLevel = -1;
		writeObject(config, new WriterOutput(writer), true);
	}

	private void writeObject(UnmodifiableConfig config, CharacterOutput output, boolean root) {
		if (config.isEmpty()) {
			output.write(EMPTY_OBJECT);
			return;
		}
		if (!root) {
			output.write('{');// HOCON allows to omit the root braces
		}
		if (newlineAfterObjectStart) {
			output.write(newline);
		}
		final Iterator<Map.Entry<String, Object>> it = config.valueMap().entrySet().iterator();
		final boolean indentElements = indentObjectElementsPredicate.test(config);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}
		while (true) {
			final Map.Entry<String, Object> entry = it.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();
			if (indentElements) {
				writeIndent(output);// Indents the line
			}
			writeString(key, output);// key
			if (value instanceof UnmodifiableConfig) {
				output.write(' ');
			} else {
				output.write(entrySeparator);
				// HOCON allows to omit the separator if the value is a config
			}
			writeValue(value, output);// value
			if (indentElements) {
				output.write(newline);
			} else {
				output.write(',');
			}
			if (!it.hasNext()) break;
		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		if (!root) {
			output.write('}');// HOCON allows to omit the root braces
		}
	}

	private void writeValue(Object v, CharacterOutput output) {
		if (v == null) { output.write(NULL_CHARS); } else if (v instanceof String) {
			writeString((String)v, output);
		} else if (v instanceof Number) {
			output.write(v.toString());
		} else if (v instanceof UnmodifiableConfig) {
			writeObject((UnmodifiableConfig)v, output, false);
		} else if (v instanceof Collection) {
			writeArray((Collection<?>)v, output);
		} else if (v instanceof Boolean) { writeBoolean((boolean)v, output); } else {
			throw new WritingException("Unsupported value type: " + v.getClass());
		}
	}

	private void writeArray(Collection<?> collection, CharacterOutput output) {
		if (collection.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}
		output.write('[');// Opens the array
		if (newlineAfterObjectStart) {
			output.write(newline);
		}
		final Iterator<?> it = collection.iterator();
		final boolean indentElements = indentArrayElementsPredicate.test(collection);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}
		while (true) {
			Object value = it.next();
			if (indentElements) {
				writeIndent(output);
			}
			writeValue(value, output);
			if (it.hasNext()) {
				output.write(VALUE_SEPARATOR);
				if (indentElements) {
					output.write(newline);
				}
			} else {
				if (indentElements) {
					output.write(newline);
				}
				break;// Nothing else to write
			}
		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		output.write(']');// Closes the array
	}

	private void writeBoolean(boolean b, CharacterOutput output) {
		if (b) {
			output.write(TRUE_CHARS);
		} else {
			output.write(FALSE_CHARS);
		}
	}

	private void writeString(String s, CharacterOutput output) {
		if (canBeUnquoted(s)) {
			output.write(s);
			return;
		}
		output.write('"');
		final int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int escapeIndex = Utils.arrayIndexOf(TO_ESCAPE, c);
			if (escapeIndex == -1) {
				output.write(c);
			} else {// the character must be escaped
				char escaped = ESCAPED[escapeIndex];
				output.write('\\');
				output.write(escaped);
			}
		}
		output.write('"');
	}

	private boolean canBeUnquoted(CharSequence s) {
		final int length = s.length();
		for (int i = 0; i < length; i++) {
			if (Utils.arrayContains(FORBIDDEN_IN_UNQUOTED, s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private void increaseIndentLevel() {
		currentIndentLevel++;
	}

	private void decreaseIndentLevel() {
		currentIndentLevel--;
	}

	private void writeIndent(CharacterOutput output) {
		for (int i = 0; i < currentIndentLevel; i++) {
			output.write(indent);
		}
	}

	// --- Getters/Setters for the settings ---
	public Predicate<UnmodifiableConfig> getIndentObjectElementsPredicate() {
		return indentObjectElementsPredicate;
	}

	public void setIndentObjectElementsPredicate(Predicate<UnmodifiableConfig> indentObjectElementsPredicate) {
		this.indentObjectElementsPredicate = indentObjectElementsPredicate;
	}

	public Predicate<Collection<?>> getIndentArrayElementsPredicate() {
		return indentArrayElementsPredicate;
	}

	public void setIndentArrayElementsPredicate(
			Predicate<Collection<?>> indentArrayElementsPredicate) {
		this.indentArrayElementsPredicate = indentArrayElementsPredicate;
	}

	public boolean isNewlineAfterObjectStart() {
		return newlineAfterObjectStart;
	}

	public void setNewlineAfterObjectStart(boolean newlineAfterObjectStart) {
		this.newlineAfterObjectStart = newlineAfterObjectStart;
	}

	public boolean isNewlineAfterArrayStart() {
		return newlineAfterArrayStart;
	}

	public void setNewlineAfterArrayStart(boolean newlineAfterArrayStart) {
		this.newlineAfterArrayStart = newlineAfterArrayStart;
	}

	public char[] getIndent() {
		return indent;
	}

	public void setIndent(char[] indent) {
		this.indent = indent;
	}

	public void setIndent(String indent) {
		setIndent(indent.toCharArray());
	}

	public char[] getNewline() {
		return newline;
	}

	public void setNewline(char[] newline) {
		this.newline = newline;
	}

	public void setNewline(String newline) {
		setNewline(newline.toCharArray());
	}

	public char[] getEntrySeparator() {
		return entrySeparator;
	}

	public void setEntrySeparator(char[] entrySeparator) {
		this.entrySeparator = entrySeparator;
	}

	public void setEntrySeparator(String entrySeparator) {
		setEntrySeparator(entrySeparator.toCharArray());
	}
}