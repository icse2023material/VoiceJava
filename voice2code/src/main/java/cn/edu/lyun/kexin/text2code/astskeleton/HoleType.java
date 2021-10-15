package cn.edu.lyun.kexin.text2code.astskeleton;

import com.github.javaparser.ast.body.MethodDeclaration;

public enum HoleType {
	Undefined, Wrapper, CompilationUnit, PackageDeclaration, ImportDeclaration, TypeDeclaration, FieldDeclaration,
	MethodDeclaration, VariableDeclarator, BodyDeclaration, Expression, TypeExtends,
}
