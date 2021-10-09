package lyun.voice2code.nfa;

import java.util.*;
import lyun.voice2code.pattern.PatternSet;
import lyun.voice2code.pattern.Pattern;

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
