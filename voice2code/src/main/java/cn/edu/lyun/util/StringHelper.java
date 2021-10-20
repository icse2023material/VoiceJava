package cn.edu.lyun.util;

public class StringHelper {
	public static String getClassName(String fullName) {
		// "class cn.lyun.edu.Hello" -> "Hello"
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}

}
