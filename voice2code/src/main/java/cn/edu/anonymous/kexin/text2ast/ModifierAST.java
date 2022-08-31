package cn.edu.anonymous.kexin.text2ast;

import java.util.*;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;

import cn.edu.anonymous.kexin.text2pattern.pattern.Unit;

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
					break;
				case "abstract":
					modifier.setKeyword(Modifier.Keyword.ABSTRACT);
					break;
				case "default":
					modifier.setKeyword(Modifier.Keyword.DEFAULT);
					break;
				case "final":
					modifier.setKeyword(Modifier.Keyword.FINAL);
					break;
				case "native":
					modifier.setKeyword(Modifier.Keyword.NATIVE);
					break;
				case "protected":
					modifier.setKeyword(Modifier.Keyword.PROTECTED);
					break;
				case "static":
					modifier.setKeyword(Modifier.Keyword.STATIC);
					break;
				case "strictfp":
					modifier.setKeyword(Modifier.Keyword.STRICTFP);
					break;
				case "synchronized":
					modifier.setKeyword(Modifier.Keyword.SYNCHRONIZED);
					break;
				case "transient":
					modifier.setKeyword(Modifier.Keyword.TRANSIENT);
					break;
				case "transitive":
					modifier.setKeyword(Modifier.Keyword.TRANSITIVE);
					break;
				case "volatile":
					modifier.setKeyword(Modifier.Keyword.VOLATILE);
					break;
			}
			nodeList.add(modifier);
		}
		return nodeList;
	}
}
