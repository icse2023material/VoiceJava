package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.expr.Expression;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class PrimitiveTypeAST {
	public static Expression generatePrimiviteExpr(List<Unit> units) {
		Unit unit = units.remove(0);
		if (PrimitiveTypeSet.isTypeWord(unit.getKeyword())) {
			String value = units.get(0).getKeyword();
			switch (unit.getKeyword()) {
				case "int":
					return new IntegerLiteralExpr(value);
				case "string":
					return new StringLiteralExpr(value);
				case "boolean":
					return new BooleanLiteralExpr(Boolean.parseBoolean(value));
				case "double":
					return new DoubleLiteralExpr(value);
				case "long":
					return new LongLiteralExpr(value);
				case "char":
					return new CharLiteralExpr(value);
				case "byte":
					break;
				case "float":
					break;
				case "short":
					break;
			}
		} else {
			System.out.println("Not primitive type");
		}
		return null;
	}
}
