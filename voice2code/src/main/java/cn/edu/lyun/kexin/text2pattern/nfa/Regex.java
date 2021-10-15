package cn.edu.lyun.kexin.text2pattern.nfa;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import cn.edu.lyun.kexin.text2pattern.nfa.strategy.KeyWordSet;
import cn.edu.lyun.kexin.text2pattern.nfa.strategy.MatchStrategy;
import cn.edu.lyun.kexin.text2pattern.nfa.strategy.MatchStrategyManager;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;
import cn.edu.lyun.util.Pair;

public class Regex {
	private NFAGraph nfaGraph;
	private Pattern pattern;

	public Regex(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		this.pattern = pattern;
		this.nfaGraph = unitsToNFAGraph(units);
	}

	public Pattern getPattern() {
		return this.pattern;
	}

	public NFAGraph unitsToNFAGraph(Unit[] units) {
		ArrayList<Unit> unitList = new ArrayList<Unit>();
		Collections.addAll(unitList, units);
		NFAGraph nfaGraph = null;
		Iterator<Unit> iterator = unitList.iterator();
		while (iterator.hasNext()) {
			nfaGraph = addUnitToNFAGraph(iterator.next(), nfaGraph);
		}

		nfaGraph.end.setStateType(StateType.END); // 将NFA的end节点标记为终止态
		return nfaGraph;
	}

	public NFAGraph addUnitToNFAGraph(Unit unit, NFAGraph nfaGraph) {
		switch (unit.getType()) {
			case "keyword":
				NFAState start = new NFAState();
				NFAState end = new NFAState();
				start.addNext(unit.getKeyword(), end);
				NFAGraph newNFAGraph = new NFAGraph(start, end);
				if (nfaGraph == null) {
					nfaGraph = newNFAGraph;
				} else {
					nfaGraph.addSeriesGraph(newNFAGraph);
				}
				break;
			case "any":
				start = new NFAState();
				end = new NFAState();
				start.addNext("any", end);
				newNFAGraph = new NFAGraph(start, end);
				if (nfaGraph == null) {
					nfaGraph = newNFAGraph;
				} else {
					nfaGraph.addSeriesGraph(newNFAGraph);
				}
				break;
			case "normal":
				Unit first = unit.getFirst();
				if (first != null) {
					newNFAGraph = addUnitToNFAGraph(first, nfaGraph);
					if (nfaGraph == null) {
						nfaGraph = newNFAGraph;
					} else {
						nfaGraph.addSeriesGraph(newNFAGraph);
					}
				}
				Unit second = unit.getSecond();
				if (second != null) {
					newNFAGraph = addUnitToNFAGraph(second, nfaGraph);
					if (nfaGraph == null) {
						nfaGraph = newNFAGraph;
					} else {
						nfaGraph.addSeriesGraph(newNFAGraph);
					}
				}
				break;
			case "question":
				// create a new NFAGraph for subregex
				// first() normally shall not be null
				newNFAGraph = addUnitToNFAGraph(unit.getFirst(), null);
				second = unit.getSecond();
				if (second != null) {
					NFAGraph nfaSecondGraph = addUnitToNFAGraph(second, null);
					newNFAGraph.addSeriesGraph(nfaSecondGraph);
				}
				// then add quesiton
				newNFAGraph.addEpsilonToEnd();
				if (nfaGraph == null) {
					nfaGraph = newNFAGraph;
				} else {
					nfaGraph.addSeriesGraph(newNFAGraph);
				}
				break;
			case "plus":
				newNFAGraph = addUnitToNFAGraph(unit.getFirst(), null);
				second = unit.getSecond();
				if (second != null) {
					NFAGraph nfaSecondGraph = addUnitToNFAGraph(second, null);
					newNFAGraph.addSeriesGraph(nfaSecondGraph);
				}
				newNFAGraph.repeatPlus();
				if (nfaGraph == null) {
					nfaGraph = newNFAGraph;
				} else {
					nfaGraph.addSeriesGraph(newNFAGraph);
				}
				break;
			case "asterisk":
				newNFAGraph = addUnitToNFAGraph(unit.getFirst(), null);
				second = unit.getSecond();
				if (second != null) {
					NFAGraph secondNFAGraph = addUnitToNFAGraph(second, null);
					newNFAGraph.addSeriesGraph(secondNFAGraph);
				}
				newNFAGraph.repeatStar();
				if (nfaGraph == null) {
					nfaGraph = newNFAGraph;
				} else {
					nfaGraph.addSeriesGraph(newNFAGraph);
				}
				break;
			case "or":
				first = unit.getFirst();
				NFAGraph leftNFAGraph = addUnitToNFAGraph(unit.getFirst(), null);
				NFAGraph rightNFAGraph = addUnitToNFAGraph(unit.getSecond(), null);
				leftNFAGraph.addParallelGraph(rightNFAGraph);
				if (nfaGraph == null) {
					nfaGraph = leftNFAGraph;
				} else {
					nfaGraph.addSeriesGraph(leftNFAGraph);
				}
				break;
			case "list":
				newNFAGraph = unitsToNFAGraph(unit.getList());
				if (nfaGraph == null) {
					nfaGraph = newNFAGraph;
				} else {
					nfaGraph.addSeriesGraph(newNFAGraph);
				}
				break;
		}
		return nfaGraph;
	}

