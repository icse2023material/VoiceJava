package lyun.voice2code.nfa.strategy;

public class KeywordMatchStrategy extends MatchStrategy {
	@Override
	public boolean isMatch(String str, String edge) {
		return str.equals(edge);
	}
}
