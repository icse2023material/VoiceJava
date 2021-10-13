package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

public class LetStmtASTTest {

	public static void main(String[] args) {
		String[] textList = { "let hello equal call world", "let hello dot world equal call world" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());

			LetStmtAST stmt = new LetStmtAST();
			Node node = stmt.generate(pattern);
			System.out.println(node);
		}
	}
}
