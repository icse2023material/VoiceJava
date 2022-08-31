package cn.edu.anonymous.kexin.text2ast;

import java.util.*;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.Name;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

public class PackageAST implements AST {

	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		unitList.remove(0); // remove "package"
		// TODO: temporary ignore annotations i.e. public/private etc.
		Collections.reverse(unitList); // reverse name list
		Name name = (new NameAST()).generate(unitList);
		return new PackageDeclaration(name);
	}
}
