package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.CharacterOutput;
import com.electronwill.nightconfig.core.serialization.SerializationException;
import com.electronwill.nightconfig.core.serialization.Utils;

import java.util.Iterator;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public final class JsonWriter {
	private static final char[] NULL_CHARS = {'n', 'u', 'l', 'l'};
	private static final char[] TRUE_CHARS = {'t', 'r', 'u', 'e'};
	private static final char[] FALSE_CHARS = {'f', 'a', 'l', 's', 'e'};
	private static final char[] TO_ESCAPE = {'"', '\n', '\r', '\t', '\\'};
	private static final char[] ESCAPED = {'"', 'n', 'r', 't', '\\'};

	private final CharacterOutput output;

	public JsonWriter(CharacterOutput output) {
		this.output = output;
	}

	public void writeJsonObject(Config config) {
		writeObject(config);
	}

	private void writeObject(Config config) {
		output.write('{');
		final Iterator<Map.Entry<String, Object>> it = config.asMap().entrySet().iterator();
		do {
			final Map.Entry<String, Object> entry = it.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();
			writeString(key);
			output.write(':');
			writeValue(value);
			if (it.hasNext())
				output.write(',');
			else
				break;

		} while (true);
		output.write('}');
	}

	private void writeValue(Object v) {
		if (v == null)
			writeNull();
		else if (v instanceof CharSequence)
			writeString((CharSequence) v);
		else if (v instanceof Number)
			output.write(v.toString());
		else if (v instanceof Config)
			writeObject((Config) v);
		else if (v instanceof Iterable)
			writeArray((Iterable) v);
		else if (v instanceof Boolean)
			writeBoolean((boolean) v);
		else
			throw new SerializationException("Unsupported value type: " + v.getClass());
	}

	private void writeArray(Iterable<?> iterable) {
		output.write('[');
		final Iterator<?> it = iterable.iterator();
		do {
			Object value = it.next();
			writeValue(value);
			if (it.hasNext())
				output.write(',');
			else
				break;
		} while (true);
		output.write(']');
	}

	private void writeBoolean(boolean b) {
		if (b)
			output.write(TRUE_CHARS);
		else
			output.write(FALSE_CHARS);
	}

	private void writeNull() {
		output.write(NULL_CHARS);
	}

	private void writeString(CharSequence s) {
		output.write('"');
		final int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int escapeIndex = Utils.arrayIndexOf(TO_ESCAPE, c);
			if (escapeIndex != -1) {
				char escaped = ESCAPED[escapeIndex];
				output.write('\\');
				output.write(escaped);
			} else {
				output.write(c);
			}
		}
		output.write('"');
	}

}
