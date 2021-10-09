package test.nfa;

import lyun.voice2code.pattern.*;
import lyun.voice2code.nfa.*;

public class RegexSetTest {
	public static void main(String[] args) {
		String text = "define package hello";
		Regex reg = RegexSet.compile(new PatternSet()).matchPattern(text);
		System.out.println(reg.getPattern().getPattern());
	}

}
