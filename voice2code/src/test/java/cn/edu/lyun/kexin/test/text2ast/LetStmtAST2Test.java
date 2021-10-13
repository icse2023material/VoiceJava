package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

public class LetStmtAST2Test {

	public static void main(String[] args) {
		// let _ [dot _]? equal _ [call _]+
		String[] textList = { "let world equal hello call world",
				"let hello dot world equal hello call world call greeting" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());

			AST stmt = new LetStmtAST2();
			Node node = stmt.generate(pattern);
			System.out.println(node);
		}
	}
}