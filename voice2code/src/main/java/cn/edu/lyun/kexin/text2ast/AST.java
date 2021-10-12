package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;

public interface AST {
	public Node generate(String test);
}
