package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.BinaryExpr;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

import java.util.*;

// [expression]? subexpression (op | compare) subexpression
public class ExprAST10 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}

		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}

		String operatorStr = BinaryOperatorAST.getOperatorStr(unitList);
		BinaryExpr binaryExpr = new BinaryExpr();
		binaryExpr.setOperator(BinaryOperatorAST.generateOperator(operatorStr));

		return binaryExpr;
	}

}
