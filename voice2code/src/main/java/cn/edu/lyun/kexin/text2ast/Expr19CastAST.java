package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.CastExpr;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public class Expr19CastAST implements AST {
	public Node generate(Pattern pattern) {
    return new CastExpr();
	}
}
