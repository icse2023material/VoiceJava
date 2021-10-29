package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

// [expression]? _ [call _]+
public class ExprAST2Test {
	public static void main(String[] args) {
		String[] textList = { "expression hello call world", "hello call world call greeting",
				"System dot out call println" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST2();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
