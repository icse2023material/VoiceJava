package cn.edu.anonymous.util;

public class StringHelper {
	public static String getClassName(String fullName) {
		// "class cn.anonymous.edu.Hello" -> "Hello"
		return fullName.substring(fullName.lastIndexOf(".") + 1);
	}

}
