package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import java.util.*;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

// [expression]? call _
public class ExprAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "exprssion"
		}
		if (unitList.get(0).getKeyword().equals("call")) {
			unitList.remove(0);
			MethodCallExpr methodCallExpr = new MethodCallExpr();
			methodCallExpr.setName(new SimpleName(unitList.get(0).getKeyword()));
			return methodCallExpr;
		} else {
			System.out.println("Should be call");
			return null;
		}
	}
}
