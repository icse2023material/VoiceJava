package cn.edu.lyun.kexin.test.text2ast;

import cn.edu.lyun.kexin.text2ast.PackageAST;
import com.github.javaparser.ast.*;

public class PackageASTTest {
	public static void main(String[] args) {
		PackageAST packageAST = new PackageAST();
		String text = "define package hello";
		Node n = packageAST.generate(text);
		System.out.println(n);
	}

}
