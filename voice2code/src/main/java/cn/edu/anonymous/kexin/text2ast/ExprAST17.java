package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.LambdaExpr;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;

// expression? lambda expression
public class ExprAST17 implements AST {
	public Node generate(Pattern pattern) {
    LambdaExpr lambdaExpr = new LambdaExpr();
    lambdaExpr.setEnclosingParameters(true);
    return lambdaExpr;
	}
}
