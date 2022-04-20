package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class NewInstanceAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "new"
		unitList.remove(0); // remove "instance"
		ObjectCreationExpr objCreationExpr = new ObjectCreationExpr();
    Type type = new TypeAST().generateType(unitList);

		objCreationExpr.setType((ClassOrInterfaceType)type);
		return objCreationExpr;
	}

}