	/**
	 * 有向图的广度优先遍历
	 */
	public void printNFA() {
		Queue<State> queue = new ArrayDeque<>();
		Set<Integer> addedStates = new HashSet<>();
		queue.add(nfaGraph.start);
		addedStates.add(nfaGraph.start.getId());
		while (!queue.isEmpty()) {
			State curState = queue.poll();
			for (Map.Entry<String, Set<State>> entry : curState.next.entrySet()) {
				String key = entry.getKey();
				Set<State> nexts = entry.getValue();
				for (State next : nexts) {
					System.out.printf("%2d->%2d  %s\n", curState.getId(), next.getId(), key);
					if (!addedStates.contains(next.getId())) {
						queue.add(next);
						addedStates.add(next.getId());
					}
				}
			}
		}
	}

	public void writeDotFile() {
		FileWriter filewriter;
		try {
			filewriter = new FileWriter("regex.dot");
			filewriter.write("digraph {");
			Queue<State> queue = new ArrayDeque<>();
			Set<Integer> addedStates = new HashSet<>();
			queue.add(nfaGraph.start);
			addedStates.add(nfaGraph.start.getId());
			while (!queue.isEmpty()) {
				State curState = queue.poll();
				for (Map.Entry<String, Set<State>> entry : curState.next.entrySet()) {
					String key = entry.getKey();
					Set<State> nexts = entry.getValue();
					for (State next : nexts) {
						String line = Integer.toString(curState.getId()) + " -> " + Integer.toString(next.getId()) + "[label=\""
								+ (key == "Epsilon" ? "ε" : key) + "\"]";
						filewriter.write(line);
						if (!addedStates.contains(next.getId())) {
							queue.add(next);
							addedStates.add(next.getId());
						}
					}
				}
			}
			filewriter.write("}");
			filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Pair<Boolean, Pattern> isMatch(String text) {
		State start = nfaGraph.start;
		String[] tokenList = text.split(" ");
		Pair<Boolean, List<Unit>> isMatchResult = isMatch(tokenList, 0, start, new ArrayList<Unit>());
		if (isMatchResult.getFirst()) {
			List<Unit> unitInstanceList = isMatchResult.getSecond();
			Unit[] unitList = new Unit[unitInstanceList.size()];
			unitInstanceList.toArray(unitList);
			return new Pair<Boolean, Pattern>(true, new Pattern(this.pattern.getName(), this.pattern.toString(), unitList));
		} else {
			return new Pair<Boolean, Pattern>(false,
					new Pattern(this.pattern.getName(), this.pattern.toString(), new Unit[] {}));
		}
	}

	private Pair<Boolean, List<Unit>> isMatch(String[] tokenList, int pos, State curState, List<Unit> unitInstanceList) {
		if (pos == tokenList.length) {
			for (State nextState : curState.next.getOrDefault(Constant.EPSILON, Collections.<State>emptySet())) {
				Pair<Boolean, List<Unit>> isMatchResult = isMatch(tokenList, pos, nextState, unitInstanceList);
				if (isMatchResult.getFirst()) {
					return isMatchResult;
				}
			}
			if (curState.isEndState()) {
				return new Pair<Boolean, List<Unit>>(true, unitInstanceList);
			}
			return new Pair<Boolean, List<Unit>>(false, new ArrayList<Unit>());
		}

		for (Map.Entry<String, Set<State>> entry : curState.next.entrySet()) {
			String edge = entry.getKey();
			// System.out.println("edge: " + edge);
			// 这个if和else的先后顺序决定了是贪婪匹配还是非贪婪匹配
			if (Constant.EPSILON.equals(edge)) {
				for (State nextState : entry.getValue()) {
					Pair<Boolean, List<Unit>> isMatchResult = isMatch(tokenList, pos, nextState, unitInstanceList);
					if (isMatchResult.getFirst()) {
						return isMatchResult;
					}
				}
			} else {
				MatchStrategy matchStrategy = null;
				if (KeyWordSet.isKeyword(edge)) {
					matchStrategy = MatchStrategyManager.getStrategy("keyword");
				} else {
					matchStrategy = MatchStrategyManager.getStrategy(edge);
				}
				if (!matchStrategy.isMatch(tokenList[pos], edge)) {
					continue;
				}

				Unit unitInstance;
				if (KeyWordSet.isKeyword(edge)) {
					unitInstance = new Unit(edge);
				} else {
					unitInstance = new Unit();
					unitInstance.setAnyValue(tokenList[pos]);
				}
				// 缓存结果，势必耗费大量资源
				List<Unit> clonedUnitInstanceList = new ArrayList<Unit>(unitInstanceList);
				clonedUnitInstanceList.add(unitInstance);

				// 遍历匹配策略
				for (State nextState : entry.getValue()) {
					Pair<Boolean, List<Unit>> isMatchResult = isMatch(tokenList, pos + 1, nextState, clonedUnitInstanceList);
					if (isMatchResult.getFirst()) {
						return isMatchResult;
					}
				}
			}
		}
		return new Pair<Boolean, List<Unit>>(false, new ArrayList<Unit>());
	}

}
