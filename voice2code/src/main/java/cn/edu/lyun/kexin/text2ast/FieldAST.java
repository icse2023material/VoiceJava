package cn.edu.lyun.kexin.text2ast;

import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.Pair;
import cn.edu.lyun.util.ListHelper;
import java.util.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;

public class FieldAST implements AST {

	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"

		// Split modifiers part
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "variable");
		List<Unit> modifierList = pair.getFirst();
		// TODO: support complex name case
		if (modifierList.size() >= 2) {
			if (modifierList.get(modifierList.size() - 1).getKeyword().equals("of")) {
				Unit u = modifierList.remove(modifierList.size() - 1);
				unitList.add(0, u); // add of
				u = modifierList.remove(modifierList.size() - 1);
				unitList.add(0, u); // add list
			}
		}
		NodeList<Modifier> modifierNodeList = new ModifierAST().generateModifierList(modifierList);

		FieldDeclaration fieldDeclaration = new FieldDeclaration();
		fieldDeclaration.setModifiers(modifierNodeList);

		List<Unit> variableNameList = pair.getSecond();

		VariableDeclarator variableDeclarator = new VariableDeclarator();
		// TODO: 目前仅考虑非组合词的情况
		if (variableNameList.size() == 1) {
			variableDeclarator.setName(new SimpleName(variableNameList.get(0).getKeyword()));
		} else {
			System.out.println("Haven't implemented for word combined case");
		}

		NodeList<VariableDeclarator> nodeList = new NodeList<VariableDeclarator>();
		nodeList.add(variableDeclarator);
		fieldDeclaration.setVariables(nodeList);

		return fieldDeclaration;
	}

	public Node generateVariableDeclarationExpr(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"
    unitList.remove(0); // remove "variable"
		VariableDeclarationExpr variableDeclarationExpr = new VariableDeclarationExpr(StaticJavaParser.parseType("void"),
    unitList.get(0).getKeyword());

		return new ExpressionStmt(variableDeclarationExpr);
	}
}
