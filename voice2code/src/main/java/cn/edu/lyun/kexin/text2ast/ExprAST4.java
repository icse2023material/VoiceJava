package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

// [expression]? [variable]? _
public class ExprAST4 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}
		Unit unit = unitList.get(0);
		if (unit.getKeyword().equals("variable")) {
			unitList.remove(0);
		}
		return new NameExpr(new SimpleName(unitList.get(0).getKeyword()));
	}
}
