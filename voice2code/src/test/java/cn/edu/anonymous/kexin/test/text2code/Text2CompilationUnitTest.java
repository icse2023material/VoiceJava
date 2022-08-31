package cn.edu.anonymous.kexin.test.text2code;

import java.io.IOException;

import cn.edu.anonymous.kexin.text2code.Text2CompilationUnit;

public class Text2CompilationUnitTest {
	public static void main(String[] args) throws IOException {
		Boolean isStepDebug = false;
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
		String[] forExample = { "define package lyun", "import java dot util dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define public int variable count", "int 0", "define private int variable sum", "int 2",
				"define public String variable name", "move next", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "define int variable sum", "int 0",
				"define int variable b", "move next", "define for", "define int variable i", "int 0",
				"expression i less equal 10", "i plus plus", "let sum equal expression", "expression sum plus i",
				"let count equal expression", "expression count plus i" };
		String[] whileExample = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "define int variable sum", "int 0",
				"define while", "i less than 10", "let sum equal call fibonacci", "move next",
				"let hello dot sum equal hello call fibonacci", "move next", "let sum equal hello dot sum",
				"let sum equal variable x", "let sum equal int 200", "let sum equal expression", "int 2" };
		String[] ifExample = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "define int variable sum", "int 0",
				"define if", "i less than 10", "sum plus plus", "sum plus plus", "move next", "i greater than 10",
				"sum plus plus", "move next", "i double equal 10", "sum plus plus", "move next", "move next", "move next",
				"move next", "return sum" };
		String[] letExpr = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "let sum equal call sum",
				"let sum equal sum call sum", "let sum equal score dot sum", "let sum equal variable sumResult",
				"let sum equal int 100", "let sum dot count equal", "variable sum" };
		String[] switchExample = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "define int variable sum", "int 0",
				"define switch", "variable userType", "int 1", "let sum equal call fibonacci", "int 2", "move next",
				"expression 15 times 15", "expression 15 times sum", "move next", "int 2", "expression 15 times 15",
				"move next", "move next", "return sum" };

		String[] returnExpr = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "call notify",
				"subexpression plus subexpression", "int 3", "int 5", "move next", "return hello call notify", "int 2",
				"move next", };

		String[] general = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello",
				"type Nodelist with Statement", "type Nodelist with Statement variable nodelist", "move next",
				"let sum equal call fibonacci", "int 2", "move next", "let sum equal call fibonacci", "int 20", "move next" };

		isStepDebug = true;
		String[] lines = ifExample;
		for (String line : lines) {
			text2CompilationUnit.generate(line);

			if (isStepDebug) {
				text2CompilationUnit.generatePNGofHoleAST();
			}
		}

		text2CompilationUnit.generatePNGofHoleAST();

		System.out.println("done");
	}

}
