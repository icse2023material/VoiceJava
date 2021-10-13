package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.*;
import cn.edu.lyun.util.ListHelper;

public class FieldAccessAST {

	public Expression generate(List<Unit> units) {

		if (units.size() == 1) {
			NameExpr nameExpr = new NameExpr(new SimpleName(units.get(0).getKeyword()));
			return nameExpr;
		} else {
			units = new ListHelper().removeDot(units);
			FieldAccessExpr fieldAccessExpr = generateFieldAccessExpr(units);
			return fieldAccessExpr;
		}
	}

	public FieldAccessExpr generateFieldAccessExpr(List<Unit> units) {
		Unit first = units.remove(0);
		FieldAccessExpr fieldAccessExpr = null;
		for (Unit unit : units) {
			if (fieldAccessExpr != null) {
				fieldAccessExpr = new FieldAccessExpr(fieldAccessExpr, unit.getKeyword());
			} else {
				fieldAccessExpr = new FieldAccessExpr(new NameExpr(new SimpleName(first.getKeyword())), unit.getKeyword());
			}
		}
		return fieldAccessExpr;
	}
}
