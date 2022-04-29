package cn.edu.lyun.kexin.text2ast;

import com.github.javaparser.ast.Node;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public class ASTManager {

	public static Node generate(Pattern pattern) {
		switch (pattern.getName()) {
			case "package":
				return new PackageAST().generate(pattern);
			case "import":
				return new ImportAST().generate(pattern);
			case "interface":
				return new InterfaceAST().generate(pattern);
			case "class":
				return new ClassAST().generate(pattern);
			case "constructor":
				return new ConstructorAST().generate(pattern);
			case "method":
				return new MethodAST().generate(pattern);
			case "arrowFunction":
				return null;
			case "field":
				return new FieldAST().generate(pattern);
			case "type":
				return new TypeExtendAST().generate(pattern);
		  case "typeVariable":
				return new TypeVariableAST().generate(pattern);
			case "for":
				return new ForAST().generate(pattern);
			case "while":
				return new WhileAST().generate(pattern);
			case "if":
				return new IfAST().generate(pattern);
			case "switch":
				return new SwitchAST().generate(pattern);
			case "tryCatch":
				return new TryAST().generate(pattern);
			case "Catch":
				return new CatchAST().generate(pattern);
			case "override":
				return null;
			case "subexpression":
				return new SubExpressionAST().generate(pattern);
			case "break":
				return new BreakAST().generate(pattern);
			case "continue":
				return new ContinueAST().generate(pattern);
			case "newInstance":
				return new NewInstanceAST().generate(pattern);
 			case "throwDecl":
        return new ThrowDeclAST().generate(pattern);
			case "throw":
				return new ThrowAST().generate(pattern);
			case "let":
				return new LetStmtAST().generate(pattern);
  		case "return":
				return new ReturnStmtAST().generate(pattern);
			case "expr0":
				return new ExprAST0().generate(pattern);
			case "expr1":
				return new ExprAST().generate(pattern);
			case "expr2":
				return new ExprAST2().generate(pattern);
			case "expr3":
				return new ExprAST3().generate(pattern);
			case "expr4":
				return new ExprAST4().generate(pattern);
			case "expr5":
				return new ExprAST5().generate(pattern);
			case "expr6":
				return new ExprAST6().generate(pattern);
			case "expr7":
				return new ExprAST7().generate(pattern);
			case "expr8":
				return new ExprAST8().generate(pattern);
			case "expr9":
				return new ExprAST9().generate(pattern);
			case "expr10":
				return new ExprAST10().generate(pattern);
			case "expr11":
				return new ExprAST11().generate(pattern);
			case "expr12":
				return new ExprAST12().generate(pattern);
			case "expr13":
				return new ExprAST13().generate(pattern);
			case "expr14":
				return new ExprAST14().generate(pattern);
      case "expr15":
        return new ExprAST15().generate(pattern);
      case "expr16":
        return new ExprAST16().generate(pattern);
      case "expr17":
        return new ExprAST17().generate(pattern);
      case "expr18":
        return new ExprAST18().generate(pattern);
		}

		return null;
	}
}
