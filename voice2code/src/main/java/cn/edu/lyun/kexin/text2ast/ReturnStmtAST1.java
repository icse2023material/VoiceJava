package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;

// return call _
public class ReturnStmtAST1 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "return"
		MethodCallExpr methodCallExpr = MethodCallExprAST.generateMethoCallExpr(unitList);
		return new ReturnStmt(methodCallExpr);
	}

}
