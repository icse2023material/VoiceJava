package cn.edu.anonymous.kexin.test.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2ast.*;
import cn.edu.anonymous.kexin.text2pattern.nfa.RegexSet;
import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.PatternSet;

public class ForASTTest {
	public static void main(String[] args) {
		String[] textList = { "define enhanced for" };

		ForAST forAST = new ForAST();

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			Node n = forAST.generate(pattern);
			System.out.println(n.toString());
		}
	}

}
