package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

public class ForAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"

		if (unitList.get(0).getKeyword().equals("for")) {
			ForStmt forStmt = new ForStmt();
			return forStmt;
		} else {
			ForEachStmt forEachStmt = new ForEachStmt();
			return forEachStmt;
		}

	}
}
