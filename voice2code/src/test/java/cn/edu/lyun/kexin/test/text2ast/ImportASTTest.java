package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import cn.edu.lyun.kexin.text2ast.*;
import com.github.javaparser.ast.*;

public class ImportASTTest {
	public static void main(String[] args) {
		String[] textList = { "import static cn dot edu dot lyun", "import cn dot edu dot lyun",
				"import cn dot edu dot lyun dot star", "import static cn dot edu dot lyun dot star", };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			// System.out.println(pattern.getPattern());
			// System.out.println(pattern.toVoiceJavaPattern());
			System.out.println(pattern.showInstance());

			ImportAST importAST = new ImportAST();
			Node node = importAST.generate(pattern);
			System.out.println(node);
		}
	}

}
