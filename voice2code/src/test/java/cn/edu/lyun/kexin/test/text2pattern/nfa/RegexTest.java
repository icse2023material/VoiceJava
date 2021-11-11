package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import java.io.IOException;
import cn.edu.lyun.util.Pair;

import cn.edu.lyun.kexin.text2pattern.nfa.*;

public class RegexTest {

	public static void main(String[] args) {
		Unit opUnit = new Unit("or", new Unit("plus"), new Unit("or", new Unit("minus"),
				new Unit("or", new Unit("times"), new Unit("or", new Unit("divide"), new Unit("mod")))));
		Unit compareUnit = new Unit("or", new Unit("normal", new Unit("less"), new Unit("than")),
				new Unit("or", new Unit("normal", new Unit("less"), new Unit("equal")),
						new Unit("or", new Unit("normal", new Unit("greater"), new Unit("than")),
								new Unit("or", new Unit("normal", new Unit("greater"), new Unit("equal")),
										new Unit("or", new Unit("normal", new Unit("double"), new Unit("equal")),
												new Unit("or", new Unit("and"), new Unit("normal", new Unit("double"), new Unit("and"))))))));

		Pattern expr10Pat = new Pattern("expr10", "[expression]? subexpression (op | compare) subexpression",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("subexpression"),
						new Unit("or", opUnit, compareUnit), new Unit("subexpression") });
		Pattern typePat2 = new Pattern("typeVariable", "type (_ list | _ [dot _]? [with _+]?) variable _",
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
		Pattern let5Pat = new Pattern("let5",
				"let _ [dot _]? equal (int | byte | short | long | char | float | double | boolean | string) _ ",
				new Unit[] { new Unit("let"), new Unit(), new Unit("question", new Unit("dot"), new Unit()), new Unit("equal"),
						typeUnit, new Unit() });
		Regex regex = new Regex(let5Pat);
		regex.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		String text = "let x equal float 2";
		Pair<Boolean, Pattern> result = regex.isMatch(text);
		if (result.getFirst()) {
			System.out.println("Matched:");
			System.out.println(result.getSecond().getPattern());
			System.out.println(result.getSecond().toVoiceJavaPattern());
			System.out.println(result.getSecond().showInstance());
			for (Unit unit : result.getSecond().getUnits()) {
				System.out.print(unit);
			}
		} else {
			System.out.println("Match fail!");
			System.out.println(text);
			System.out.println(result.getSecond().getPattern());
		}
	}
}
