package cn.edu.anonymous.kexin.test.text2pattern.pattern;

import cn.edu.anonymous.kexin.text2pattern.pattern.*;

public class PatternTest {
	public static void main(String[] args) {

        Pattern expr20Pat = new Pattern("expr20Null", "expression? null", 
        new Unit[]{new Unit("question", new Unit("expression")), new Unit("null")});
     
		System.out.println(expr20Pat.toString());
		System.out.println(expr20Pat.toVoiceJavaPattern());
	}

}
