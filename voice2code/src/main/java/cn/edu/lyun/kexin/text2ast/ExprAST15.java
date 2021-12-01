package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

// [expression]? null
public class ExprAST15 implements AST {
	public Node generate(Pattern pattern) {
		return new NullLiteralExpr();
	}
}
