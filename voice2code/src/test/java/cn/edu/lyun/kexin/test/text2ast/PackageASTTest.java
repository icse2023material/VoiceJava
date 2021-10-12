package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.PackageAST;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;

import com.github.javaparser.ast.*;

public class PackageASTTest {
	public static void main(String[] args) {
		String[] textList = { "define package hello", "define package hello dot world",
				"define package hello dot world dot star" };

		PackageAST packageAST = new PackageAST();

		for (String text : textList) {
			Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
			Node n = packageAST.generate(pattern);
			System.out.println(n.toString());
		}
	}

}
