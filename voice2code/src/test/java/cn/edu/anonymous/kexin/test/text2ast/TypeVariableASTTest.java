package cn.edu.anonymous.kexin.test.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2ast.*;
import cn.edu.anonymous.kexin.text2pattern.nfa.RegexSet;
import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.PatternSet;

public class TypeVariableASTTest {
	public static void main(String[] args) {
		String[] textList = { "type int variable count", "type int list variable countArray",
				"type Nodelist with Statement variable nodelist" };

		AST ast = new TypeVariableAST();

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			Node n = ast.generate(pattern);
			System.out.println(n.toString());
		}
	}

}
