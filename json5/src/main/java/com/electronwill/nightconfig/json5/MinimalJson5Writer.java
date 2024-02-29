package com.electronwill.nightconfig.json5;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.*;

import java.io.Writer;
import java.util.*;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * A simple JSON writer that produces a minimized output: no line breaks, no spaces, no indentation, no comments.
 * Use the {@link FancyJson5Writer} if you want a nicer output.
 *
 * @author TheElectronWill
 */
public class MinimalJson5Writer implements ConfigWriter {
	static final char[] NULL_CHARS = {'n', 'u', 'l', 'l'};
	static final char[] TRUE_CHARS = {'t', 'r', 'u', 'e'};
	static final char[] FALSE_CHARS = {'f', 'a', 'l', 's', 'e'};
	static final char[] TO_ESCAPE = {'"', '\n', '\r', '\t', '\\'};
	static final char[] ESCAPED = {'"', 'n', 'r', 't', '\\'};
	static final char[] EMPTY_OBJECT = {'{', '}'}, EMPTY_ARRAY = {'[', ']'};

	@Override
	public void write(UnmodifiableConfig config, Writer writer) {
		writeConfig(config, new WriterOutput(writer));
	}

	public void writeCollection(Collection<?> collection, Writer writer) {
		writeCollection(collection, writer);
	}

	public void writeString(CharSequence csq, Writer writer) {
		writeString(csq, writer);
	}

	public void writeValue(Object value, Writer writer) {
		writeValue(value, writer);
	}

	private void writeConfig(UnmodifiableConfig config, CharacterOutput output) {
		if (config.isEmpty()) {
			output.write(EMPTY_OBJECT);
			return;
		}
		Iterator<? extends UnmodifiableConfig.Entry> it = config.entrySet().iterator();
		output.write('{');
		while (true) {
			UnmodifiableConfig.Entry entry = it.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			writeString(key, output); // key
			output.write(':'); // separator
			writeValue(value, output); // value
			if (it.hasNext()) output.write(',');
			else break;
		}
		output.write('}');
	}

	private void writeValue(Object value, CharacterOutput output) {
		if (value == null || value == NULL_OBJECT) output.write(NULL_CHARS);
		else if (value instanceof CharSequence) writeString((CharSequence) value, output);
		else if (value instanceof Enum) writeString(((Enum<?>) value).name(), output);
		else if (value instanceof Number) output.write(value.toString());
		else if (value instanceof UnmodifiableConfig) writeConfig((UnmodifiableConfig) value, output);
		else if (value instanceof Collection) writeCollection((Collection<?>) value, output);
		else if (value instanceof Boolean) writeBoolean((boolean) value, output);
		else if (value instanceof Object[]) {
			List<Object> list = Collections.singletonList(value);
			writeCollection(list, output);
		}
		else if (value instanceof long[]) output.write(Arrays.toString((long[]) value));
		else if (value instanceof int[]) output.write(Arrays.toString((int[]) value));
		else if (value instanceof double[]) output.write(Arrays.toString((double[]) value));
		else if (value instanceof float[]) output.write(Arrays.toString((float[]) value));
		else if (value instanceof short[]) output.write(Arrays.toString((short[]) value));
		else if (value instanceof byte[]) output.write(Arrays.toString((byte[]) value));
		else throw new WritingException("Unsupported value type: " + value.getClass());
	}

	private void writeCollection(Collection<?> collection, CharacterOutput output) {
		if (collection.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}

		output.write('[');
		if (collection instanceof RandomAccess) {
			List<?> list = (List<?>) collection; // A class implementing RandomAccess should be a List
			int lastIndex = list.size();
			for (int i = 0; i < lastIndex; i++) {
				Object value = list.get(i);
				writeValue(value, output);
				output.write(',');
			}
			writeValue(list.get(lastIndex), output);
		} else {
			Iterator<?> it = collection.iterator();
			while(true) {
				Object value = it.next();
				writeValue(value, output); // Write the Value
				if (it.hasNext()) output.write(',');
				else break;
			}
		}
		output.write(']');
	}

	private void writeBoolean(boolean b, CharacterOutput output) {
		if (b) output.write(TRUE_CHARS);
		else output.write(FALSE_CHARS);
	}

	private void writeString(CharSequence csq, CharacterOutput output) {
		output.write('"');
		final int length = csq.length();
		for (int i = 0; i < length; i++) {
			char c = csq.charAt(i);
			int escapeIndex = Utils.arrayIndexOf(TO_ESCAPE, c);
			if (escapeIndex == -1) {
				output.write(c);
			} else {
				char escaped = ESCAPED[escapeIndex];
				output.write('\\');
				output.write(escaped);
			}
		}
		output.write('"');
	}
}
