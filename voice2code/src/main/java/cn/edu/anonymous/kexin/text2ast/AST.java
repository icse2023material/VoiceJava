package cn.edu.anonymous.kexin.text2ast;

import com.github.javaparser.ast.*;

import cn.edu.anonymous.kexin.text2pattern.pattern.Pattern;

public interface AST {
	public Node generate(Pattern pattern);
}
