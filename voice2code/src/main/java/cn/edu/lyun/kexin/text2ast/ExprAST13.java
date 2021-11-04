package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;

// [expression]? variable _ index _
// Simplified version.
// getNameList()[15*15] not supported
// name[15] is supported
public class ExprAST13 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "exprssion"
		}

		String name = unitList.get(1).getKeyword();
		String index = unitList.get(3).getKeyword();
		return new ArrayAccessExpr(new NameExpr(name), new NameExpr(index));
	}
}
