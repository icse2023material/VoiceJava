package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

// [expression]? plus plus _
public class ExprAST8Test {
	public static void main(String[] args) {
		String[] textList = { "plus plus 5", "expression plus plus 6" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST8();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
