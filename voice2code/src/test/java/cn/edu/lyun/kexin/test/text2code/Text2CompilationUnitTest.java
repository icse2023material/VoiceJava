package cn.edu.lyun.kexin.test.text2code;

import java.io.IOException;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;

public class Text2CompilationUnitTest {
	public static void main(String[] args) throws IOException {
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
		String[] example1 = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "define int variable sum", "int 0",
				"define for", "define int variable i", "int 0", "expression i less equal 10", "i plus plus",
				"let sum equal expression", "expression sum plus i", };
		String[] example2 = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "define int variable sum", "int 0",
				"define while", "i less than 10", "let sum equal call fibonacci",
				"let hello dot sum equal hello call fibonacci", "let sum equal hello dot sum", "let sum equal variable x",
				"let sum equal int 200", "let sum equal expression", "int 2" };
		String[] ifExample = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greeting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next", "define int variable sum", "int 0",
				"define if", "i less than 10", "sum plus plus", "sum plus plus", "move next", "i double equal 10",
				"sum plus plus", "sum plus plus", "move next", "i greater than 10", "sum plus plus", "move next", "move next",
				"move next", "move next", "move next", "move next", "move next", "return sum" };

		String[] lines = ifExample;

		for (String line : lines) {
			text2CompilationUnit.generate(line);
		}

		// text2CompilationUnit.generate("return sum");
		text2CompilationUnit.getHoleAST().writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng holeAST.dot -o holeAST.png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done");
	}
}
