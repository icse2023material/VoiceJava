package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.stmt.CatchClause;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public class CatchAST implements AST {
	public Node generate(Pattern pattern) {
    return new CatchClause();
	}
}
