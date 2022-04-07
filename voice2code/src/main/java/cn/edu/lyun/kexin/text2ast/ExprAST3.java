package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.NullLiteralExpr;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

// [expression]? _ [dot _]*
public class ExprAST3 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}
    // case: expression null
    if(unitList.size()==1 && unitList.get(0).getKeyword().equals("null")){
      return new NullLiteralExpr();
    } else {
		  return new FieldAccessAST().generate(unitList);
    }
	}
}
