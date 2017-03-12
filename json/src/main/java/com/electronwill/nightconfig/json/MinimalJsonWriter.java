package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.CharacterOutput;
import com.electronwill.nightconfig.core.serialization.ConfigWriter;
import com.electronwill.nightconfig.core.serialization.SerializationException;
import com.electronwill.nightconfig.core.serialization.Utils;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A simple JSON writer that produces a minimized output: no line breaks, no spaces, no indentation. Use
 * the {@link FancyJsonWriter} if you want a nicer output.
 *
 * @author TheElectronWill
 */
public final class MinimalJsonWriter implements ConfigWriter {
	static final char[] NULL_CHARS = {'n', 'u', 'l', 'l'};
	static final char[] TRUE_CHARS = {'t', 'r', 'u', 'e'};
	static final char[] FALSE_CHARS = {'f', 'a', 'l', 's', 'e'};
	static final char[] TO_ESCAPE = {'"', '\n', '\r', '\t', '\\'};
	static final char[] ESCAPED = {'"', 'n', 'r', 't', '\\'};
	static final char[] EMPTY_OBJECT = {'{', '}'}, EMPTY_ARRAY = {'[', ']'};

	/**
	 * Writes a configuration as a JSON object.
	 *
	 * @param config the config to write
	 * @param output the output to write to
	 */
	@Override
	public void writeConfig(Config config, CharacterOutput output) {
		if (config.isEmpty()) {
			output.write(EMPTY_OBJECT);
			return;
		}
		Iterator<Map.Entry<String, Object>> it = config.asMap().entrySet().iterator();
		output.write('{');//open object
		while (true) {
			final Map.Entry<String, Object> entry = it.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();
			writeString(key, output);//key
			output.write(':');//separator
			writeValue(value, output);//value
			if (it.hasNext()) {
				output.write(',');
			} else {
				break;
			}
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
			writeConfig((Config)v, output);
		else if (v instanceof Collection)
			writeArray((Collection<?>)v, output);
		else if (v instanceof Boolean)
			writeBoolean((boolean)v, output);
		else if (v.getClass().isArray())
			writeArray(v, output);
		else
			throw new SerializationException("Unsupported value type: " + v.getClass());
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
		while (true) {
			Object value = it.next();
			writeValue(value, output);//the value
			if (it.hasNext()) {
				output.write(',');//the separator
			} else {
				break;
			}
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
}
