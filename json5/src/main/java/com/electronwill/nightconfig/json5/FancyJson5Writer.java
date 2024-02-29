package com.electronwill.nightconfig.json5;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig.Entry;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.*;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.io.Writer;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
import static com.electronwill.nightconfig.json5.MinimalJson5Writer.*;

public class FancyJson5Writer implements ConfigWriter {
	private static final char[] ENTRY_SEPARATOR = {':', ' '}, VALUE_SEPARATOR = {',', ' '};

	// --- Writer Settings ---
	private Predicate<UnmodifiableConfig> indentObjectElementsPredicate = c -> true;
	private Predicate<Collection<?>> indentArrayElementsPredicate = c -> true;
	private boolean newlineAfterObjectStart;
	private boolean writeCommentsInLine;
	private char[] newline = NewlineStyle.system().chars;
	private char[] indent = IndentStyle.TABS.chars;
	private int currentIndentLevel;

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

		output.write('{');
		if (newlineAfterObjectStart) output.write(newline);

		boolean indentElements = indentObjectElementsPredicate.test(config);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}

		UnmodifiableCommentedConfig commentedConfig = UnmodifiableCommentedConfig.fake(config);
		for (Iterator<? extends Entry> it = commentedConfig.entrySet().iterator(); it.hasNext();) {
			Entry entry = it.next();

			if (indentElements) writeIndent(output);
			if (!writeCommentsInLine) writeIndentComment(entry.getComment(), output);

			writeString(entry.getKey(), output);
			output.write(ENTRY_SEPARATOR);
			writeValue(entry.getValue(), output);

			if (it.hasNext()) output.write(',');
			if (writeCommentsInLine) writeInlineComment(entry.getComment(), output);
			if (indentElements) output.write(newline);
			if (!it.hasNext()) break;


		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		output.write('}');
	}

	private void writeValue(Object value, CharacterOutput output) {
		if (value == null || value == NULL_OBJECT) output.write(NULL_CHARS);
		else if (value instanceof CharSequence) writeString((CharSequence)value, output);
		else if (value instanceof Enum) writeString(((Enum<?>)value).name(), output);
		else if (value instanceof Number) output.write(value.toString());
		else if (value instanceof UnmodifiableConfig) writeObject((UnmodifiableConfig)value, output);
		else if (value instanceof Collection) writeArray((Collection<?>)value, output);
		else if (value instanceof Boolean) writeBoolean((boolean)value, output);
		else if (value instanceof Object[]) writeArray(Arrays.asList((Object[])value), output);
		else if (value.getClass().isArray()) writeArray(value, output);
		else throw new WritingException("Unsupported value type: " + value.getClass());
	}

	private void writeArray(Collection<?> collection, CharacterOutput output) {
		if (collection.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}

		output.write('[');
		if (newlineAfterObjectStart) {
			output.write(newline);
		}
		boolean indentElements = indentArrayElementsPredicate.test(collection);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}

		for (Iterator<?> it = collection.iterator(); it.hasNext();) {
			Object value = it.next();
			if (indentElements) writeIndent(output);
			writeValue(value, output);

			if (it.hasNext()) {
				output.write(VALUE_SEPARATOR);
            }
            if (indentElements) {
                output.write(newline);
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
			} else { // This character must be escaped
				char escaped = ESCAPED[escapeIndex];
				output.write('\\');
				output.write(escaped);
			}
		}
		output.write('"');
	}

	private void writeInlineComment(String commentString, CharacterOutput output) {
		if (commentString == null || commentString.isEmpty()) return;
		if (commentString.contains("\n")) {
			throw new IllegalArgumentException("Invalid comment, remove line breaks to fix it.");
		} else {
			output.write(' ');
			output.write("//");
			output.write(commentString);
		}
	}

	private void writeIndentComment(String commentString, CharacterOutput output) {
		List<String> comments = StringUtils.splitLines(commentString);
		for (String comment : comments) {
			output.write("//");
			output.write(comment);
			output.write(newline);
			writeIndent(output);
		}
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
	public FancyJson5Writer setIndentObjectElementsPredicate(
		Predicate<UnmodifiableConfig> indentObjectElementsPredicate) {
		this.indentObjectElementsPredicate = indentObjectElementsPredicate;
		return this;
	}

	public FancyJson5Writer setIndentArrayElementsPredicate(
		Predicate<Collection<?>> indentArrayElementsPredicate) {
		this.indentArrayElementsPredicate = indentArrayElementsPredicate;
		return this;
	}

	public FancyJson5Writer setNewlineAfterObjectStart(boolean newlineAfterObjectStart) {
		this.newlineAfterObjectStart = newlineAfterObjectStart;
		return this;
	}

	public FancyJson5Writer setWriteCommentsInLine(boolean writeCommentsInLine) {
		this.writeCommentsInLine = writeCommentsInLine;
		return this;
	}

	public FancyJson5Writer setIndent(IndentStyle indentStyle) {
		this.indent = indentStyle.chars;
		return this;
	}

	public FancyJson5Writer setIndent(String indent) {
		this.indent = indent.toCharArray();
		return this;
	}

	public FancyJson5Writer setNewline(NewlineStyle newlineStyle) {
		this.newline = newlineStyle.chars;
		return this;
	}

	public FancyJson5Writer setNewline(String newlineString) {
		this.newline = newlineString.toCharArray();
		return this;
	}
}
