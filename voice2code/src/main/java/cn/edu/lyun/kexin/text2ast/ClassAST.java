package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.Pair;
import cn.edu.lyun.util.ListHelper;

public class ClassAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "class");
		List<Unit> modifierList = pair.getFirst();
		NodeList<Modifier> modifierNodeList = new ModifierAST().generateModifierList(modifierList);

		ClassOrInterfaceDeclaration classOrInterfaceDeclaration = new ClassOrInterfaceDeclaration();
		classOrInterfaceDeclaration.setInterface(false);
		classOrInterfaceDeclaration.setModifiers(modifierNodeList);

		unitList = pair.getSecond();
		Unit nameUnit = unitList.remove(0);
		classOrInterfaceDeclaration.setName(new SimpleName(nameUnit.getKeyword()));

		// extends or implemnts
		while (unitList.size() > 0) {
			Unit extendsOrImplementsUnit = unitList.remove(0);
			String name = extendsOrImplementsUnit.getKeyword();
			if (name.equals("extends") || name.equals("implements")) {
				NodeList<ClassOrInterfaceType> extendedTypes = new NodeList<ClassOrInterfaceType>();
				// TODO: extends multiple case
				nameUnit = unitList.remove(0);
				ClassOrInterfaceType classOrInterfaceType = new ClassOrInterfaceType();
				classOrInterfaceType.setName(new SimpleName(nameUnit.getKeyword()));
				extendedTypes.add(classOrInterfaceType);
				if (name.equals("extends")) {
					classOrInterfaceDeclaration.setExtendedTypes(extendedTypes);
				} else {
					classOrInterfaceDeclaration.setImplementedTypes(extendedTypes);
				}
			} else {
				System.out.println("wrong keyword, expecting extends or implements, " + extendsOrImplementsUnit.getKeyword());
			}
		}

		return classOrInterfaceDeclaration;
	}

}
