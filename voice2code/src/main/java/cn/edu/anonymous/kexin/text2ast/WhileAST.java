package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

import java.util.*;

public class WhileAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"

		if (unitList.get(0).getKeyword().equals("while")) {
			WhileStmt whileStmt = new WhileStmt();
			return whileStmt;
		} else {
			DoStmt doStmt = new DoStmt();
			return doStmt;
		}

	}
}
