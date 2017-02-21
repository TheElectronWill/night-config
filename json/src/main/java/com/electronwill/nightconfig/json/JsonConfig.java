package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.serialization.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A JSON configuration. It supports the following types:
 * <ul>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Boolen</li>
 * <li>String</li>
 * <li>List and its subclasses</li>
 * <li>Config and its subclasses</li>
 * </ul>
 *
 * @author TheElectronWill
 */
public final class JsonConfig extends MapConfig implements FileConfig {

	@Override
	public boolean supportsType(Class<?> type) {
		return type == Integer.class
			|| type == Long.class
			|| type == Float.class
			|| type == Double.class
			|| type == Boolean.class
			|| type == String.class
			|| List.class.isAssignableFrom(type)
			|| Config.class.isAssignableFrom(type);
	}

	@Override
	public JsonConfig createEmptyConfig() {
		return new JsonConfig();
	}

	@Override
	public void writeTo(File file) throws IOException {
		try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			CharacterOutput output = new WriterOutput(fileWriter);
			JsonWriter jsonWriter = new JsonWriter(output);
			jsonWriter.writeJsonObject(this);
		}//finally closes the writer
	}

	@Override
	public void readFrom(File file) throws IOException {
		try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			CharacterInput input = new ReaderInput(fileReader);
			JsonParser jsonParser = new JsonParser(input);
			this.asMap().clear();//clears the config
			jsonParser.parseJsonObject(this);//reads the value from the file to the config
		}//finally closes the reader
	}
}
