package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.SwitchStmt;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

public class SwitchAST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		if (unitList.get(0).getKeyword().equals("switch")) {
			SwitchStmt stmt = new SwitchStmt();
			return stmt;
		} else {
			System.out.println("Wrong switch command sequence");
			return null;
		}
	}
}
