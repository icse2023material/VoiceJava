package cn.edu.lyun.kexin.test.text2pattern.pattern;

import cn.edu.lyun.kexin.text2pattern.pattern.*;

public class PatternTest {
	public static void main(String[] args) {
		// Pattern pat = new Pattern("typeVariable", "type (_ list | _ [dot _]? [with
		// _+]?) variable _",
		// new Unit[] { new Unit("type"),
		// new Unit("or", new Unit("normal", new Unit(), new Unit("list")),
		// new Unit(new Unit[] { new Unit(), new Unit("question", new Unit("dot"), new
		// Unit()),
		// new Unit("question", new Unit("with"), new Unit("plus", new Unit())) })),
		// new Unit("variable"), new Unit() });
		Pattern pat = new Pattern("expr13", "[expression]? variable _ index _",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("variable"), new Unit(), new Unit("index"),
						new Unit() });
		pat = new Pattern("expr14", "[expression]? string [_]+",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("string"), new Unit("plus", new Unit()) });
		pat = new Pattern("typeVariable", "type (_ list | _ [dot _]? [with _+]?) variable _",
				new Unit[] { new Unit("type"),
						new Unit("or", new Unit("normal", new Unit(), new Unit("list")),
								new Unit(new Unit[] { new Unit(), new Unit("question", new Unit("dot"), new Unit()),
										new Unit("question", new Unit("with"), new Unit("plus", new Unit())) })),
						new Unit("variable"), new Unit() });
		Unit typeUnit = new Unit("or", new Unit("int"),
				new Unit("or", new Unit("byte"),
						new Unit("or", new Unit("short"),
								new Unit("or", new Unit("long"), new Unit("or", new Unit("char"), new Unit("or", new Unit("float"),
										new Unit("or", new Unit("double"), new Unit("or", new Unit("boolean"), new Unit("string")))))))));
		pat = new Pattern("let5",
				"let _ [dot _]? equal (int | byte | short | long | char | float | double | boolean | string) _ ",
				new Unit[] { new Unit("let"), new Unit(), new Unit("question", new Unit("dot"), new Unit()), new Unit("equal"),
						typeUnit, new Unit() });
		System.out.println(pat.toString());
		System.out.println(pat.toVoiceJavaPattern());
	}

}
