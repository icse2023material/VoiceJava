package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import java.util.*;
import com.github.javaparser.ast.type.Type;

public class ThrowDeclAST implements AST {
	public Node generate(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
		unitList.remove(0); // remove throw
    Type type = new TypeAST().generateType(unitList);
    return type;
	}

}
