package cn.edu.lyun.kexin.text2code.astskeleton;

import java.util.HashMap;

public class TypeNameMap {

	public static HashMap<HoleType, String> map = createMap();

	private static HashMap<HoleType, String> createMap() {
		HashMap<HoleType, String> map = new HashMap<HoleType, String>();
		map.put(HoleType.PackageDeclaration, "getPackageDeclaration");
		map.put(HoleType.ImportDeclaration, "getImports"); // get all impors
		map.put(HoleType.TypeDeclaration, "getTypes");
		return map;
	}
}
