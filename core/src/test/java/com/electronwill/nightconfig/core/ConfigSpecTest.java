package com.electronwill.nightconfig.core;

import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
class ConfigSpecTest {

	@Test
	public void test() {
		ConfigSpec spec = new ConfigSpec();
		spec.defineInRange("a.i", 0, -20, 20);
		spec.defineInRange("a.l", 0, -20, 20);
		spec.defineInRange("a.f", 0.1f, -0.2f, 0.2f);
		spec.defineInRange("a.d", 0.1, -0.1, 0.2);
		spec.defineInList("a.s", "default", Arrays.asList("a", "b", "c", "d", "e", "f", "default"));
		spec.defineList("a.list", Arrays.asList("1", "2"), element -> element instanceof String);

		{
			Config config = new SimpleConfig();
			config.set("a.i", 256);
			config.set("a.l", 1234567890);
			config.set("a.f", 12f);
			config.set("a.d", 123d);
			config.set("a.s", "value");
			config.set("a.list", Arrays.asList("hey", null, false, 1));

			assert !spec.isCorrect(config);
			System.out.println("Before correction: " + configToString(config));
			spec.correct(config);
			System.out.println("After correction: " + configToString(config));
			assert spec.isCorrect(config) : "Config was not corrected correctly!";
		}

		{
			Config config = new SimpleConfig();
			config.set("a.i", 18);
			config.set("a.l", 18);
			config.set("a.f", 0.15f);
			config.set("a.d", -0.09);
			config.set("a.s", "a");
			config.set("a.list", Arrays.asList("test", ""));

			assert spec.isCorrect(config);
			System.out.println("Before correction: " + configToString(config));
			spec.correct(config);
			System.out.println("After correction: " + configToString(config));
			assert spec.isCorrect(config) : "Config was not corrected correctly!";
		}
	}

	private static String configToString(Config c) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (Map.Entry<String, Object> entry : c.valueMap().entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			sb.append(key).append(" = ");
			if (value instanceof Config) {
				sb.append(configToString((Config)value));
			} else {
				sb.append(value);
			}
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append('}');
		return sb.toString();
	}

}