package cn.edu.lyun.kexin.text2pattern.pattern;

import java.util.*;

public class TypeWordSet {

	private static Set<String> typeWordList = new HashSet<>(Arrays.asList(new String[] { "int", "byte", "short", "long",
			"char", "float", "double", "boolean", "string", }));

	public static boolean isTypeWord(String str) {
		return TypeWordSet.typeWordList.contains(str);
	}
}
