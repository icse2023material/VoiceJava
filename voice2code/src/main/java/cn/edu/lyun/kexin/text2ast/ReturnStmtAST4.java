package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

// return [variable]? _
public class ReturnStmtAST4 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "return"
		Unit unit = unitList.get(0);
		if (unit.getKeyword().equals("variable")) {
			unitList.remove(0);
		}
		Expression expr = new NameExpr(new SimpleName(unitList.get(0).getKeyword()));
		return new ReturnStmt(expr);
	}

}
