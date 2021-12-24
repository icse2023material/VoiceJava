package cn.edu.lyun.kexin.test.text2code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;

public class RunSingleTest {
	private static boolean isDebug = true;

	public static void main(String[] args) throws IOException {
		String dir = System.getProperty("user.dir");
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
		String filePath = dir
				+
				"/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/testcases/12While_11.voiceJava";
		// + "/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/.voiceJava";
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		for (String line; (line = br.readLine()) != null;) {
			text2CompilationUnit.generate(line);

			if (RunSingleTest.isDebug) {
				text2CompilationUnit.generatePNGofHoleAST();
			}
		}
		br.close();

		text2CompilationUnit.generatePNGofHoleAST();
		System.out.println("done");
	}
}
