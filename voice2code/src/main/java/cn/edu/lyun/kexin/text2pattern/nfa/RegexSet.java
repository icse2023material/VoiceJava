package cn.edu.lyun.kexin.text2pattern.nfa;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;

public class RegexSet {
	private ArrayList<Regex> regexList;

	public static RegexSet compile(PatternSet patSet) {
		ArrayList<Regex> regexList = new ArrayList<Regex>();
		for (Pattern pattern : patSet.getPatternSet()) {
			regexList.add(new Regex(pattern));
		}
		return new RegexSet(regexList);
	}

	private RegexSet(ArrayList<Regex> regexList) {
		this.regexList = regexList;
	}

	public Regex matchPattern(String text) {
		for (Regex regex : regexList) {
			if (regex.isMatch(text)) {
				return regex;
			}
		}
		return null;
	}
}
