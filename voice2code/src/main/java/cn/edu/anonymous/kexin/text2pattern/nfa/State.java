package cn.edu.anonymous.kexin.text2pattern.nfa;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class State {
	protected static int idCounter = 0;
	protected int id;
	protected StateType stateType;

	public State() {
		this.id = idCounter++;
		this.stateType = StateType.GENERAL;
	}

	public Map<String, Set<State>> next = new HashMap<String, Set<State>>();

	public void addNext(String edge, State nfaState) {
		Set<State> set = next.get(edge);
		if (set == null) {
			set = new LinkedHashSet<>();
			next.put(edge, set);
		}
		set.add(nfaState);
	}

	public void setStateType(StateType stateType) {
		this.stateType = stateType;
	}

	public boolean isEndState() {
		return stateType == StateType.END;
	}

	public int getId() {
		return this.id;
	}
}
