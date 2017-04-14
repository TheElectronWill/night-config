package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.NewlineStyle;
import com.electronwill.nightconfig.core.io.Utils;
import com.electronwill.nightconfig.core.io.WriterOutput;
import com.electronwill.nightconfig.core.io.WritingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.electronwill.nightconfig.json.MinimalJsonWriter.*;

/**
 * A configurable <a href="http://www.json.org/">JSON</a> writer.
 *
 * @author TheElectronWill
 */
public final class FancyJsonWriter implements ConfigWriter<UnmodifiableConfig> {
	private static final char[] ENTRY_SEPARATOR = {':', ' '}, VALUE_SEPARATOR = {',', ' '};

	// --- Writer's settings ---
	private Predicate<UnmodifiableConfig> indentObjectElementsPredicate = c -> true;
	private Predicate<Collection<?>> indentArrayElementsPredicate = c -> true;
	private boolean newlineAfterObjectStart;
	private char[] newline = NewlineStyle.system().chars;
	private char[] indent = IndentStyle.TABS.chars;
	private int currentIndentLevel;

	// --- Writer's methods --
	@Override
	public void write(UnmodifiableConfig config, Writer writer) {
		currentIndentLevel = 0;
		writeObject(config, new WriterOutput(writer));
	}

	private void writeObject(UnmodifiableConfig config, CharacterOutput output) {
		if (config.isEmpty()) {
			output.write(EMPTY_OBJECT);
			return;
		}
		Iterator<Map.Entry<String, Object>> it = config.valueMap().entrySet().iterator();
		output.write('{');
		if (newlineAfterObjectStart) {
			output.write(newline);
		}
		boolean indentElements = indentObjectElementsPredicate.test(config);
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
			output.write(ENTRY_SEPARATOR);// separator
			writeValue(value, output);// value
			if (it.hasNext()) {
				output.write(',');
				if (indentElements) {
					output.write(newline);
				}
			} else {
				if (indentElements) {
					output.write(newline);
				}
				break;
			}
		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		output.write('}');
	}

	/**
	 * Writes some value in the JSON format.
	 *
	 * @param v      the value to write
	 * @param output the output to write to
	 */
	private void writeValue(Object v, CharacterOutput output) {
		if (v == null) {
			output.write(NULL_CHARS);
		} else if (v instanceof CharSequence) {
			writeString((CharSequence)v, output);
		} else if (v instanceof Number) {
			output.write(v.toString());
		} else if (v instanceof UnmodifiableConfig) {
			writeObject((UnmodifiableConfig)v, output);
		} else if (v instanceof Collection) {
			writeArray((Collection<?>)v, output);
		} else if (v instanceof Boolean) {
			writeBoolean((boolean)v, output);
		} else if (v instanceof Object[]) {
			writeArray(Arrays.asList((Object[])v), output);
		} else if (v.getClass().isArray()) {
			writeArray(v, output);
		} else {
			throw new WritingException("Unsupported value type: " + v.getClass());
		}
	}

	/**
	 * Writes a Collection as a JSON array.
	 *
	 * @param collection the Collection to write
	 * @param output     the output to write to
	 */
	private void writeArray(Collection<?> collection, CharacterOutput output) {
		if (collection.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}
		Iterator<?> it = collection.iterator();
		output.write('[');
		if (newlineAfterObjectStart) {
			output.write(newline);
		}
		boolean indentElements = indentArrayElementsPredicate.test(collection);
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
				break;
			}
		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		output.write(']');
	}

	private void writeArray(Object array, CharacterOutput output) {
		// Converts the array into a List:
		int length = Array.getLength(array);
		List<Object> list = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			list.add(Array.get(array, i));
		}
		// Then, writes the list as a JSON array:
		writeArray(list, output);
	}

	/**
	 * Writes a boolean in the JSON format.
	 *
	 * @param b      the boolean to write
	 * @param output the output to write to
	 */
	private void writeBoolean(boolean b, CharacterOutput output) {
		if (b) {
			output.write(TRUE_CHARS);
		} else {
			output.write(FALSE_CHARS);
		}
	}

	/**
	 * Writes a String in the JSON format.
	 *
	 * @param s      the String to write
	 * @param output the output to write to
	 */
	private void writeString(CharSequence s, CharacterOutput output) {
		output.write('"');
		final int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int escapeIndex = Utils.arrayIndexOf(TO_ESCAPE, c);
			if (escapeIndex == -1) {
				output.write(c);
			} else {// This character must be escaped
				char escaped = ESCAPED[escapeIndex];
				output.write('\\');
				output.write(escaped);
			}
		}
		output.write('"');
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

	// --- Settings ---
	public FancyJsonWriter setIndentObjectElementsPredicate(
		Predicate<UnmodifiableConfig> indentObjectElementsPredicate) {
		this.indentObjectElementsPredicate = indentObjectElementsPredicate;
		return this;
	}

	public FancyJsonWriter setIndentArrayElementsPredicate(
		Predicate<Collection<?>> indentArrayElementsPredicate) {
		this.indentArrayElementsPredicate = indentArrayElementsPredicate;
		return this;
	}

	public FancyJsonWriter setNewlineAfterObjectStart(boolean newlineAfterObjectStart) {
		this.newlineAfterObjectStart = newlineAfterObjectStart;
		return this;
	}

	public FancyJsonWriter setIndent(IndentStyle indentStyle) {
		this.indent = indentStyle.chars;
		return this;
	}

	public FancyJsonWriter setIndent(String indent) {
		this.indent = indent.toCharArray();
		return this;
	}

	public FancyJsonWriter setNewline(NewlineStyle newlineStyle) {
		this.newline = newlineStyle.chars;
		return this;
	}

	public FancyJsonWriter setNewline(String newlineString) {
		this.newline = newlineString.toCharArray();
		return this;
	}
}