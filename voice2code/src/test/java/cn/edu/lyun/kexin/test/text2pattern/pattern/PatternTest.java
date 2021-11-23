package cn.edu.lyun.kexin.test.text2pattern.pattern;

import cn.edu.lyun.kexin.text2pattern.pattern.*;

public class PatternTest {
	public static void main(String[] args) {
		Pattern pat = new Pattern("package", "define package [_]+ [dot [_]+]*",
				new Unit[] { new Unit("define"), new Unit("package"), new Unit("plus", new Unit()),
						new Unit("asterisk", new Unit("dot"), new Unit("plus", new Unit())) });

		System.out.println(pat.toString());
		System.out.println(pat.toVoiceJavaPattern());
	}

}
