package cn.edu.lyun.kexin.text2ast;

import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.Pair;
import cn.edu.lyun.util.ListHelper;
import java.util.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.Type;

public class FieldAST implements AST {

	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "define"

		// Split modifiers part
		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitListAtFirstAnyAndKeepAny(unitList);
		List<Unit> modifierList = pair.getFirst();
		NodeList<Modifier> modifierNodeList = new ModifierAST().generateModifierList(modifierList);

		FieldDeclaration fieldDeclaration = new FieldDeclaration();
		fieldDeclaration.setModifiers(modifierNodeList);

		pair = new ListHelper().splitList(unitList, "variable");
		List<Unit> typeList = pair.getFirst();
		List<Unit> variableNameList = pair.getSecond();

		VariableDeclarator variableDeclarator = new VariableDeclarator();
		Type type = new TypeAST().generateType(typeList);
		// TODO: 目前仅考虑非组合词的情况
		if (variableNameList.size() == 1) {
			variableDeclarator.setType(type);
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

		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "variable");
		List<Unit> typeList = pair.getFirst();
		List<Unit> variableNameList = pair.getSecond();

		Type type = new TypeAST().generateType(typeList);
		VariableDeclarationExpr variableDeclarationExpr = new VariableDeclarationExpr(type,
				variableNameList.get(0).getKeyword());

		return new ExpressionStmt(variableDeclarationExpr);
	}
}
