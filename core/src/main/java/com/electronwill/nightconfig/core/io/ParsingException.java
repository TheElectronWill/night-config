package com.electronwill.nightconfig.core.io;

import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a parsing operation fails.
 *
 * @author TheElectronWill
 */
public class ParsingException extends RuntimeException {
	private final long line, column;

	public ParsingException(@Nullable Cursor positionReference, String message) {
		super(message);

		this.line = positionReference == null ? -1 : positionReference.line();
		this.column = positionReference == null ? -1 : positionReference.column();
	}

	public ParsingException(@Nullable Cursor positionReference, String message, Throwable cause) {
		super(message, cause);

		this.line = positionReference == null ? -1 : positionReference.line();
		this.column = positionReference == null ? -1 : positionReference.column();
	}

	@Override
	public String getMessage() {
		String pos = "";
		boolean hasLine = line != -1;
		boolean hasColumn = column != -1;
		boolean hasAny = hasLine || hasColumn;
		if (hasAny)
			pos += " [";
		if (hasLine)
			pos += "line " + line;
		if (hasColumn) {
			if (hasLine)
				pos += ", ";
			pos += "pos " + column;
		}
		if (hasAny)
			pos += "]";
		return super.getMessage() + pos;
	}

	public static ParsingException readFailed(Throwable cause) {
		return new ParsingException(null, "Failed to parse data from Reader", cause);
	}

	public static ParsingException notEnoughData() {
		return new ParsingException(null, "Not enough data available");
	}
}