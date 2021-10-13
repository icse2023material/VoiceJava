package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

// [expression]? _ (op | compare) subexpression
public class ExprAST11Test {
	public static void main(String[] args) {
		String[] textList = { "expression 5 plus subexpression", "5 minus subexpression", "5 times subexpression",
				"5 divide subexpression", "5 mod subexpression", "5 less than subexpression", "5 less equal subexpression",
				"5 greater than subexpression", "5 greater equal subexpression", "5 double equal subexpression",
				"5 double and subexpression" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST11();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
