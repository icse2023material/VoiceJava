package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr.Operator;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;

// expression? not expression
public class ExprAST16 implements AST {
	public Node generate(Pattern pattern) {
    UnaryExpr unaryExpr = new UnaryExpr();
    unaryExpr.setOperator(Operator.LOGICAL_COMPLEMENT);
    return unaryExpr;
	}
}
