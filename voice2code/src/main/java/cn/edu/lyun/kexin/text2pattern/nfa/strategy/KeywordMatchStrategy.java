package cn.edu.lyun.kexin.text2pattern.nfa.strategy;

public class KeywordMatchStrategy extends MatchStrategy {
	@Override
	public boolean isMatch(String str, String edge) {
		// TODO: 模糊匹配，自动纠错，人工智能
		return str.equals(edge);
	}
}
