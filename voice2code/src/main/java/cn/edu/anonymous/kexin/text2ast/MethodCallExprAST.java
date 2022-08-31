package cn.edu.anonymous.kexin.text2ast;

import java.util.*;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;

import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;
import cn.edu.anonymous.util.*;

public class MethodCallExprAST {
	public static MethodCallExpr generateMethoCallExpr(List<Unit> units) {
		if (units.size() == 2) { // "call name"
			return new MethodCallExpr(units.get(1).getKeyword());
		}
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitListKeepAtSecond(units, "call");
		List<Unit> dots = pair.getFirst();
		Expression fieldExpr = new FieldAccessAST().generate(dots);
		List<Unit> calls = pair.getSecond();

		calls = new ListHelper().removeCall(calls);
		MethodCallExpr methodCallExpr = null;
		for (Unit unit : units) {
			if (methodCallExpr != null) {
				methodCallExpr = new MethodCallExpr(methodCallExpr, new SimpleName(unit.getKeyword()));
			} else {
				methodCallExpr = new MethodCallExpr(fieldExpr, new SimpleName(unit.getKeyword()));
			}
		}
		return methodCallExpr;
	}
}
