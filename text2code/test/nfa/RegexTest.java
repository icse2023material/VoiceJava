package test.nfa;

import lyun.voice2code.pattern.*;

import java.io.IOException;

import lyun.voice2code.nfa.*;

public class RegexTest {

	public static void main(String[] args) {
		Pattern packagePat = new Pattern("define package _ [dot _]*", new Unit[] { new Unit("define"), new Unit("package"),
				new Unit(), new Unit("asterisk", new Unit("dot"), new Unit()) });
		Pattern importPat = new Pattern("import static? _ [dot [_|star]]*",
				new Unit[] { new Unit("import"), new Unit("question", new Unit("static")), new Unit(),
						new Unit("asterisk", new Unit("dot"), new Unit("or", new Unit(), new Unit("star"))) });

		Regex regex = new Regex(importPat);
		regex.writeDotFile();

		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
