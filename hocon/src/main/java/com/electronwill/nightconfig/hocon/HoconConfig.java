package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.serialization.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

/**
 * @author TheElectronWill
 */
public final class HoconConfig extends MapConfig implements FileConfig {

	private static final HashSet<Class<?>> SUPPORTED_TYPES = new HashSet<>();

	static {
		SUPPORTED_TYPES.add(Integer.class);
		SUPPORTED_TYPES.add(Long.class);
		SUPPORTED_TYPES.add(Float.class);
		SUPPORTED_TYPES.add(Double.class);
		SUPPORTED_TYPES.add(Boolean.class);
		SUPPORTED_TYPES.add(String.class);
		SUPPORTED_TYPES.add(List.class);
		SUPPORTED_TYPES.add(Config.class);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type) || List.class.isAssignableFrom(type) || Config.class.isAssignableFrom(type);
	}

	@Override
	public HoconConfig createEmptyConfig() {
		return new HoconConfig();
	}

	@Override
	public void writeTo(File file) throws IOException {
		try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			CharacterOutput output = new WriterOutput(fileWriter);
			HoconWriter jsonWriter = new HoconWriter(output);
			jsonWriter.writeJsonObject(this);
		}//finally closes the writer
	}

	@Override
	public void readFrom(File file) throws IOException {
		try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			CharacterInput input = new ReaderInput(fileReader);
			HoconParser jsonParser = new HoconParser(input);
			this.asMap().clear();//clears the config
			jsonParser.parseJsonObject(this);//reads the value from the file to the config
		}//finally closes the reader
	}
}
