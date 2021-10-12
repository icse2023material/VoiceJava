
package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import cn.edu.lyun.kexin.text2pattern.nfa.*;

public class RegexSetTest {
	public static void main(String[] args) {
		String text = "define package hello world";
		Pattern patternResult = RegexSet.compile(new PatternSet()).matchPattern(text);
		System.out.println(patternResult.getPattern());
	}

}
