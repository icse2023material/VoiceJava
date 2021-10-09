package lyun.voice2code.nfa;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import lyun.voice2code.nfa.strategy.KeyWordSet;
import lyun.voice2code.nfa.strategy.MatchStrategy;
import lyun.voice2code.nfa.strategy.MatchStrategyManager;
import lyun.voice2code.pattern.Pattern;
import lyun.voice2code.pattern.Unit;

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

	public boolean isMatch(String text) {
		State start = nfaGraph.start;
		String[] tokenList = text.split(" ");
		// for (String token : tokenList) {
		// System.out.println(token);
		// }
		return isMatch(tokenList, 0, start);
	}

	private boolean isMatch(String[] tokenList, int pos, State curState) {
		if (pos == tokenList.length) {
			for (State nextState : curState.next.getOrDefault(Constant.EPSILON, Collections.emptySet())) {
				if (isMatch(tokenList, pos, nextState)) {
					return true;
				}
			}
			if (curState.isEndState()) {
				return true;
			}
			return false;
		}

		for (Map.Entry<String, Set<State>> entry : curState.next.entrySet()) {
			String edge = entry.getKey();
			System.out.println("edge: " + edge);
			// 这个if和else的先后顺序决定了是贪婪匹配还是非贪婪匹配
			if (Constant.EPSILON.equals(edge)) {
				for (State nextState : entry.getValue()) {
					if (isMatch(tokenList, pos, nextState)) {
						return true;
					}
				}
			} else {
				MatchStrategy matchStrategy = null;
				if (KeyWordSet.isKeyword(edge)) {
					System.out.println("keyword match");
					matchStrategy = MatchStrategyManager.getStrategy("keyword");
				} else {
					matchStrategy = MatchStrategyManager.getStrategy(edge);
				}
				if (!matchStrategy.isMatch(tokenList[pos], edge)) {
					continue;
				}
				System.out.println("entry: " + entry.getValue());
				// 遍历匹配策略
				for (State nextState : entry.getValue()) {
					System.out.println("nextState: " + nextState.getId());
					if (isMatch(tokenList, pos + 1, nextState)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
