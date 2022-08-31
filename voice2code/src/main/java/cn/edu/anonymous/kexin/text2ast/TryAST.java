package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.TryStmt;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;

public class TryAST implements AST {
	public Node generate(Pattern pattern) {
		return new TryStmt();
	}

}
