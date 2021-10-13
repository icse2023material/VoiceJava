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

		Pattern expr10Pat = new Pattern("[expression]? subexpression (op | compare) subexpression",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("subexpression"),
						new Unit("or", opUnit, compareUnit), new Unit("subexpression") });
		Regex regex = new Regex(expr10Pat);
		regex.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		String text = "expression subexpression plus subexpression";
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
