package com.electronwill.nightconfig.hocon;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
class HoconParserTest {

	@Test
	public void readWriteReadAgain() throws IOException {
		File file = new File("test.hocon");
		HoconConfig parsed = new HoconParser().parseConfig(file);

		System.out.println("--- parsed --- \n" + parsed);
		System.out.println("--------------------------------------------");
		java.io.StringWriter sw = new StringWriter();
		HoconWriter writer = new HoconWriter();
		writer.writeConfig(parsed, sw);
		System.out.println("--- written --- \n" + sw);
		System.out.println("--------------------------------------------");

		HoconConfig reparsed = new HoconParser().parseConfig(new StringReader(sw.toString()));
		System.out.println("--- reparsed --- \n" + reparsed);
		assertEquals(parsed, reparsed);
	}

}