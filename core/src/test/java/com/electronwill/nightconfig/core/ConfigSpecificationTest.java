package com.electronwill.nightconfig.core;

import java.util.Arrays;
import org.junit.Test;

/**
 * @author TheElectronWill
 */
public class ConfigSpecificationTest {

	@Test
	public void testConfigSpec() {
		ConfigSpecification spec = new ConfigSpecification();
		spec.defineInt("a.i", 0, -20, 20);
		spec.defineLong("a.l", 0, -20, 20);
		spec.defineFloat("a.f", 0.1f, -0.2f, 0.2f);
		spec.defineDouble("a.d", 0.1, -0.1, 0.2);
		spec.defineString("a.s", "default", "a", "b", "c", "d", "e", "f", "default");
		spec.defineList("a.list", Arrays.asList("1", "2"), element -> element instanceof String);

		{
			Config config = new SimpleConfig();
			config.setInt("a.i", 256);
			config.setLong("a.l", 1234567890);
			config.setFloat("a.f", 12f);
			config.setDouble("a.d", 123d);
			config.setString("a.s", "value");
			config.setList("a.list", Arrays.asList("hey", null, false, 1));

			assert !spec.check(config);
			System.out.println("Before correction: " + config);
			spec.correct(config);
			System.out.println("After correction: " + config);
			assert spec.check(config) : "Config was not corrected correctly!";
		}

		{
			Config config = new SimpleConfig();
			config.setInt("a.i", 18);
			config.setLong("a.l", 18);
			config.setFloat("a.f", 0.15f);
			config.setDouble("a.d", -0.09);
			config.setString("a.s", "a");
			config.setList("a.list", Arrays.asList("test", ""));

			assert spec.check(config);
			System.out.println("Before correction: " + config);
			spec.correct(config);
			System.out.println("After correction: " + config);
			assert spec.check(config) : "Config was not corrected correctly!";
		}
	}

}