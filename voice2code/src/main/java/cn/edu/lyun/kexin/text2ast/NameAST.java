package cn.edu.lyun.kexin.text2ast;

import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.ListHelper;

import java.util.*;
import com.github.javaparser.ast.expr.Name;

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
