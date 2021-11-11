package cn.edu.lyun.kexin.test.text2code;

import java.io.File;
import java.io.IOException;

public class RunMultipleTest {
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";

	public static void main(String[] args) throws IOException {
		String dir = System.getProperty("user.dir");
		File directoryPath = new File(dir + "/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/testcases");
		File filesList[] = directoryPath.listFiles();
		for (File file : filesList) {
			if (!RunMultipleTest.getFileExtension(file).equals(".voiceJava")) {
				continue;
			}
			Generate.generate(file.getAbsolutePath());
			String fileName = file.getName();
			fileName = fileName.substring(0, fileName.indexOf("."));
			fileName += ".out";
			Boolean isEqual = Generate.compare(fileName);
			System.out.println(
					file.getName() + ": " + (isEqual ? ANSI_GREEN + "true" + ANSI_RESET : ANSI_RED + "false" + ANSI_RESET));
		}
	}

	private static String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf);
	}

}
