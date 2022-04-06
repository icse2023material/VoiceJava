package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import com.github.javaparser.ast.stmt.ReturnStmt;

// return [expression]?
public class ReturnStmtAST implements AST {
	public Node generate(Pattern pattern) {
		return new ReturnStmt();
	}

}
