package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;
import cn.edu.anonymous.util.ListHelper;
import cn.edu.anonymous.util.Pair;

import java.util.*;

public class MethodAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "function");
		List<Unit> modifierList = pair.getFirst();
		NodeList<Modifier> modifierNodeList = new ModifierAST().generateModifierList(modifierList);

		MethodDeclaration methodDeclaration = new MethodDeclaration();
		methodDeclaration.setModifiers(modifierNodeList);

		Unit nameUnit = unitList.remove(0);
		methodDeclaration.setName(new SimpleName(nameUnit.getKeyword()));

		// TODO: how to create exception instance
		// NodeList<ReferenceType> thrownExceptions = new NodeList<ReferenceType>();
		// while (unitList.size() > 0) {
		// Unit throwsUnit = unitList.remove(0);
		// if (throwsUnit.getKeyword().equals("throws")) {
		// nameUnit = unitList.remove(0);
		// // thrownExceptions.add();
		// }
		// }
		// methodDeclaration.setThrownExceptions(thrownExceptions);

		// Type reference:
		// https://www.javadoc.io/doc/com.github.javaparser/javaparser-core/3.5.11/index.html
		Type type = new VoidType();
		methodDeclaration.setType(type);
		return methodDeclaration;
	}
}
