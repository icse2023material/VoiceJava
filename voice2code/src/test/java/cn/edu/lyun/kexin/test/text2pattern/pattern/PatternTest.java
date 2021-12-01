package cn.edu.lyun.kexin.test.text2pattern.pattern;

import cn.edu.lyun.kexin.text2pattern.pattern.*;

public class PatternTest {
	public static void main(String[] args) {
		Pattern expr15Pat = new Pattern("expr15", "[expression]? null",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("null") });

		System.out.println(expr15Pat.toString());
		System.out.println(expr15Pat.toVoiceJavaPattern());
	}

}
