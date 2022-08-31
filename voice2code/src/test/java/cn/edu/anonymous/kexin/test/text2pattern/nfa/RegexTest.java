package cn.edu.anonymous.kexin.test.text2pattern.nfa;

import java.io.IOException;

import cn.edu.anonymous.kexin.text2pattern.nfa.*;
import cn.edu.anonymous.kexin.text2pattern.pattern.*;
import cn.edu.anonymous.util.Pair;

public class RegexTest {

	public static void main(String[] args) {

		Unit Name = new Unit("plus", new Unit());

		// 定义类型
		Pattern typePat = new Pattern("typeExtends",
				"type (list of [_]+  | [_]+ [dot [_]+]? [with [_]+ [and [_]+]*]?) [extends [_]+]?",
				new Unit[] { new Unit("type"),
						new Unit("or", new Unit("normal", new Unit("list"), new Unit("normal", new Unit("of"), (Name))),
								new Unit(new Unit[] { Name, new Unit("question", new Unit("dot"), Name),
										new Unit("question", new Unit("with"),
												new Unit("normal", Name, new Unit("asterisk", new Unit("and"), Name))) })),
						new Unit("question", new Unit("extends"), Name) });

    Pattern packagePat = new Pattern("package", "define package [_]+ [dot [_]+]*",
            new Unit[] { new Unit("define"), new Unit("package"), Name, new Unit("asterisk", new Unit("dot"), Name) });
            Pattern expr20Pat = new Pattern("expr20Null", "expression? null", 
            new Unit[]{new Unit("question", new Unit("expression")), new Unit("null")});
     
            
		Regex regex = new Regex(expr20Pat);
		regex.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		String text = "null";
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
