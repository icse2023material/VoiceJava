package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.EnclosedExpr;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

// subexpression
public class SubExpressionAST implements AST {
	public Node generate(Pattern pattern) {
		return new EnclosedExpr();
	}

}
