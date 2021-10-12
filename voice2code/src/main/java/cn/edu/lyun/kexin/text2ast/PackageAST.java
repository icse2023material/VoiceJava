package cn.edu.lyun.kexin.text2ast;

import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

import java.util.*;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.Name;

public class PackageAST implements AST {

	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		unitList.remove(0); // remove "package"
		// TODO: temporary ignore annotations i.e. public/private etc.
		Collections.reverse(unitList); // reverse name list
		Name name = generate(unitList);
		return new PackageDeclaration(name);
	}

	// TODO: packge hello.dot.world case
	private Name generate(List<Unit> units) {
		units = removeDot(units);
		Unit first = units.get(0);
		String keyword = first.getKeyword();
		keyword = keyword.equals("star") ? "*" : keyword;
		if (units.size() == 1) {
			return new Name(keyword);
		} else {
			units.remove(0);
			return new Name(generate(units), keyword);
		}
	}

	private List<Unit> removeDot(List<Unit> units) {
		if (units.get(0).getKeyword() == "dot") {
			units.remove(0);
		}
		return units;
	}
}
