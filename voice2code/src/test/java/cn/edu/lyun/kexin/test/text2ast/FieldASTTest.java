package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.FieldAST;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;

import com.github.javaparser.ast.*;

public class FieldASTTest {
	public static void main(String[] args) {
		String[] textList = { "define public int variable count", "define int variable count",
				"define public string list variable count", "define public hello dot world variable greeting",
				"define public hello dot world with a b variable greeting", "define public hello with a b variable greeting" };

		FieldAST fieldAST = new FieldAST();

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			Node n = fieldAST.generate(pattern);
			System.out.println(n.toString());
		}
	}

}
