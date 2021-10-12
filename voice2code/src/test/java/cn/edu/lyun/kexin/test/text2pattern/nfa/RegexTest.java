package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import java.io.IOException;
import cn.edu.lyun.kexin.text2pattern.nfa.*;

public class RegexTest {

	public static void main(String[] args) {
		Pattern packagePat = new Pattern("define package _ [dot _]*", new Unit[] { new Unit("define"), new Unit("package"),
				new Unit(), new Unit("asterisk", new Unit("dot"), new Unit()) });
		// Pattern importPat = new Pattern("import static? _ [dot [_|star]]*",
		// new Unit[] { new Unit("import"), new Unit("question", new Unit("static")),
		// new Unit(),
		// new Unit("asterisk", new Unit("dot"), new Unit("or", new Unit(), new
		// Unit("star"))) });

		Unit fieldModifier = new Unit("or", new Unit("annotation"),
				new Unit("or", new Unit("public"),
						new Unit("or", new Unit("protected"), new Unit("or", new Unit("private"), new Unit("or", new Unit("static"),
								new Unit("or", new Unit("final"), new Unit("or", new Unit("transient"), new Unit("volatile"))))))));
		Pattern fieldPat = new Pattern(
				"define [Annotation|public|protected|private|static|final|transient|volatile]* (_ list | _ [dot _]? [with _+]?) variable _ ",
				new Unit[] { new Unit("define"), new Unit("asterisk", fieldModifier),
						new Unit("or", new Unit("normal", new Unit(), new Unit("list")),
								new Unit(new Unit[] { new Unit(), new Unit("question", new Unit("dot"), new Unit()),
										new Unit("question", new Unit("with"), new Unit("plus", new Unit())) })),
						new Unit("variable"), new Unit() });

		Regex regex = new Regex(fieldPat);
		regex.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng regex.dot -o regex.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean result = regex.isMatch("define public int variable count");
		System.out.println(result);
	}
}
