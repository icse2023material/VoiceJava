package cn.edu.anonymous.kexin.text2pattern.nfa.strategy;

import java.util.HashMap;
import java.util.Map;

import cn.edu.anonymous.kexin.text2pattern.nfa.Constant;

public class MatchStrategyManager {
	private static Map<String, MatchStrategy> matchStrategyMap;

	static {
		matchStrategyMap = new HashMap<>();
		matchStrategyMap.put(Constant.EPSILON, new EpsilonMatchStrategy());
		matchStrategyMap.put("keyword", new KeywordMatchStrategy());
		matchStrategyMap.put("any", new AnyMatchStrategy());
	}

	public static MatchStrategy getStrategy(String key) {
		// 特殊字符的匹配
		if (matchStrategyMap.containsKey(key)) {
			return matchStrategyMap.get(key);
		}
		// 单字符和字符集的匹配
		if (key.length() == 1) {
			return matchStrategyMap.get(Constant.CHAR);
		} else {
			return matchStrategyMap.get(Constant.CHARSET);
		}
	}
}
