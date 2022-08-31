package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

import java.util.*;

// [expression]? (int | byte | short | long | char | float | double | boolean | String) _
public class ExprAST5 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}
		return PrimitiveTypeAST.generatePrimiviteExpr(unitList);
	}
}
