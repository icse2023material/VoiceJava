package cn.edu.lyun.kexin.test.text2code;

import java.io.IOException;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;

public class Text2CompilationUnitTest {
	public static void main(String[] args) throws IOException {
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
		String[] lines = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greting", "int 2", "define public function sayHello", "type int",
				"type int list variable intArray", "type String variable name", "move next" };

		for (String line : lines) {
			text2CompilationUnit.generate(line);
		}

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
