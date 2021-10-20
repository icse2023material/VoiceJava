package cn.edu.lyun.kexin.test.text2code;

import java.io.IOException;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;

public class Text2CompilationUnitTest {
	public static void main(String[] args) throws IOException {
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
		String[] lines = { "define package lyun", "import java dot uitl dot star",
				"import java dot lang dot reflect dot star", "move next", "define public class Hello",
				"define private int variable greting", "int 2", "define public function sayHello", "type int" };

		for (String line : lines) {
			text2CompilationUnit.generate(line);
		}
		System.out.println("done");
	}
}
