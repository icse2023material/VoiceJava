package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;
import cn.edu.anonymous.util.ListHelper;
import cn.edu.anonymous.util.Pair;

import java.util.*;

public class InterfaceAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "interface");
		List<Unit> modifierList = pair.getFirst();
		NodeList<Modifier> modifierNodeList = new ModifierAST().generateModifierList(modifierList);

		ClassOrInterfaceDeclaration classOrInterfaceDeclaration = new ClassOrInterfaceDeclaration();
		classOrInterfaceDeclaration.setName(new SimpleName(pair.getSecond().get(0).getKeyword()));
		classOrInterfaceDeclaration.setModifiers(modifierNodeList);
		classOrInterfaceDeclaration.setInterface(true);
		return classOrInterfaceDeclaration;
	}

}
