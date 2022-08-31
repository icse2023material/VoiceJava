package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import java.util.*;

import com.github.javaparser.ast.expr.MethodCallExpr;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

// [expression]? _ [call _]+
public class ExprAST2 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "exprssion"
		}
		MethodCallExpr methodCallExpr = MethodCallExprAST.generateMethoCallExpr(unitList);
		return methodCallExpr;
	}
}
