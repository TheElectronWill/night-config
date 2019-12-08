package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MemoryConfig;
import com.electronwill.nightconfig.core.impl.ReaderInput;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.electronwill.nightconfig.core.utils.StringUtils.single;
import static com.electronwill.nightconfig.json.JsonToken.*;

/**
 * Produces data structures from json tokens ({@link com.electronwill.nightconfig.json.JsonToken},
 * produced by {@link JsonTokenizer}).
 *
 * @author TheElectronWill
 */
public final class JsonParser implements ConfigParser {
	private final boolean allowEmptyDoc;

	public JsonParser() {
		this(false);
	}

	public JsonParser(boolean alllowEmptyDoc) {
		this.allowEmptyDoc = alllowEmptyDoc;
	}

	public Object parseElement(Reader reader) {
		JsonTokenizer tokenizer = new JsonTokenizer(new ReaderInput(reader));
		return parseValue(tokenizer, tokenizer.next());
	}

	private Config parseConfigContent(JsonTokenizer tokenizer, Config dst) {
		JsonToken token = tokenizer.next();
		if (token == OBJECT_END)
			return dst;
		parseKeyValue(tokenizer, token, dst);
		while ((token = tokenizer.next()) == ELEMENT_SEPARATOR) {
			parseKeyValue(tokenizer, tokenizer.next(), dst);
		}
		if (token != OBJECT_END)
			throw new ParsingException("");
		return dst;
	}

	private void parseKeyValue(JsonTokenizer tokenizer, JsonToken keyToken, Config dst) {
		if (keyToken != VALUE_STRING)
			throw new ParsingException("");

		CharSequence key = tokenizer.textValue();
		JsonToken separator = tokenizer.next();
		if (separator != KV_SEPARATOR)
			throw new ParsingException("");

		Object value = parseValue(tokenizer, tokenizer.next());
		dst.set(single(key.toString()), value);
	}

	private List<?> parseListContent(JsonTokenizer tokenizer, List<Object> dst) {
		JsonToken token = tokenizer.next();
		if (token == ARRAY_END)
			return dst;
		dst.add(parseValue(tokenizer, token));
		while ((token = tokenizer.next()) == ELEMENT_SEPARATOR) {
			dst.add(parseValue(tokenizer, tokenizer.next()));
		}
		if (token != ARRAY_END)
			throw new ParsingException("");
		return dst;
	}

	private Object parseValue(JsonTokenizer tokenizer, JsonToken token) {
		switch (token) {
			case VALUE_STRING:
				return tokenizer.textValue();
			case VALUE_INTEGER:
				return tokenizer.intValue();
			case VALUE_FLOATING:
				return tokenizer.doubleValue();
			case VALUE_TRUE:
				return Boolean.TRUE;
			case VALUE_FALSE:
				return Boolean.FALSE;
			case VALUE_NULL:
				return null;
			case ARRAY_START:
				return parseListContent(tokenizer, new ArrayList<>());
			case OBJECT_START:
				return parseConfigContent(tokenizer, new MemoryConfig());
			default:
				throw new ParsingException("");
		}
	}
}