package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.serialization.FileConfig;
import com.electronwill.nightconfig.core.serialization.WriterOutput;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public final class HoconConfig extends MapConfig implements FileConfig {
	public HoconConfig() {}

	public HoconConfig(Map<String, Object> map) {
		super(map);
	}

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
	public HoconConfig createSubConfig() {
		return new HoconConfig();
	}

	@Override
	public void writeTo(File file) throws IOException {
		try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			HoconWriter hoconWriter = new HoconWriter();
			hoconWriter.writeConfig(this, new WriterOutput(fileWriter));
		}
	}

	@Override
	public void readFrom(File file) throws IOException {
		try (Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			this.asMap().clear();
			HoconParser.parseConfiguration(file, this);
		}
	}
}
