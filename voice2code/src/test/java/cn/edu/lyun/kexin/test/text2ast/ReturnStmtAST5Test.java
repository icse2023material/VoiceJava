package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;

import com.github.javaparser.ast.*;

// return (int | byte | short | long | char | float | double | boolean | String) _
public class ReturnStmtAST5Test {
	public static void main(String[] args) {
		String[] textList = { "return int 50", "return string helloworld" };

		AST ast = new ReturnStmtAST5();

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			Node n = ast.generate(pattern);
			System.out.println(n.toString());
		}
	}

}
