package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.IfStmt;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

public class IfAST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		if (unitList.get(0).getKeyword().equals("if")) {
			IfStmt ifStmt = new IfStmt();
			return ifStmt;
		} else {
			System.out.println("Wrong if command sequence");
			return null;
		}
	}
}
