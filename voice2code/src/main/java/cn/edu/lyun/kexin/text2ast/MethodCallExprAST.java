package cn.edu.lyun.kexin.text2ast;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.*;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

public class MethodCallExprAST {
	public static MethodCallExpr generateMethoCallExpr(List<Unit> units) {
		units = new ListHelper().removeCall(units);
		if (units.size() == 1) {
			return new MethodCallExpr(units.get(0).getKeyword());
		} else {
			Unit name = units.remove(0);
			MethodCallExpr methodCallExpr = null;
			Expression nameExpr = new NameExpr(new SimpleName(name.getKeyword()));
			for (Unit unit : units) {
				if (methodCallExpr != null) {
					methodCallExpr = new MethodCallExpr(methodCallExpr, new SimpleName(unit.getKeyword()));
				} else {
					methodCallExpr = new MethodCallExpr(nameExpr, new SimpleName(unit.getKeyword()));
				}
			}
			return methodCallExpr;
		}
	}
}
