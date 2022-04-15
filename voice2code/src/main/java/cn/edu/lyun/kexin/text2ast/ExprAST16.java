package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr.Operator;

// expression? not expression
public class ExprAST16 implements AST {
	public Node generate(Pattern pattern) {
    UnaryExpr unaryExpr = new UnaryExpr();
    unaryExpr.setOperator(Operator.LOGICAL_COMPLEMENT);
    return unaryExpr;
	}
}
