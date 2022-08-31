package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.ReturnStmt;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;

// return [expression]?
public class ReturnStmtAST implements AST {
	public Node generate(Pattern pattern) {
		return new ReturnStmt();
	}

}
