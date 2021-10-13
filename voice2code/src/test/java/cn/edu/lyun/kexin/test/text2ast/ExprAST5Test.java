package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

// [expression]? (int | byte | short | long | char | float | double | boolean | String) _
public class ExprAST5Test {
	public static void main(String[] args) {
		String[] textList = { "expression int 5", "string hello" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST5();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
