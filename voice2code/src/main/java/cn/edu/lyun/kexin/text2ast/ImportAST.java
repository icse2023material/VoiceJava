package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.Name;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

public class ImportAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "import"
		boolean isStatic = unitList.get(0).getKeyword().equals("static");
		if (isStatic) {
			unitList.remove(0);
		}
		Collections.reverse(unitList); // reverse name list
		Name name = (new NameAST()).generate(unitList);
		ImportDeclaration importDeclaration = new ImportDeclaration(name, isStatic, false);
		return importDeclaration;
	}
}
