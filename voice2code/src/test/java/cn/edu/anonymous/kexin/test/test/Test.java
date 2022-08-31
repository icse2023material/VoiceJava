package cn.edu.anonymous.kexin.test.test;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DotPrinter;

public class Test {
  public static void main(String[] args) throws IOException {
    // Parse the code you want to inspect:
    CompilationUnit cu = StaticJavaParser.parse("package hello; import java.util.*; public class HelloWorld{}");
    // Now comes the inspection code:
    System.out.println(cu);
    DotPrinter printer = new DotPrinter(true);
    try (FileWriter fileWriter = new FileWriter("ast.dot");
    PrintWriter printWriter = new PrintWriter(fileWriter)) {
    printWriter.print(printer.output(cu));
    Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng ast.dot -o ast.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
}
}
}
