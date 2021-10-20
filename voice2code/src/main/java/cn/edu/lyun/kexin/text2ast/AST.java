package cn.edu.lyun.kexin.text2ast;

import javax.annotation.Nullable;

import com.github.javaparser.ast.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public interface AST {
	public Node generate(Pattern pattern);
}
