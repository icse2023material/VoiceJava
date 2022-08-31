package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;
import cn.edu.anonymous.util.ListHelper;
import cn.edu.anonymous.util.Pair;

import java.util.*;

public class TypeAST {

	public Type generateType(List<Unit> units) {
		// case 1: e.g. int, double, float
		if (units.size() == 1) {
			return StaticJavaParser.parseType(units.get(0).getKeyword());
		}
		// case 2: e.g. int list
		else if (units.get(0).getKeyword().equals("list")) {
			List<Unit> subUnits = new ArrayList<Unit>();
			subUnits.add(units.get(units.size() - 1));
			Type type = generateType(subUnits);
			// may have bug: ArrayType exptects more than one argument
			// see:
			// https://javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/type/ArrayType.html
			return new ArrayType(type);
		}

		String typeStr = "";
		// check dot
		if (new ListHelper().containsKeyword(units, "dot")) {
			Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitListAtLastKeyword(units, "dot");
			List<Unit> first = pair.getFirst();
			typeStr += generateDotTypeString(first);

			List<Unit> second = pair.getSecond();
			typeStr += generateWithTypeString(second);
			Type type = StaticJavaParser.parseClassOrInterfaceType(typeStr);
			return type;
		}

		if (new ListHelper().containsKeyword(units, "with")) {
			Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitListKeepAtSecond(units, "with");
			List<Unit> first = pair.getFirst();
			List<Unit> second = pair.getSecond();

			if (first.size() == 1) {
				typeStr = first.get(0).getKeyword();
				typeStr += generateWithTypeString(second);
				Type type = StaticJavaParser.parseClassOrInterfaceType(typeStr);
				return type;
			} else {
				System.out.println("this case not handled yet.");
			}
		}

		return null;
	}

	// Reference:
	// https://stackoverflow.com/questions/53344553/javaparser-add-arrayliststring-as-method-return-type
	public Type generatePrimitivType(Unit unit) {
		switch (unit.getKeyword()) {
			case "int":
				return PrimitiveType.intType();
			case "byte":
				return PrimitiveType.byteType();
			case "boolean":
				return PrimitiveType.booleanType();
			case "short":
				return PrimitiveType.shortType();
			case "char":
				return PrimitiveType.charType();
			case "double":
				return PrimitiveType.doubleType();
			case "long":
				return PrimitiveType.longType();
			case "float":
				return PrimitiveType.floatType();
			default:
				return null;
		}
	}

	public String generateDotTypeString(List<Unit> units) {
		String str = "";
		for (Unit unit : units) {
			str += unit.getKeyword().equals("dot") ? "." : unit.getKeyword();
		}
		return str;
	}

	// with a and b: <A,B>
	public String generateWithTypeString(List<Unit> units) {
		String str = "";
		if (new ListHelper().containsKeyword(units, "with")) {
			units.remove(0);
			str += "<";
			for (Unit unit : units) {
				if (unit.getKeyword().equals("and")) {
					continue;
				}
        String name = unit.getKeyword();
        name = name.equals("QuestionMark") || name.equals("questionMark") ? "?" : name;
				str += name + ",";
			}
      if(str.length()>1){
			  str = str.substring(0, str.length() - 1);
      } // else "<"
			str += ">";
		}

		return str;
	}
}
