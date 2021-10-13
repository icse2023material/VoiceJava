package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.*;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;

// let _ [dot _]? equal call _ 
public class LetStmtAST implements AST {
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
		if (second.get(0).getKeyword().equals("call")) {
			second.remove(0);
			MethodCallExpr methodCallExpr = new MethodCallExpr();
			methodCallExpr.setName(new SimpleName(second.get(0).getKeyword()));
			assignExpr.setValue(methodCallExpr);
		} else {
			System.out.println("Should be call");
		}
		return assignExpr;
	}

}
