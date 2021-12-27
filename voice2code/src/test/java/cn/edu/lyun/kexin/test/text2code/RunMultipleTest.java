package cn.edu.lyun.kexin.test.text2code;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RunMultipleTest {
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";

	public static void main(String[] args) throws IOException {
		String dir = System.getProperty("user.dir");
		File directoryPath = new File(dir + "/voice2code/src/test/java/cn/edu/lyun/kexin/test/text2code/testcases");
		File filesList[] = directoryPath.listFiles();
		int passCount = 0, failCount = 0;
		ArrayList<String> failedFileList = new ArrayList<String>();
		for (File file : filesList) {
			if (!RunMultipleTest.getFileExtension(file).equals(".voiceJava")) {
				continue;
			}
			System.out.print(file.getName() + ": ");
			Generate.generate(file.getAbsolutePath());
			String fileName = file.getName();
			fileName = fileName.substring(0, fileName.indexOf("."));
			fileName += ".out";
			Boolean isEqual = Generate.compare(fileName);
			if (isEqual) {
				passCount++;
			} else {
				failCount++;
				failedFileList.add(file.getName());
			}
			System.out.println(
					(isEqual ? ANSI_GREEN + "true" + ANSI_RESET : ANSI_RED + "false" + ANSI_RESET));
		}

		RunMultipleTest.output(passCount, failCount, failedFileList);
	}

	private static String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf);
	}

	private static void output(int passCount, int failCount, ArrayList<String> failedArrayList) {
		System.out.println();
		System.out.println("-------------------------------------");
		System.out.println("Test Result:");
		System.out.println("Total: " + (passCount + failCount));
		System.out.println("Pass : " + ANSI_GREEN + passCount + ANSI_RESET);
		System.out.println("Fail : " + ANSI_RED + failCount + ANSI_RED + ANSI_RESET);
		if (failCount != 0) {
			System.out.println("Failed file name list:");
			for (String str : failedArrayList) {
				System.out.println("     " + str);
			}
		}
	}

}
