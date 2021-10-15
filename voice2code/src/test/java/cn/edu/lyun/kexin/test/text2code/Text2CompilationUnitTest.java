package cn.edu.lyun.kexin.test.text2code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;

import cn.edu.lyun.kexin.text2ast.ASTManager;
import cn.edu.lyun.kexin.text2code.Text2CompilationUnit;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleNode;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

		// }
	}
}
