package cn.edu.anonymous.kexin.test.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2ast.*;
import cn.edu.anonymous.kexin.text2pattern.nfa.RegexSet;
import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.PatternSet;

// [expression]? subexpression (op | compare) subexpression
public class ExprAST10Test {
	public static void main(String[] args) {
		String[] textList = { "expression subexpression plus subexpression", "subexpression minus subexpression",
				"subexpression times subexpression", "subexpression divide subexpression", "subexpression mod subexpression",
				"subexpression less than subexpression", "subexpression less equal subexpression",
				"subexpression greater than subexpression", "subexpression greater equal subexpression",
				"subexpression double equal subexpression", "subexpression double and subexpression" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST10();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
