package com.electronwill.nightconfig.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

/**
 * @author TheElectronWill
 */
public class StringUtilsTest {
	@Test
	public void split() throws Exception {
		List<String> split1 = StringUtils.split("a.b.c", '.');
		assert split1.equals(Arrays.asList("a", "b", "c")) : "Invalid split1: " + split1;

		List<String> split2 = new LinkedList<>();
		StringUtils.split("some spaces here but no separator!", '.', split2);
		assert split2.equals(Arrays.asList("some spaces here but no separator!")) : "Invalid split2: " +
				split2;
	}

	@Test
	public void compareWithJreSplit() {
		//Split with '.' separator
		printSplitted("a.b.c", '.');
		printSplitted("a.b.c", ' ');//should not split because there is no space in "a.b.c"

		printSplitted(".a", '.');
		printSplitted("..a", '.');
		printSplitted("...a", '.');

		printSplitted("a.", '.');
		printSplitted("a..", '.');
		printSplitted("a...", '.');

		printSplitted("", '.');
		printSplitted(".", '.');
		printSplitted("..", '.');
		printSplitted("...", '.');

		printSplitted(".a.a.", '.');
		printSplitted("a..a", '.');
		printSplitted("a...a", '.');


		//Split with 's' separator
		System.out.println("========================================");
		printSplitted("asbsc", 's');
		printSplitted("asbsc", ' ');//should not split because there is no space in "a.b.c"

		printSplitted("sa", 's');
		printSplitted("ssa", 's');
		printSplitted("ssa", 's');

		printSplitted("as", 's');
		printSplitted("ass", 's');
		printSplitted("asss", 's');

		printSplitted("", 's');
		printSplitted("s", 's');
		printSplitted("ss", 's');
		printSplitted("sss", 's');

		printSplitted("sasas", 's');
		printSplitted("assa", 's');
		printSplitted("asssa", 's');
	}

	private void printSplitted(String str, char sep) {
		System.out.println();
		List<String> mySplit = StringUtils.split(str, sep);
		System.out.println(str + " -(" + sep + ")-> " + listToString(mySplit));

		String regex = (sep == '.') ? "\\." : String.valueOf(sep);
		String[] jreSplit = str.split(regex);
		System.out.println(str + " -(" + sep + ")-> " + arrayToString(jreSplit));
	}

	private String listToString(List<String> list) {
		return (list.isEmpty()) ? "∅" : list.toString();
	}

	private String arrayToString(String[] array) {
		return (array.length == 0) ? "∅" : Arrays.toString(array);
	}

}