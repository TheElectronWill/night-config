package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.*;
import com.electronwill.nightconfig.core.utils.FakeUnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * A configurable HOCON writer.
 *
 * @author TheElectronWill
 */
public final class HoconWriter implements ConfigWriter {
	// --- Constant char arrays ---
	private static final char[] NULL_CHARS = {'n', 'u', 'l', 'l'};
	private static final char[] TRUE_CHARS = {'t', 'r', 'u', 'e'};
	private static final char[] FALSE_CHARS = {'f', 'a', 'l', 's', 'e'};
	private static final char[] EMPTY_OBJECT = {'{', '}'}, EMPTY_ARRAY = {'[', ']'};

	private static final char[] TO_ESCAPE = {'"', '\n', '\r', '\t', '\\'};
	private static final char[] ESCAPED = {'"', 'n', 'r', 't', '\\'};

	private static final char[] VALUE_SEPARATOR = {',', ' '};
	private static final char[] FORBIDDEN_IN_UNQUOTED = {'$', '"', '{', '}', '[', ']', ':', '=',
														 ',', '+', '#', '`', '^', '?', '!', '@',
														 '*', '&', '\\'};

	// --- Writer's settings ---
	private Predicate<UnmodifiableConfig> indentObjectElementsPredicate = c -> true;
	private Predicate<Collection<?>> indentArrayElementsPredicate = c -> true;
	private boolean newlineAfterObjectStart;
	private char[] newline = NewlineStyle.system().chars;
	private char[] indent = IndentStyle.TABS.chars;
	private char[] kvSeparator = KeyValueSeparatorStyle.COLON.chars;
	private char[] commentPrefix = CommentStyle.HASH.chars;
	private int currentIndentLevel;

	// --- Writer's methods ---
	@Override
	public void write(UnmodifiableConfig config, Writer writer) {
		currentIndentLevel = -1;
		UnmodifiableCommentedConfig commentedConfig;
		if (config instanceof UnmodifiableCommentedConfig) {
			commentedConfig = (UnmodifiableCommentedConfig)config;
		} else {
			commentedConfig = new FakeUnmodifiableCommentedConfig(config);
		}
		writeObject(commentedConfig, new WriterOutput(writer), true);
	}

