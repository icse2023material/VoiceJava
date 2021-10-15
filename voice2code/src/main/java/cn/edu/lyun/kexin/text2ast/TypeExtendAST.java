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
import com.github.javaparser.ast.type.Type;

public class TypeExtendAST implements AST {

	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove "type"

		Pair<List<Unit>, List<Unit>> pair = new ListHelper().splitList(unitList, "extends");
		List<Unit> typeList = pair.getFirst();
		Type type = new TypeAST().generateType(typeList);

		// TODO: extends part

		return type;
	}
}
