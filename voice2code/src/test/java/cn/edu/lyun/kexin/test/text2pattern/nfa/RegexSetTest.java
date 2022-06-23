
package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import cn.edu.lyun.kexin.text2pattern.nfa.*;

public class RegexSetTest {
	public static void main(String[] args) {
		String text = "define variable split at index";
		Pattern patternResult = RegexSet.compile(new PatternSet()).matchPattern(text);
		System.out.println(patternResult.toString());
		patternResult.concatNames();
		System.out.println(patternResult.toString());
		System.out.println(patternResult.toVoiceJavaPattern());
		for (Unit unit : patternResult.getUnits()) {
			System.out.print("(");
			System.out.print(unit);
			System.out.print(unit.getKeyword());
			System.out.print(") ");
		}

	}

}
