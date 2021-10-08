package test;

import lyun.voice2code.*;

public class PatternTest {
	public static void main(String[] args) {
		Unit[] units = { new Unit("define"), new Unit("package"), new Unit(),
				new Unit("asterisk", new Unit("dot"), new Unit()) };
		Pattern pat = new Pattern("define package _ [dot _]*", units);
		System.out.println(pat.toString());
		System.out.println(pat.toVoiceJavaPattern());
	}

}
