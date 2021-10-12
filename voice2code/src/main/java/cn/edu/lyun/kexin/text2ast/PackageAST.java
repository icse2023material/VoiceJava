package cn.edu.lyun.kexin.text2ast;

import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import java.util.ArrayList;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.Name;

public class PackageAST implements AST {

	/*
	 * text sample: 1. define package hello 2. define package hello dot world 3.
	 * define package hello dot star 4. define package hello world
	 */
	public Node generate(String text) {
		// PatternSet patSet = new PatternSet();
		// ArrayList<Pattern> patList = patSet.getPatternSet();
		// Pattern packagePattern = patList.get(0);

		Name qualifier = new Name("world");
		Name name = new Name(qualifier, "hello");
		PackageDeclaration packageDeclaration = new PackageDeclaration(name);
		return packageDeclaration;
	}
}