	private void writeObject(UnmodifiableCommentedConfig config, CharacterOutput output, boolean root) {
		if (config.isEmpty()) {
			output.write(EMPTY_OBJECT);
			return;
		}
		if (!root) {
			output.write('{');// HOCON allows to omit the root braces
		}
		if (newlineAfterObjectStart) {
			output.write(newline);
		}
		final Iterator<? extends UnmodifiableCommentedConfig.Entry> it = config.entrySet().iterator();
		final boolean indentElements = indentObjectElementsPredicate.test(config);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}
		do {
			final UnmodifiableCommentedConfig.Entry entry = it.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();
			final List<String> comments = StringUtils.splitLines(entry.getComment());
			for (String comment : comments) {
				writeIndent(output);
				output.write(commentPrefix);
				output.write(comment);
				output.write(newline);
			}
			if (indentElements) {
				writeIndent(output);// Indents the line
			}
			writeString(key, output);// key
			if (value instanceof UnmodifiableConfig) {
				output.write(' ');
			} else {
				output.write(kvSeparator);
				// HOCON allows to omit the separator if the value is a config
			}
			writeValue(value, output);// value
			if (indentElements) {
				output.write(newline);
			} else {
				output.write(',');
			}
		} while (it.hasNext());
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		if (!root) {
			output.write('}');// HOCON allows to omit the root braces
		}
	}

	private void writeValue(Object v, CharacterOutput output) {
		if (v == null || v == NULL_OBJECT) {
			output.write(NULL_CHARS);
		} else if (v instanceof String) {
			writeString(v.toString(), output);
		} else if (v instanceof Enum) {
			writeString(((Enum<?>)v).name(), output);
		} else if (v instanceof Number) {
			output.write(v.toString());
		} else if (v instanceof UnmodifiableCommentedConfig) {
			writeObject((UnmodifiableCommentedConfig)v, output, false);
		} else if (v instanceof UnmodifiableConfig) {
			writeObject(new FakeUnmodifiableCommentedConfig((UnmodifiableConfig)v), output, false);
		} else if (v instanceof Collection) {
			writeArray((Collection<?>)v, output);
		} else if (v instanceof Boolean) { writeBoolean((boolean)v, output); } else {
			throw new WritingException("Unsupported value type: " + v.getClass());
		}
	}

	private void writeArray(Collection<?> collection, CharacterOutput output) {
		if (collection.isEmpty()) {
			output.write(EMPTY_ARRAY);
			return;
		}
		output.write('[');// Opens the array
		if (newlineAfterObjectStart) {
			output.write(newline);
		}
		final Iterator<?> it = collection.iterator();
		final boolean indentElements = indentArrayElementsPredicate.test(collection);
		if (indentElements) {
			output.write(newline);
			increaseIndentLevel();
		}
		while (true) {
			Object value = it.next();
			if (indentElements) {
				writeIndent(output);
			}
			writeValue(value, output);
			if (it.hasNext()) {
				output.write(VALUE_SEPARATOR);
				if (indentElements) {
					output.write(newline);
				}
			} else {
				if (indentElements) {
					output.write(newline);
				}
				break;// Nothing else to write
			}
		}
		if (indentElements) {
			decreaseIndentLevel();
			writeIndent(output);
		}
		output.write(']');// Closes the array
	}

	private void writeBoolean(boolean b, CharacterOutput output) {
		if (b) {
			output.write(TRUE_CHARS);
		} else {
			output.write(FALSE_CHARS);
		}
	}

	private void writeString(String s, CharacterOutput output) {
		if (canBeUnquoted(s)) {
			output.write(s);
			return;
		}
		output.write('"');
		final int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int escapeIndex = Utils.arrayIndexOf(TO_ESCAPE, c);
			if (escapeIndex == -1) {
				output.write(c);
			} else {// the character must be escaped
				char escaped = ESCAPED[escapeIndex];
				output.write('\\');
				output.write(escaped);
			}
		}
		output.write('"');
	}

	private boolean canBeUnquoted(CharSequence s) {
		final int length = s.length();
		for (int i = 0; i < length; i++) {
			if (Utils.arrayContains(FORBIDDEN_IN_UNQUOTED, s.charAt(i))) {
				return false;
			}
		}
		return true;
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
	public HoconWriter setIndentObjectElementsPredicate(
		Predicate<UnmodifiableConfig> indentObjectElementsPredicate) {
		this.indentObjectElementsPredicate = indentObjectElementsPredicate;
		return this;
	}

	public HoconWriter setIndentArrayElementsPredicate(
		Predicate<Collection<?>> indentArrayElementsPredicate) {
		this.indentArrayElementsPredicate = indentArrayElementsPredicate;
		return this;
	}

	public HoconWriter setNewlineAfterObjectStart(boolean newlineAfterObjectStart) {
		this.newlineAfterObjectStart = newlineAfterObjectStart;
		return this;
	}

	public HoconWriter setIndent(IndentStyle indentStyle) {
		this.indent = indentStyle.chars;
		return this;
	}

	public HoconWriter setIndent(String indentString) {
		this.indent = indentString.toCharArray();
		return this;
	}

	public HoconWriter setNewline(NewlineStyle newlineStyle) {
		this.newline = newlineStyle.chars;
		return this;
	}

	public HoconWriter setNewline(String newlineString) {
		this.newline = newlineString.toCharArray();
		return this;
	}

	public HoconWriter setKeyValueSeparator(KeyValueSeparatorStyle separatorStyle) {
		this.kvSeparator = separatorStyle.chars;
		return this;
	}

	public HoconWriter setKeyValueSeparator(String separatorString) {
		this.kvSeparator = separatorString.toCharArray();
		return this;
	}

	public HoconWriter setCommentPrefix(CommentStyle commentPrefixStyle) {
		this.commentPrefix = commentPrefixStyle.chars;
		return this;
	}

	public HoconWriter setCommentPrefix(String commentPrefixString) {
		this.commentPrefix = commentPrefixString.toCharArray();
		return this;
	}
}