package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

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
