package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

// TODO: constructor command "define constructor"
// 1. we need the class name to generate full constructor
// 2. constructor may have arguments
public class ConstructorAST implements AST {
	public Node generate(Pattern pattern) {
		ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration();
		return constructorDeclaration;
	}
}
