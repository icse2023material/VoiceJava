package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.Pair;

public class InterfaceAST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
		Pair<List<Unit>, List<Unit>> pair = splitList(unitList, "interface");
		List<Unit> modifierList = pair.getFirst();
		NodeList<Modifier> modifierNodeList = new ModifierAST().generateModifierList(modifierList);

		ClassOrInterfaceDeclaration classOrInterfaceDeclaration = new ClassOrInterfaceDeclaration();
		classOrInterfaceDeclaration.setName(new SimpleName(pair.getSecond().get(0).getKeyword()));
		classOrInterfaceDeclaration.setModifiers(modifierNodeList);
		classOrInterfaceDeclaration.setInterface(true);
		return classOrInterfaceDeclaration;
	}

	public Pair<List<Unit>, List<Unit>> splitList(List<Unit> units, String splitAt) {
		List<Unit> left = new ArrayList<Unit>();
		int splitAtIndex = -1;
		for (int i = 0; i < units.size(); i++) {
			if (!units.get(i).getKeyword().equals(splitAt)) {
				left.add(units.get(i));
			} else {
				splitAtIndex = i;
				break;
			}
		}
		for (int i = 0; i <= splitAtIndex; i++) {
			units.remove(0);
		}
		return new Pair<List<Unit>, List<Unit>>(left, units);
	}

}
