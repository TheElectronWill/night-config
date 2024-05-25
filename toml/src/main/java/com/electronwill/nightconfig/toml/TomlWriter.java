package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.*;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public final class TomlWriter implements ConfigWriter {
	// --- Writer's settings ---
	private boolean lenientBareKeys = false;
	private Predicate<UnmodifiableConfig> writeTableInlinePredicate = UnmodifiableConfig::isEmpty;
	private Predicate<String> writeStringLiteralPredicate = str -> false;
	private Predicate<String> writeStringMultilinePredicate = str -> {
		int i0 = str.indexOf('\n'); // index of first newline
		int i1 = str.indexOf('\n', i0+1);
		int i2 = str.indexOf('\n', i1+1);
		int cr = str.indexOf('\r');
		return cr == -1 // no \r (it causes problem with splitLines and tests)
			&& ((i0 >= 0 && i1 > 0 && i2 > 0) // at least 3 newlines
			|| (i0 > 0 && i0 < str.length()-1)); // at least one newline in the middle
	};
	private Predicate<List<?>> indentArrayElementsPredicate = elem -> false;
	private char[] indent = IndentStyle.TABS.chars;
	private char[] newline = NewlineStyle.system().chars;
	private boolean hideRedundantLevels = true;

	// state
	private int currentIndentLevel;

	// --- Writer's methods ---
	@Override
	public void write(UnmodifiableConfig config, Writer writer) {
		currentIndentLevel = -1;//-1 to make the root entries not indented
		CharacterOutput output = new WriterOutput(writer);
		TableWriter.writeTopLevel(config, new ArrayList<>(), output, this);
	}

	// --- Getters/setters for the settings ---
	/**
	 * Gets the "hide redundant levels" policy. It is enabled by default.
	 * <p>
	 * If set to false, a possible output would be:
	 * <pre>
	 * {@literal
	 * [table]
	 * [table.sub]
	 * [table.sub.nested]
	 * value = 1
	 * }
	 * </pre>
	 * <p>
	 * If set to true, the same config will be written as:
	 * <pre>
	 * {@literal
	 * [table.sub.nested]
	 * value = 1
	 * }
	 * </pre>
	 * @return true if this writer hides the redundant intermediate levels.
	 */
	public boolean isHidingRedundantLevels() {
		return hideRedundantLevels;
	}

	/**
	 * Sets whether redundant intermediate levels should be hidden or written to the TOML output.
	 * @see #isHidingRedundantLevels()
	 */
	public void setHideRedundantLevels(boolean hideRedundantLevels) {
		this.hideRedundantLevels = hideRedundantLevels;
	}

	/**
	 * @see #isHidingRedundantLevels()
	 */
	public boolean isOmitIntermediateLevels() {
		return hideRedundantLevels;
	}

	/**
	 * @see #setHideRedundantLevels(boolean)
	 */
	public void setOmitIntermediateLevels(boolean omitIntermediateLevels) {
		setHideRedundantLevels(omitIntermediateLevels);
	}

	public boolean isLenientWithBareKeys() {
		return lenientBareKeys;
	}

	public void setLenientWithBareKeys(boolean lenientBareKeys) {
		this.lenientBareKeys = lenientBareKeys;
	}

	public void setWriteTableInlinePredicate(Predicate<UnmodifiableConfig> predicate) {
		this.writeTableInlinePredicate = predicate;
	}

	public void setWriteStringLiteralPredicate(Predicate<String> predicate) {
		this.writeStringLiteralPredicate = predicate;
	}

	public void setWriteStringMultilinePredicate(Predicate<String> predicate) {
		this.writeStringMultilinePredicate = predicate;
	}

	public void setIndentArrayElementsPredicate(Predicate<List<?>> predicate) {
		this.indentArrayElementsPredicate = predicate;
	}

	/**
	 * Changes the indentation style using a predefined style.
	 * For example, to indent with four spaces, call {@code setIndent(IndentStyle.SPACES_4)}.
	 */
	public void setIndent(IndentStyle indentStyle) {
		this.indent = indentStyle.chars;
	}

	/**
	 * Changes the indentation style using a custom string.
	 * For example, to indent with tabs, call {@code setIndent("\t")}.
	 */
	public void setIndent(String indentString) {
		this.indent = indentString.toCharArray();
	}

	public void setNewline(NewlineStyle newlineStyle) {
		this.newline = newlineStyle.chars;
	}

	/**
	 * Changes the string to write for newlines.
	 * By default, the system's line separator is used.
	 */
	public void setNewline(String newlineString) {
		this.newline = newlineString.toCharArray();
	}

	// --- Methods used by the writing classes ---
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

	void writeNewline(CharacterOutput output) {
		output.write(newline);
	}

	void writeIndentedComment(String commentString, CharacterOutput output) {
		List<String> comments = StringUtils.splitLines(commentString);
		for (String comment : comments) {
			writeIndent(output);
			output.write('#');
			output.write(comment);
			output.write(newline);
		}
	}

	void writeIndentedKey(String key, CharacterOutput output) {
		writeIndent(output);
		writeKey(key, output);
	}

	void writeKey(String key, CharacterOutput output) {
		if (Toml.isValidBareKey(key, lenientBareKeys)) {
			output.write(key);
		} else if (writeStringLiteralPredicate.test(key)) {
			StringWriter.writeLiteral(key, output);
		} else {
			StringWriter.writeBasic(key, output);
		}
	}

	boolean writesInline(UnmodifiableConfig config) {
		return writeTableInlinePredicate.test(config);
	}

	boolean writesLiteral(String string) {
		return writeStringLiteralPredicate.test(string);
	}

	boolean writesMultiline(String string) {
		return writeStringMultilinePredicate.test(string);
	}

	boolean writesIndented(List<?> list) {
		return indentArrayElementsPredicate.test(list);
	}
}