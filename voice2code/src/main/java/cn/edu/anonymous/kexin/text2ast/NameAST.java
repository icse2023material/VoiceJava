package cn.edu.anonymous.kexin.text2ast;

import java.util.*;
import com.github.javaparser.ast.expr.Name;

import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;
import cn.edu.anonymous.util.ListHelper;

public class NameAST {

	public Name generate(List<Unit> units) {
		units = new ListHelper().removeDot(units);
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

}
