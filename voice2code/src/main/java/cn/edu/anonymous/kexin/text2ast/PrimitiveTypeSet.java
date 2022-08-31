package cn.edu.anonymous.kexin.text2ast;

import java.util.*;

public class PrimitiveTypeSet {
	private static Set<String> list = new HashSet<>(
			Arrays.asList(new String[] { "int", "byte", "short", "long", "char", "float", "double", "boolean", "string" }));

	public static boolean isTypeWord(String str) {
		return PrimitiveTypeSet.list.contains(str);
	}
}
