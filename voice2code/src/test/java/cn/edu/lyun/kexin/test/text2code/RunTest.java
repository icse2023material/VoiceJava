package cn.edu.lyun.kexin.test.text2code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;

public class RunTest {
	public static void main(String[] args) throws IOException {
		String dir = System.getProperty("user.dir");
		Text2CompilationUnit text2CompilationUnit = new Text2CompilationUnit();
		String filePath = dir + "/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/testcases/Interface.voiceJava";
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		for (String line; (line = br.readLine()) != null;) {
			text2CompilationUnit.generate(line);

			text2CompilationUnit.getHoleAST().writeDotFile();
			Runtime rt = Runtime.getRuntime();
			try {
				rt.exec("dot -Tpng holeAST.dot -o holeAST.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
			text2CompilationUnit.getHoleAST().cleverMove();

			text2CompilationUnit.getHoleAST().writeDotFile();
			rt = Runtime.getRuntime();
			try {
				rt.exec("dot -Tpng holeAST.dot -o holeAST.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		br.close();
		// text2CompilationUnit.generate("");
		// // text2CompilationUnit.generate("");
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
