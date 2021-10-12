package cn.edu.lyun.kexin.text2ast;

import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

import java.util.*;

import com.github.javaparser.ast.expr.Name;

public class NameAST {

	public Name generate(List<Unit> units) {
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
