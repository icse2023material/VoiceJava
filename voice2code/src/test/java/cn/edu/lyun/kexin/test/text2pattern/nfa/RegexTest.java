package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import java.io.IOException;
import cn.edu.lyun.util.Pair;

import cn.edu.lyun.kexin.text2pattern.nfa.*;

public class RegexTest {

	public static void main(String[] args) {

		Unit Name = new Unit("plus", new Unit());

		Pattern packagePat = new Pattern("package", "define package [_]+ [dot [_]+]*",
				new Unit[] { new Unit("define"), new Unit("package"), Name, new Unit("asterisk", new Unit("dot"), Name) });

		Regex regex = new Regex(packagePat);
		regex.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		String text = "define package org dot hello world dot star";
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
