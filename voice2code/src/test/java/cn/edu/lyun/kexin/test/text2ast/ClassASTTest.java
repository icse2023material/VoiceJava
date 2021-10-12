package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.ClassAST;
import cn.edu.lyun.kexin.text2ast.ImportAST;
import cn.edu.lyun.kexin.text2ast.InterfaceAST;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.*;

public class ClassASTTest {
	public static void main(String[] args) {
		String[] textList = { "define public class hello", "define public class hello extends world implements greetings" };
		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			System.out.println(pattern.getPattern());
			System.out.println(pattern.toVoiceJavaPattern());
			System.out.println(pattern.showInstance());

			ClassAST classAST = new ClassAST();
			Node node = classAST.generate(pattern);
			System.out.println(node);

		}
	}
}