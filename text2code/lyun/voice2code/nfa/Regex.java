package lyun.voice2code.nfa;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import lyun.voice2code.nfa.strategy.MatchStrategy;
import lyun.voice2code.nfa.strategy.MatchStrategyManager;
import lyun.voice2code.pattern.Pattern;
import lyun.voice2code.pattern.Unit;

public class Regex {
	private NFAGraph nfaGraph;

	public Regex(Pattern pattern) {
		Unit[] units = pattern.getUnits();
		this.nfaGraph = unitsToNFAGraph(units);
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
					addUnitToNFAGraph(first, nfaGraph);
				}
				Unit second = unit.getSecond();
				if (second != null) {
					addUnitToNFAGraph(second, nfaGraph);
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
						System.out.printf("%2d->%2d  %s\n", curState.getId(), next.getId(), key);
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

	public boolean isMatch(String text, int mode) {
		State start = nfaGraph.start;
		return isMatch(text, 0, start);
	}

	/**
	 * 匹配过程就是根据输入遍历图的过程, 这里DFA和NFA用了同样的代码, 但实际上因为DFA的特性是不会产生回溯的, 所以DFA可以换成非递归的形式
	 */
	private boolean isMatch(String text, int pos, State curState) {
		if (pos == text.length()) {
			for (State nextState : curState.next.getOrDefault(Constant.EPSILON, Collections.emptySet())) {
				if (isMatch(text, pos, nextState)) {
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
			// 这个if和else的先后顺序决定了是贪婪匹配还是非贪婪匹配
			if (Constant.EPSILON.equals(edge)) {
				// 如果是DFA模式,不会有EPSILON边,所以不会进这
				for (State nextState : entry.getValue()) {
					if (isMatch(text, pos, nextState)) {
						return true;
					}
				}
			} else {
				MatchStrategy matchStrategy = MatchStrategyManager.getStrategy(edge);
				if (!matchStrategy.isMatch(text.charAt(pos), edge)) {
					continue;
				}
				// 遍历匹配策略
				for (State nextState : entry.getValue()) {
					if (isMatch(text, pos + 1, nextState)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public List<String> match(String text) {
		return match(text, 0);
	}

	public List<String> match(String text, int mod) {
		int s = 0;
		int e = -1;
		List<String> res = new LinkedList<>();
		while (s != text.length()) {
			// e = getMatchEnd(text, s, dfaGraph.start);
			e = getMatchEnd(text, s, nfaGraph.start);
			if (e != -1) {
				res.add(text.substring(s, e));
				s = e;
			} else {
				s++;
			}
		}
		return res;
	}

	// 获取正则表达式在字符串中能匹配到的结尾的位置
	private int getMatchEnd(String text, int pos, State curState) {
		int end = -1;
		if (curState.isEndState()) {
			return pos;
		}

		if (pos == text.length()) {
			for (State nextState : curState.next.getOrDefault(Constant.EPSILON, Collections.emptySet())) {
				end = getMatchEnd(text, pos, nextState);
				if (end != -1) {
					return end;
				}
			}
		}

		for (Map.Entry<String, Set<State>> entry : curState.next.entrySet()) {
			String edge = entry.getKey();
			if (Constant.EPSILON.equals(edge)) {
				for (State nextState : entry.getValue()) {
					end = getMatchEnd(text, pos, nextState);
					if (end != -1) {
						return end;
					}
				}
			} else {
				MatchStrategy matchStrategy = MatchStrategyManager.getStrategy(edge);
				if (!matchStrategy.isMatch(text.charAt(pos), edge)) {
					continue;
				}
				// 遍历匹配策略
				for (State nextState : entry.getValue()) {
					end = getMatchEnd(text, pos + 1, nextState);
					if (end != -1) {
						return end;
					}
				}
			}
		}
		return -1;
	}
}
