package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

import java.util.*;

// [expression]? string hello world
public class ExprAST14 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}
		if (unitList.get(0).getKeyword().equals("string")) {
			unitList.remove(0); // remove "string"
		}
		String value = PrimitiveTypeAST.generateStrFromUnits("string", unitList);
		return new StringLiteralExpr(value.strip());
	}
}
