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
public final class FancyJsonWriter {
	private static final char[] NULL_CHARS = {'n', 'u', 'l', 'l'};
	private static final char[] TRUE_CHARS = {'t', 'r', 'u', 'e'};
	private static final char[] FALSE_CHARS = {'f', 'a', 'l', 's', 'e'};
	private static final char[] TO_ESCAPE = {'"', '\n', '\r', '\t', '\\'};
	private static final char[] ESCAPED = {'"', 'n', 'r', 't', '\\'};

	private final CharacterOutput output;
	private final char[] indent, newline;
	private final boolean indentObjects, indentArrays, spaceArrays;
	private final boolean newlineBeforeObject, newlineBeforeArray;
	private int indentation = 0;

	public FancyJsonWriter(CharacterOutput output, char[] indent, char[] newline, boolean indentObjects, boolean indentArrays, boolean spaceArrays, boolean newlineBeforeObject, boolean newlineBeforeArray) {
		this.output = output;
		this.indent = indent;
		this.newline = newline;
		this.indentObjects = indentObjects;
		this.indentArrays = indentArrays;
		this.spaceArrays = spaceArrays;
		this.newlineBeforeObject = newlineBeforeObject;
		this.newlineBeforeArray = newlineBeforeArray;
	}

	public void writeJsonObject(Config config) {
		writeObject(config);
	}

	private void indent() {
		for (int i = 0; i < indentation; i++) {
			output.write(indent);
		}
	}

	private void writeObject(Config config) {
		if (newlineBeforeObject) {
			output.write(newline);
			indent();
		}
		if (indentObjects) {
			indentation++;
		}
		output.write('{');
		if (indentObjects) output.write(newline);
		final Iterator<Map.Entry<String, Object>> it = config.asMap().entrySet().iterator();
		do {
			final Map.Entry<String, Object> entry = it.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();
			if (indentObjects) indent();
			writeString(key);
			output.write(':');
			output.write(' ');
			writeValue(value);
			if (it.hasNext()) {
				output.write(',');
				if (indentObjects) output.write(newline);
			} else {
				break;
			}

		} while (true);
		if (indentObjects) {
			output.write(newline);
			indentation--;
			indent();
		}
		output.write('}');
	}

	private void writeArray(Iterable<?> iterable) {
		if (newlineBeforeArray) {
			output.write(newline);
			indent();
		}
		if (indentArrays) {
			indentation++;
		}
		output.write('[');
		if (indentArrays) output.write(newline);
		final Iterator<?> it = iterable.iterator();
		do {
			Object value = it.next();
			if (indentArrays) indent();
			writeValue(value);
			if (it.hasNext()) {
				output.write(',');
				if (spaceArrays) output.write(' ');
				if (indentArrays) output.write(newline);
			} else {
				break;
			}
		} while (true);
		if (indentArrays) {
			output.write(newline);
			indentation--;
			indent();
		}
		output.write(']');
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

	public static final class Builder {
		private char[] indent = {'\t'}, newline = {'\n'};
		private boolean indentObjects = true, indentArrays = true, spaceArrays = false;
		private boolean newlineBeforeObject = false, newlineBeforeArray = false;

		public FancyJsonWriter build(CharacterOutput output) {
			return new FancyJsonWriter(output, indent, newline, indentObjects, indentArrays, spaceArrays, newlineBeforeObject, newlineBeforeArray);
		}

		public Builder newlineBeforeObject(boolean newlineBeforeObject) {
			this.newlineBeforeObject = newlineBeforeObject;
			return this;
		}

		public Builder newlineBeforeArray(boolean newlineBeforeArray) {
			this.newlineBeforeArray = newlineBeforeArray;
			return this;
		}

		public Builder indent(char[] indent) {
			this.indent = indent;
			return this;
		}

		public Builder newline(char[] newline) {
			this.newline = newline;
			return this;
		}

		public Builder indent(String indent) {
			this.indent = indent.toCharArray();
			return this;
		}

		public Builder newline(String newline) {
			this.newline = newline.toCharArray();
			return this;
		}

		public Builder indentObjects(boolean indentObjects) {
			this.indentObjects = indentObjects;
			return this;
		}

		public Builder indentArrays(boolean indentArrays) {
			this.indentArrays = indentArrays;
			return this;
		}

		public Builder spaceArrays(boolean spaceArrays) {
			this.spaceArrays = spaceArrays;
			return this;
		}
	}

}
