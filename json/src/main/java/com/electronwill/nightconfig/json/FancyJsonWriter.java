package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.*;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

import static com.electronwill.nightconfig.json.MinimalJsonWriter.*;

/**
 * A configurable JSON writer.
 *
 * @author TheElectronWill
 */
public final class FancyJsonWriter implements ConfigWriter<Config> {
	private static final char[] ENTRY_SEPARATOR = {':', ' '}, VALUE_SEPARATOR = {',', ' '};

	private Predicate<Config> indentObjectElementsPredicate = c -> true;
	private Predicate<Collection> indentArrayElementsPredicate = c -> true;
	private boolean newlineAfterObjectStart = false, newlineAfterArrayStart = false;
	private char[] indent = {'\t'};
	private char[] newline = System.getProperty("line.separator").toCharArray();
	private int currentIndentLevel;

	@Override
	public void writeConfig(Config config, Writer writer) throws IOException {
		currentIndentLevel = 0;
		writeObject(config, new WriterOutput(writer));
	}

	private void writeObject(Config config, CharacterOutput output) {
		if (config.isEmpty()) {
			output.write(EMPTY_OBJECT);
			return;
		}
		Iterator<Map.Entry<String, Object>> it = config.asMap().entrySet().iterator();
		output.write('{');//open object
		if (newlineAfterObjectStart) output.write(newline);
		boolean indentElements = indentObjectElementsPredicate.test(config);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}
		while (true) {
			final Map.Entry<String, Object> entry = it.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();

			if (indentElements) writeIndent(output);//Indents the line
			writeString(key, output);//key
			output.write(ENTRY_SEPARATOR);//separator
			writeValue(value, output);//value
			if (it.hasNext()) {
				output.write(',');
				if (indentElements) output.write(newline);
			} else {
				if (indentElements) output.write(newline);
				break;
			}
		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		output.write('}');//close object
	}

	/**
	 * Writes some value in the JSON format.
	 *
	 * @param v      the value to write
	 * @param output the output to write to
	 */
	public void writeValue(Object v, CharacterOutput output) {
		if (v == null)
			output.write(NULL_CHARS);
		else if (v instanceof CharSequence)
			writeString((CharSequence)v, output);
		else if (v instanceof Number)
			output.write(v.toString());
		else if (v instanceof Config)
			writeObject((Config)v, output);
		else if (v instanceof Collection)
			writeArray((Collection<?>)v, output);
		else if (v instanceof Boolean)
			writeBoolean((boolean)v, output);
		else if (v.getClass().isArray())
			writeArray(v, output);
		else
			throw new WritingException("Unsupported value type: " + v.getClass());
	}

	/**
	 * Writes a Collection as a JSON array.
	 *
	 * @param collection the Collection to write
	 * @param output     the output to write to
	 */
	public void writeArray(Collection<?> collection, CharacterOutput output) {
		if (collection.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}
		Iterator<?> it = collection.iterator();
		output.write('[');//open array
		if (newlineAfterObjectStart) output.write(newline);
		boolean indentElements = indentArrayElementsPredicate.test(collection);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}
		while (true) {
			Object value = it.next();
			if (indentElements) writeIndent(output);
			writeValue(value, output);
			if (it.hasNext()) {
				output.write(VALUE_SEPARATOR);
				if (indentElements) output.write(newline);
			} else {
				if (indentElements) output.write(newline);
				break;
			}
		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		output.write(']');//close array
	}

	private void writeArray(Object array, CharacterOutput output) {
		//Converts the array into a List:
		int length = Array.getLength(array);
		List<Object> list = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			list.add(Array.get(array, i));
		}
		//Then, writes the list as a JSON array:
		writeArray(list, output);
	}

	/**
	 * Writes a boolean in the JSON format.
	 *
	 * @param b      the boolean to write
	 * @param output the output to write to
	 */
	public void writeBoolean(boolean b, CharacterOutput output) {
		if (b) output.write(TRUE_CHARS);
		else output.write(FALSE_CHARS);
	}

	/**
	 * Writes a String in the JSON format.
	 *
	 * @param s      the String to write
	 * @param output the output to write to
	 */
	public void writeString(CharSequence s, CharacterOutput output) {
		output.write('"');//open string
		final int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int escapeIndex = Utils.arrayIndexOf(TO_ESCAPE, c);
			if (escapeIndex != -1) {//the character must be escaped
				char escaped = ESCAPED[escapeIndex];
				output.write('\\');
				output.write(escaped);
			} else {
				output.write(c);
			}
		}
		output.write('"');//close string
	}

	public Predicate<Config> getIndentObjectElementsPredicate() {
		return indentObjectElementsPredicate;
	}

	public void setIndentObjectElementsPredicate(Predicate<Config> indentObjectElementsPredicate) {
		this.indentObjectElementsPredicate = indentObjectElementsPredicate;
	}

	public Predicate<Collection> getIndentArrayElementsPredicate() {
		return indentArrayElementsPredicate;
	}

	public void setIndentArrayElementsPredicate(Predicate<Collection> indentArrayElementsPredicate) {
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

	void increaseIndentLevel() {
		currentIndentLevel++;
	}

	void decreaseIndentLevel() {
		currentIndentLevel--;
	}

	void writeIndent(CharacterOutput output) {
		for (int i = 0; i < currentIndentLevel; i++) {
			output.write(indent);
		}
	}
}
