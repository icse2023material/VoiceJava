package cn.edu.lyun.kexin.test.text2code;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Generate {
	public static void generate(String filePath) throws IOException {
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();

		BufferedReader br = new BufferedReader(new FileReader(filePath));
		for (String line; (line = br.readLine()) != null;) {
			text2CompilationUnit.generate(line);
		}
		br.close();
		text2CompilationUnit.generatePNGofHoleAST();
	}

	public static boolean compare(String standardResultFileName) throws IOException {
		String dir = System.getProperty("user.dir");
		Path outputFileName = Path.of(dir + "/Test.java");
		String outputContent = Files.readString(outputFileName);
		outputContent = outputContent.replaceAll("\\s+", "");

		Path standardFileName = Path
				.of(dir + "/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/teststandard/" + standardResultFileName);
		String standardContent = Files.readString(standardFileName);
		standardContent = standardContent.replaceAll("\\s+", "");
		return standardContent.equals(outputContent);
	}
}
