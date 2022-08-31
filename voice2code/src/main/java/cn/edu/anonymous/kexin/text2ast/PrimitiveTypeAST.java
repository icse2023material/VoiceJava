package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.expr.Expression;
import java.util.*;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

public class PrimitiveTypeAST {
	public static Expression generatePrimiviteExpr(List<Unit> units) {
		Unit unit = units.remove(0);
		if (PrimitiveTypeSet.isTypeWord(unit.getKeyword())) {
			String typeStr = unit.getKeyword();
			String value = generateStrFromUnits(typeStr, units);
			switch (unit.getKeyword()) {
				case "int":
					return new IntegerLiteralExpr(value);
				case "double":
					return new DoubleLiteralExpr(value);
				case "long":
					return new LongLiteralExpr(value);
				case "string":
					return new StringLiteralExpr(value);
				case "boolean":
					return new BooleanLiteralExpr(Boolean.parseBoolean(value));
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

	public static String generateStrFromUnits(String typeStr, List<Unit> units) {
		switch (typeStr) {
			case "int":
			case "double":
			case "long":
				return generateNumberStrFromUnits(units);
			case "boolean":
				return units.get(0).getKeyword();
			case "string":
				String value = "";
				for (Unit unit : units) {
					value += " " + unit.getKeyword();
				}
				return value;
			case "char":
				return units.get(0).getKeyword();
		}
		return null;
	}

	private static String generateNumberStrFromUnits(List<Unit> units) {
		boolean isValidInput = true;
		long result = 0;
		long finalResult = 0;
		List<String> allowedStrings = Arrays.asList(
				"zero", "one", "two", "three", "four", "five", "six", "seven",
				"eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen",
				"fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty",
				"thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety",
				"hundred", "thousand", "million", "billion", "trillion");

		if (units.size() > 0) {
			for (Unit unit : units) {
				String str = unit.getKeyword();
				if (!allowedStrings.contains(str)) {
					isValidInput = false;
					System.out.println("Invalid word found : " + str);
					break;
				}
			}
			if (isValidInput) {
				for (Unit unit : units) {
					String str = unit.getKeyword();
					if (str.equalsIgnoreCase("zero")) {
						result += 0;
					} else if (str.equalsIgnoreCase("one")) {
						result += 1;
					} else if (str.equalsIgnoreCase("two")) {
						result += 2;
					} else if (str.equalsIgnoreCase("three")) {
						result += 3;
					} else if (str.equalsIgnoreCase("four")) {
						result += 4;
					} else if (str.equalsIgnoreCase("five")) {
						result += 5;
					} else if (str.equalsIgnoreCase("six")) {
						result += 6;
					} else if (str.equalsIgnoreCase("seven")) {
						result += 7;
					} else if (str.equalsIgnoreCase("eight")) {
						result += 8;
					} else if (str.equalsIgnoreCase("nine")) {
						result += 9;
					} else if (str.equalsIgnoreCase("ten")) {
						result += 10;
					} else if (str.equalsIgnoreCase("eleven")) {
						result += 11;
					} else if (str.equalsIgnoreCase("twelve")) {
						result += 12;
					} else if (str.equalsIgnoreCase("thirteen")) {
						result += 13;
					} else if (str.equalsIgnoreCase("fourteen")) {
						result += 14;
					} else if (str.equalsIgnoreCase("fifteen")) {
						result += 15;
					} else if (str.equalsIgnoreCase("sixteen")) {
						result += 16;
					} else if (str.equalsIgnoreCase("seventeen")) {
						result += 17;
					} else if (str.equalsIgnoreCase("eighteen")) {
						result += 18;
					} else if (str.equalsIgnoreCase("nineteen")) {
						result += 19;
					} else if (str.equalsIgnoreCase("twenty")) {
						result += 20;
					} else if (str.equalsIgnoreCase("thirty")) {
						result += 30;
					} else if (str.equalsIgnoreCase("forty")) {
						result += 40;
					} else if (str.equalsIgnoreCase("fifty")) {
						result += 50;
					} else if (str.equalsIgnoreCase("sixty")) {
						result += 60;
					} else if (str.equalsIgnoreCase("seventy")) {
						result += 70;
					} else if (str.equalsIgnoreCase("eighty")) {
						result += 80;
					} else if (str.equalsIgnoreCase("ninety")) {
						result += 90;
					} else if (str.equalsIgnoreCase("hundred")) {
						result *= 100;
					} else if (str.equalsIgnoreCase("thousand")) {
						result *= 1000;
						finalResult += result;
						result = 0;
					} else if (str.equalsIgnoreCase("million")) {
						result *= 1000000;
						finalResult += result;
						result = 0;
					} else if (str.equalsIgnoreCase("billion")) {
						result *= 1000000000;
						finalResult += result;
						result = 0;
					} else if (str.equalsIgnoreCase("trillion")) {
						result *= 1000000000000L;
						finalResult += result;
						result = 0;
					}
				}

				finalResult += result;
				result = 0;
				return String.valueOf(finalResult);
			}
		}
		return null;
	}

}
