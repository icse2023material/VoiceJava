package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

// [expression]? _ (op | compare) subexpression
public class ExprAST11 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}

		Unit leftUnit = unitList.remove(0);
		Expression leftExpr = new NameExpr(new SimpleName(leftUnit.getKeyword()));

		String operatorStr = BinaryOperatorAST.getOperatorStr(unitList);
		BinaryExpr binaryExpr = new BinaryExpr();
		binaryExpr.setLeft(leftExpr);
		binaryExpr.setOperator(BinaryOperatorAST.generateOperator(operatorStr));

		return binaryExpr;
	}

}
