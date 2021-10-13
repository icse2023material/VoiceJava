package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.*;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

// let _ [dot _]? equal [variable]? _
public class LetStmtAST4 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "let"
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "equal");
		List<Unit> first = pair.getFirst();
		AssignExpr assignExpr = new AssignExpr();

		Expression left = new FieldAccessAST().generate(first);
		assignExpr.setTarget(left);

		List<Unit> second = pair.getSecond();
		Unit unit = second.get(0);
		if (unit.getKeyword().equals("variable")) {
			second.remove(0);
		}
		Expression right = new NameExpr(new SimpleName(second.get(0).getKeyword()));
		assignExpr.setValue(right);
		return assignExpr;
	}

}
