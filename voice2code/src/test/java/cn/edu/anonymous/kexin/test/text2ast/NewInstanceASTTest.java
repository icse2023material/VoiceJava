package cn.edu.anonymous.kexin.test.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2ast.*;
import cn.edu.anonymous.kexin.text2pattern.nfa.RegexSet;
import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.PatternSet;

public class NewInstanceASTTest {
	public static void main(String[] args) {
		String[] textList = { "new instance Puppy", "new instance HashMap dot Entry" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			// System.out.println(pattern.getPattern());
			System.out.println(pattern.toVoiceJavaPattern());
			System.out.println(pattern.showInstance());

			NewInstanceAST newInstanceAST = new NewInstanceAST();
			Node node = newInstanceAST.generate(pattern);
			System.out.println(node);

		}
	}
}
