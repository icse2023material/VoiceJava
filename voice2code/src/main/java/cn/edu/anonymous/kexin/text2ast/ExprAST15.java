package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.ConditionalExpr;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;

// conditional expression
public class ExprAST15 implements AST {
	public Node generate(Pattern pattern) {
    return new ConditionalExpr();
	}
}
