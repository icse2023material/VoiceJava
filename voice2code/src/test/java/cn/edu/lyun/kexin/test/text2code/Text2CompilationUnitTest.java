package cn.edu.lyun.kexin.test.text2code;

import java.io.IOException;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;

public class Text2CompilationUnitTest {
	public static void main(String[] args) throws IOException {

		// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
		// while (true) {
		// String line = reader.readLine();
		String line = "define package lyun";
		text2CompilationUnit.generate(line);

		line = "import java dot uitl dot star";
		text2CompilationUnit.generate(line);
		line = "import java dot lang dot reflect dot star";
		text2CompilationUnit.generate(line);

		line = "move next";

		text2CompilationUnit.generate(line);

		// }
	}
}
