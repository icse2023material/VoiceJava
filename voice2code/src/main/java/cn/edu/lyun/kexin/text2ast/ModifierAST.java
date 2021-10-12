package cn.edu.lyun.kexin.text2ast;

import java.util.*;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

public class ModifierAST {
	public NodeList<Modifier> generateModifierList(List<Unit> units) {
		NodeList<Modifier> nodeList = new NodeList<Modifier>();
		for (Unit unit : units) {
			Modifier modifier = new Modifier();
			switch (unit.getKeyword()) {
				case "public":
					modifier.setKeyword(Modifier.Keyword.PUBLIC);
					break;
				case "private":
					modifier.setKeyword(Modifier.Keyword.PRIVATE);
					// TODO: more case
			}
			nodeList.add(modifier);
		}
		return nodeList;
	}
}
