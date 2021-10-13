package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.ContinueStmt;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public class ContinueAST implements AST {
	public Node generate(Pattern pattern) {
		return new ContinueStmt();
	}

}
