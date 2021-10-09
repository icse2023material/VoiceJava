package lyun.voice2code.nfa.strategy;

public class AnyMatchStrategy extends MatchStrategy {
	@Override
	public boolean isMatch(String str, String edge) {
		return true;
	}
}
