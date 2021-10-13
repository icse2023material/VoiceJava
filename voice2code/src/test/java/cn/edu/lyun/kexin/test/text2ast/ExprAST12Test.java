package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import com.github.javaparser.ast.*;

// [expression]? _ (op | compare) _
public class ExprAST12Test {
	public static void main(String[] args) {
		String[] textList = { "expression 5 plus 3", "5 minus 3", "5 times 3", "5 divide 3", "5 mod 3", "5 less than 3",
				"5 less equal 3", "5 greater than 3", "5 greater equal 3", "5 double equal 3", "5 double and 3" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.showInstance());
			AST ast = new ExprAST12();
			Node node = ast.generate(pattern);
			System.out.println(node);
		}
	}
}
