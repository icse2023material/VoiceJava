package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;

import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

import java.util.*;

public class BinaryOperatorAST {
	public static String getOperatorStr(List<Unit> units) {
		String operatorStr = units.remove(0).getKeyword();
		Unit anOtherOperatorUnit = units.get(0);
		String anOtherOperatorStr = anOtherOperatorUnit.getKeyword();
		List<String> opList = new ArrayList<String>(Arrays.asList(new String[] { "than", "equal", "and", "or" }));
		if (opList.contains(anOtherOperatorStr)) {
			operatorStr += " " + anOtherOperatorStr;
			units.remove(0);
		}
		return operatorStr;
	}

	public static Operator generateOperator(String operatorStr) {
		switch (operatorStr) {
		case "plus":
			return BinaryExpr.Operator.PLUS;
		case "minus":
			return BinaryExpr.Operator.MINUS;
		case "times":
			return BinaryExpr.Operator.MULTIPLY;
		case "divide":
			return BinaryExpr.Operator.DIVIDE;
		case "mod":
			return BinaryExpr.Operator.REMAINDER;
		case "less than":
			return BinaryExpr.Operator.LESS;
		case "less equal":
			return BinaryExpr.Operator.LESS_EQUALS;
		case "greater than":
			return BinaryExpr.Operator.GREATER;
		case "greater equal":
			return BinaryExpr.Operator.GREATER_EQUALS;
		case "double equal":
			return BinaryExpr.Operator.EQUALS;
		case "double and":
			return BinaryExpr.Operator.AND;
    case "double or":
      return BinaryExpr.Operator.OR;
    case "not equal":
      return BinaryExpr.Operator.NOT_EQUALS;
		}
		return null;
	}
}
