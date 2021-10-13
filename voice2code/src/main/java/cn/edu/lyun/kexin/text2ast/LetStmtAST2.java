package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.*;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

// let _ [dot _]? equal _ [call _]+
public class LetStmtAST2 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "let"
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "equal");
		List<Unit> first = pair.getFirst();
		AssignExpr assignExpr = new AssignExpr();

		Expression fieldAccess = new FieldAccessAST().generate(first);
		assignExpr.setTarget(fieldAccess);

		List<Unit> second = pair.getSecond();
		MethodCallExpr methodCallExpr = generateMethoCallExpr(second);
		assignExpr.setValue(methodCallExpr);
		return assignExpr;
	}

	public MethodCallExpr generateMethoCallExpr(List<Unit> units) {
		units = new ListHelper().removeCall(units);
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
