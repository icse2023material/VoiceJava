package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import java.io.IOException;
import cn.edu.lyun.util.Pair;

import cn.edu.lyun.kexin.text2pattern.nfa.*;
import org.apache.commons.lang3.ArrayUtils;

public class RegexTest {

	public static void main(String[] args) {
		Unit Name = new Unit("plus", new Unit());

		Pattern importPat = new Pattern("import", "import static? [_]+ [dot [[_]+|star]]*",
				new Unit[] { new Unit("import"), new Unit("question", new Unit("static")), Name,
						new Unit("asterisk", new Unit("dot"), new Unit("or", Name, new Unit("star"))) });
		Regex regex = new Regex(importPat);
		regex.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		String text = "import java dot uitl dot star";
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
