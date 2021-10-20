package cn.edu.lyun.kexin.text2code.astskeleton;

import java.util.HashMap;

public class TypeNameMap {

	public static HashMap<HoleType, String> map = createMap();

	private static HashMap<HoleType, String> createMap() {
		HashMap<HoleType, String> map = new HashMap<HoleType, String>();
		map.put(HoleType.PackageDeclaration, "getPackageDeclaration");
		map.put(HoleType.ImportDeclaration, "getImports"); // get all imports
		map.put(HoleType.TypeDeclaration, "getTypes");
		map.put(HoleType.FieldDeclaration, "getFields");
		map.put(HoleType.MethodDeclaration, "getMethods");
		map.put(HoleType.VariableDeclarator, "getVariables");
		map.put(HoleType.TypeExtends, "getType");
		map.put(HoleType.Body, "getBody");
		map.put(HoleType.Statement, "getStatements");
		map.put(HoleType.ForInitialization, "getInitialization");
		map.put(HoleType.ThenStatement, "getThenStmt");
		map.put(HoleType.ElseStatement, "getElseStmt");
		return map;
	}
}
