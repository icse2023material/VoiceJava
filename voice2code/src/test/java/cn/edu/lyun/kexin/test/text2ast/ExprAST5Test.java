package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

// [expression]? (int | byte | short | long | char | float | double | boolean | String) _
public class ExprAST5Test {
	public static void main(String[] args) {
		// String[] textList = { "expression int five", "string hello" };
		String[] textList = { "expression int five", "expression int fifty five",
				"expression int one hundred two", "int zero" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST5();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}

// or, design a new language for spelling numbers
// 123 as one two three
// 870000 as eight seven and four zero, eight seven zero zero zero zero
// 3.1415926: three point one four one five nine two six
// 1000000000: one and nine zero
// 1000000021: one and seven zero two one
