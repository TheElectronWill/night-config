package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serialization.CharacterOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public final class TomlWriter {
	private boolean lenientBareKeys = false;
	private Predicate<Config> writeTableInlinePredicate = Config::isEmpty;
	private Predicate<String> writeStringLiteralPredicate = c -> false;
	private Predicate<List<?>> indentArrayElementsPredicate = c -> false;
	private char[] indent = {'\t'};
	private char[] newline = System.getProperty("line.separator").toCharArray();
	private int currentIndentLevel;

	/**
	 * Writes a TOML configuration.
	 *
	 * @param config the config to write
	 * @param output the output to write the config to
	 */
	public void writeConfiguration(Config config, CharacterOutput output) {
		currentIndentLevel = -1;//-1 to make the root entries not indented
		TableWriter.writeSmartly(config, new ArrayList<>(), output, this);
	}

	public boolean usesLenientBareKeys() {
		return lenientBareKeys;
	}

	public void setUseLenientBareKeys(boolean lenientBareKeys) {
		this.lenientBareKeys = lenientBareKeys;
	}

	public Predicate<Config> getWriteTableInlinePredicate() {
		return writeTableInlinePredicate;
	}

	public void setWriteTableInlinePredicate(Predicate<Config> writeTableInlinePredicate) {
		this.writeTableInlinePredicate = writeTableInlinePredicate;
	}

	public Predicate<String> getWriteStringLiteralPredicate() {
		return writeStringLiteralPredicate;
	}

	public void setWriteStringLiteralPredicate(Predicate<String> writeStringLiteralPredicate) {
		this.writeStringLiteralPredicate = writeStringLiteralPredicate;
	}

	public Predicate<List<?>> getIndentArrayElementsPredicate() {
		return indentArrayElementsPredicate;
	}

	public void setIndentArrayElementsPredicate(Predicate<List<?>> indentArrayElementsPredicate) {
		this.indentArrayElementsPredicate = indentArrayElementsPredicate;
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
