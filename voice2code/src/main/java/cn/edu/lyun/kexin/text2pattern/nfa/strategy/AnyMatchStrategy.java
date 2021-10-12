package cn.edu.lyun.kexin.text2pattern.nfa.strategy;

public class AnyMatchStrategy extends MatchStrategy {
	@Override
	public boolean isMatch(String str, String edge) {
		return true;
	}
}
