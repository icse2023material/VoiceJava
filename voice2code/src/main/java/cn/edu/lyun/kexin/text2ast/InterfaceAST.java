package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.ListHelper;
import cn.edu.lyun.util.Pair;

public class InterfaceAST {
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
