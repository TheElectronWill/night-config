package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.CharacterOutput;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WriterOutput;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author TheElectronWill
 */
public final class TomlWriter implements ConfigWriter<Config> {
	private boolean lenientBareKeys = false;
	private Predicate<Config> writeTableInlinePredicate = Config::isEmpty;
	private Predicate<String> writeStringLiteralPredicate = c -> false;
	private Predicate<List<?>> indentArrayElementsPredicate = c -> false;
	private char[] indent = {'\t'};
	private char[] newline = System.getProperty("line.separator").toCharArray();
	private int currentIndentLevel;

	@Override
	public void writeConfig(Config config, Writer writer) throws IOException {
		currentIndentLevel = -1;//-1 to make the root entries not indented
		TableWriter.writeSmartly(config, new ArrayList<>(), new WriterOutput(writer), this);
	}

	public boolean isLenientWithBareKeys() {
		return lenientBareKeys;
	}

	public void setLenientWithBareKeys(boolean lenientBareKeys) {
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
