package cn.edu.anonymous.kexin.test.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2ast.FieldAST;
import cn.edu.anonymous.kexin.text2pattern.nfa.RegexSet;
import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.PatternSet;

public class FieldASTTest {
	public static void main(String[] args) {
		String[] textList = { "define public int variable count", "define int variable count",
				"define public list of string variable count", "define public hello dot world variable greeting",
				"define public hello dot world with a b variable greeting", "define public hello with a b variable greeting",
				"define list of hello variable greeting" };

		FieldAST fieldAST = new FieldAST();

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			Node n = fieldAST.generate(pattern);
			System.out.println(n.toString());
		}
	}

}
