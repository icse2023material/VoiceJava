package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.BreakStmt;

import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public class BreakAST implements AST {
	public Node generate(Pattern pattern) {
		return new BreakStmt();
	}

}
