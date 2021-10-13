package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

// [expression]? _ minus minus
public class ExprAST7Test {
	public static void main(String[] args) {
		String[] textList = { "5 minus minus", "expression 6 minus minus" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST7();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
