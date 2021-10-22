package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public interface AST {
	public Node generate(Pattern pattern);
}
