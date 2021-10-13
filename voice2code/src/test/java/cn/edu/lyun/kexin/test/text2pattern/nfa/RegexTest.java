package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import java.io.IOException;
import cn.edu.lyun.util.Pair;

import cn.edu.lyun.kexin.text2pattern.nfa.*;

public class RegexTest {

	public static void main(String[] args) {

		Pattern let1Pat = new Pattern("let _ [dot _]? equal call _ ", new Unit[] { new Unit("let"), new Unit(),
				new Unit("question", new Unit("dot"), new Unit()), new Unit("equal"), new Unit("call"), new Unit() });
		Pattern let2Pat = new Pattern("let _ [dot _]? equal _ [call _]+",
				new Unit[] { new Unit("let"), new Unit(), new Unit("question", new Unit("dot"), new Unit()), new Unit("equal"),
						new Unit(), new Unit("plus", new Unit("call"), new Unit()) });

		Regex regex = new Regex(let2Pat);
		regex.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Pattern result = regex.isMatch("define package hello dot world");
		// Pattern result = regex.isMatch("define public int variable count");
		// String text = "import cn dot edu dot lyun dot kexin dot star";
		String text = "let world equal hello call world";
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
