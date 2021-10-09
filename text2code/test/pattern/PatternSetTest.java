package test.pattern;

import java.util.ArrayList;

import lyun.voice2code.pattern.*;

public class PatternSetTest {
	public static void main(String[] args) {
		PatternSetTest.testMatch();
	}

	public static void testMatch() {
		PatternSet patSet = new PatternSet();
		String patStr = "define package search";
		ArrayList<Pattern> patList = patSet.match(patStr);
		System.out.println("matched pattern:");
		for (Pattern pattern : patList) {
			System.out.println(pattern.toVoiceJavaPattern());
		}
	}

}
