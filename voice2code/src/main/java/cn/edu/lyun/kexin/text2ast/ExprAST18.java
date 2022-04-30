package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.*;
import java.util.*;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.NameExpr;

// expression? Name instance of ReferenceType
public class ExprAST18 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		if (unitList.get(0).getKeyword().equals("expression")) {
			unitList.remove(0); // remove "expression"
		}
 		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "instance");
		List<Unit> names = pair.getFirst();
    NameExpr nameExpr = new NameExpr(names.get(0).getKeyword());
    InstanceOfExpr instanceOfExpr = new InstanceOfExpr();
    instanceOfExpr.setExpression(nameExpr);
    return instanceOfExpr;
	}
}
