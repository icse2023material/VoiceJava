package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

public class LetStmtAST3Test {

	public static void main(String[] args) {
		// let _ [dot _]? equal _ [dot _]*
		String[] textList = { "let world equal hello", "let hello dot world equal hello dot world",
				"let hello dot world equal hello dot world dot greeting" };

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());

			AST stmt = new LetStmtAST3();
			Node node = stmt.generate(pattern);
			System.out.println(node);
		}
	}
}
