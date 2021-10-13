package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.*;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;

import com.github.javaparser.ast.*;

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
