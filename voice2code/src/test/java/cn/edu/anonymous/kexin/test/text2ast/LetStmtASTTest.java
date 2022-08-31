package cn.edu.anonymous.kexin.test.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2ast.*;
import cn.edu.anonymous.kexin.text2pattern.nfa.RegexSet;
import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.PatternSet;

public class LetStmtASTTest {

	public static void main(String[] args) {
		// let _ [dot _]? equal [expression]?
		String[] textList = { "let world equal expression", "let hello dot world equal expression" };

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());

			AST stmt = new LetStmtAST();
			Node node = stmt.generate(pattern);
			System.out.println(node);
		}
	}
}
