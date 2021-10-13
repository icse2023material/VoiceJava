package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;

import com.github.javaparser.ast.*;

public class BreakContinueASTTest {
	public static void main(String[] args) {
		// String[] textList = { "break" };
		// BreakAST ast = new BreakAST();
		String[] textList = { "continue" };
		ContinueAST ast = new ContinueAST();
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			Node n = ast.generate(pattern);
			System.out.println(n.toString());
		}
	}
}
