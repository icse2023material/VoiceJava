package cn.edu.lyun.kexin.test.text2pattern.nfa;

import cn.edu.lyun.kexin.text2pattern.pattern.*;
import java.io.IOException;
import cn.edu.lyun.util.Pair;

import cn.edu.lyun.kexin.text2pattern.nfa.*;

public class RegexTest {

	public static void main(String[] args) {
		Pattern packagePat = new Pattern("define package _ [dot _]*", new Unit[] { new Unit("define"), new Unit("package"),
				new Unit(), new Unit("asterisk", new Unit("dot"), new Unit()) });
		Pattern importPat = new Pattern("import static? _ [dot [_|star]]*",
				new Unit[] { new Unit("import"), new Unit("question", new Unit("static")), new Unit(),
						new Unit("asterisk", new Unit("dot"), new Unit("or", new Unit(), new Unit("star"))) });

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

		Unit classModifier = new Unit("or", new Unit("annotation"),
				new Unit("or", new Unit("public"),
						new Unit("or", new Unit("protected"),
								new Unit("or", new Unit("private"), new Unit("or", new Unit("abstract"),
										new Unit("or", new Unit("static"), new Unit("or", new Unit("final"), new Unit("strictfp"))))))));

		Pattern interfacePat = new Pattern("define [public|private] interface _",
				new Unit[] { new Unit("define"), classModifier, new Unit("interface"), new Unit() });
		Regex regex = new Regex(interfacePat);
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
		String text = "define public interface hello";
		Pair<Boolean, Pattern> result = regex.isMatch(text);
		if (result.getFirst()) {
			System.out.println("Matched:");
			System.out.println(result.getSecond().getPattern());
			System.out.println(result.getSecond().toVoiceJavaPattern());
			System.out.println(result.getSecond().showInstance());
		} else {
			System.out.println("Match fail!");
			System.out.println(text);
			System.out.println(result.getSecond().getPattern());
		}
	}
}
