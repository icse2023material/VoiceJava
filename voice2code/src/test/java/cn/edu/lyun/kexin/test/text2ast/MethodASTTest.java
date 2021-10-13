package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.MethodAST;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

public class MethodASTTest {

	public static void main(String[] args) {
		String[] textList = { "define public function hello", "define private function infer",
				"define public function request throws exception" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			// System.out.println(pattern.getPattern());
			// System.out.println(pattern.toVoiceJavaPattern());
			System.out.println(pattern.showInstance());

			MethodAST method = new MethodAST();
			Node node = method.generate(pattern);
			System.out.println(node);
		}
	}
}
