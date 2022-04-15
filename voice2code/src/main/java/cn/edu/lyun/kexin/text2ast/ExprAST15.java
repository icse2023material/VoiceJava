package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import com.github.javaparser.ast.expr.ConditionalExpr;

// conditional expression
public class ExprAST15 implements AST {
	public Node generate(Pattern pattern) {
    return new ConditionalExpr();
	}
}
