package cn.edu.anonymous.kexin.text2ast;

import java.util.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.Type;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;
import cn.edu.anonymous.util.ListHelper;
import cn.edu.anonymous.util.Pair;

public class TypeVariableAST implements AST {

	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "type"

		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "variable");
		List<Unit> typeList = pair.getFirst();
		List<Unit> variableNameList = pair.getSecond();

		Type type = new TypeAST().generateType(typeList);

		Parameter parameter = new Parameter();
		parameter.setName(new SimpleName(variableNameList.get(0).getKeyword()));
		parameter.setType(type);

		return parameter;
	}
}
