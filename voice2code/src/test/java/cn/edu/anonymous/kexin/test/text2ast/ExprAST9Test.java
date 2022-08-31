package cn.edu.anonymous.kexin.test.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2ast.*;
import cn.edu.anonymous.kexin.text2pattern.nfa.RegexSet;
import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.PatternSet;

// [expression]? minus minus _
public class ExprAST9Test {
	public static void main(String[] args) {
		String[] textList = { "minus minus 5", "expression minus minus 6" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST10();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
