package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.*;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;

// let _ [dot _]? equal (int | byte | short | long | char | float | double | boolean | String) _ 
public class LetStmtAST5 implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "let"
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "equal");
		List<Unit> first = pair.getFirst();
		AssignExpr assignExpr = new AssignExpr();

		Expression left = new FieldAccessAST().generate(first);
		assignExpr.setTarget(left);

		List<Unit> second = pair.getSecond();
		Unit unit = second.remove(0);
		if (PrimitiveTypeSet.isTypeWord(unit.getKeyword())) {
			Expression right = null;
			String value = second.get(0).getKeyword();
			switch (unit.getKeyword()) {
				case "int":
					right = new IntegerLiteralExpr(value);
					break;
				case "string":
					right = new StringLiteralExpr(value);
					break;
				case "boolean":
					right = new BooleanLiteralExpr(Boolean.parseBoolean(value));
					break;
				case "double":
					right = new DoubleLiteralExpr(value);
					break;
				case "long":
					right = new LongLiteralExpr(value);
					break;
				case "char":
					right = new CharLiteralExpr(value);
					break;
				case "byte":
					break;
				case "float":
					break;
				case "short":
					break;
			}
			assignExpr.setValue(right);
			return assignExpr;
		} else {
			System.out.println("Not primitive type");
			return null;
		}
	}

}
