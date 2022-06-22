package cn.edu.lyun.kexin.text2pattern.nfa.strategy;

import java.util.*;

// "star", 
public class KeyWordSet {
	private static Set<String> keywordList = new HashSet<>(Arrays.asList(new String[] { "define", "package", "dot",
			"import", "interface", "class", "extends", "implements", "constructor", "function", "throws", "exception",
			"arrow", "variable", "static", "annotation", "public", "protected", "private", "abstract", "static", "final",
			"strictfp", "synchronized", "native", "transient", "volatile", "list", "of", "with", "type", "for", "enhanced",
			"do",
			"while", "if", "switch", "try", "catch", "at", "override", "let", "equal", "return", "plus", "minus", "call",
			"plus", "times", "divide", "mod", "less", "than", "greater", "double", "and", "subexpression", "break",
			"continue", "new", "instance", "throw", "cast", "statement",
      "null",
			"new", "move", "next", "jump", "out", "before", "after", "to", "line", "start", "end", "up", "lines", "down",
			"left", "right", "select", "line", "body", "replace", "delete", "expression", "index", "null","conditional", "not", "lambda", "or" }));

	// "int", "byte", "short", "long",
	// "char", "float", "double", "boolean", "string",
	public static boolean isKeyword(String str) {
		return KeyWordSet.keywordList.contains(str);
	}
}
