package cn.edu.lyun.kexin.test.text2pattern.pattern;

import cn.edu.lyun.kexin.text2pattern.pattern.*;

public class PatternTest {
	public static void main(String[] args) {
		Pattern pat = new Pattern("typeVariable", "type (_ list | _ [dot _]? [with _+]?) variable _",
				new Unit[] { new Unit("type"),
						new Unit("or", new Unit("normal", new Unit(), new Unit("list")),
								new Unit(new Unit[] { new Unit(), new Unit("question", new Unit("dot"), new Unit()),
										new Unit("question", new Unit("with"), new Unit("plus", new Unit())) })),
						new Unit("variable"), new Unit() });
		System.out.println(pat.toString());
		System.out.println(pat.toVoiceJavaPattern());
	}

}
