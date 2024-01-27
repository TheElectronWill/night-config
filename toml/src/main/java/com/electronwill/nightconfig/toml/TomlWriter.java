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
	private Predicate<String> writeStringLiteralPredicate = c -> false;
	private Predicate<List<?>> indentArrayElementsPredicate = c -> false;
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
	public boolean isHidingRedundantLevels() {
		return hideRedundantLevels;
	}

	public void setHideRedundantLevels(boolean hideRedundantLevels) {
		this.hideRedundantLevels = hideRedundantLevels;
	}

	public boolean isLenientWithBareKeys() {
		return lenientBareKeys;
	}

	public void setLenientWithBareKeys(boolean lenientBareKeys) {
		this.lenientBareKeys = lenientBareKeys;
	}

	public void setWriteTableInlinePredicate(Predicate<UnmodifiableConfig> writeTableInlinePredicate) {
		this.writeTableInlinePredicate = writeTableInlinePredicate;
	}

	public void setWriteStringLiteralPredicate(Predicate<String> writeStringLiteralPredicate) {
		this.writeStringLiteralPredicate = writeStringLiteralPredicate;
	}

	public void setIndentArrayElementsPredicate(Predicate<List<?>> indentArrayElementsPredicate) {
		this.indentArrayElementsPredicate = indentArrayElementsPredicate;
	}

	public void setIndent(IndentStyle indentStyle) {
		this.indent = indentStyle.chars;
	}

	public void setIndent(String indentString) {
		this.indent = indentString.toCharArray();
	}

	public void setNewline(NewlineStyle newlineStyle) {
		this.newline = newlineStyle.chars;
	}

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

	boolean writesIndented(List<?> list) {
		return indentArrayElementsPredicate.test(list);
	}
}