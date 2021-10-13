package cn.edu.lyun.util;

import java.util.*;
import java.util.function.Predicate;

import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

public class ListHelper {

	public boolean containsKeyword(List<Unit> units, String keyword) {
		for (Unit unit : units) {
			if (unit.getKeyword().equals(keyword)) {
				return true;
			}
		}
		return false;
	}

	public Pair<List<Unit>, List<Unit>> splitList(List<Unit> units, String splitAt) {
		List<Unit> left = new ArrayList<Unit>();
		int splitAtIndex = -1;
		for (int i = 0; i < units.size(); i++) {
			if (!units.get(i).getKeyword().equals(splitAt)) {
				left.add(units.get(i));
			} else {
				splitAtIndex = i;
				break;
			}
		}
		for (int i = 0; i <= splitAtIndex; i++) {
			units.remove(0);
		}
		return new Pair<List<Unit>, List<Unit>>(left, units);
	}

	public Pair<List<Unit>, List<Unit>> splitListKeepAtSecond(List<Unit> units, String splitAt) {
		List<Unit> left = new ArrayList<Unit>();
		int splitAtIndex = -1;
		for (int i = 0; i < units.size(); i++) {
			if (!units.get(i).getKeyword().equals(splitAt)) {
				left.add(units.get(i));
			} else {
				splitAtIndex = i;
				break;
			}
		}
		for (int i = 0; i < splitAtIndex; i++) {
			units.remove(0);
		}
		return new Pair<List<Unit>, List<Unit>>(left, units);
	}

	public Pair<List<Unit>, List<Unit>> splitListAtFirstAnyAndKeepAny(List<Unit> units) {
		List<Unit> left = new ArrayList<Unit>();
		int splitAtIndex = -1;
		for (int i = 0; i < units.size(); i++) {
			if (!units.get(i).getType().equals("any")) {
				left.add(units.get(i));
			} else {
				splitAtIndex = i;
				break;
			}
		}
		for (int i = 0; i < splitAtIndex; i++) {
			units.remove(0);
		}
		return new Pair<List<Unit>, List<Unit>>(left, units);
	}

	public Pair<List<Unit>, List<Unit>> splitListAtLastKeyword(List<Unit> units, String keyword) {
		int splitAtIndex = -1;
		for (int i = units.size() - 1; i >= 0; i--) {
			if (!units.get(i).getKeyword().equals(keyword)) {
				continue;
			} else {
				splitAtIndex = i + 1;
				break;
			}
		}

		List<Unit> left = new ArrayList<Unit>();
		for (int i = 0; i <= splitAtIndex; i++) {
			left.add(units.get(i));
		}
		for (int i = 0; i <= splitAtIndex; i++) {
			units.remove(0);
		}

		return new Pair<List<Unit>, List<Unit>>(left, units);
	}

	public List<Unit> removeDot(List<Unit> units) {
		units.removeIf(new Predicate<Unit>() {
			@Override
			public boolean test(Unit unit) {
				return unit.getKeyword().equals("dot");
			}
		});
		return units;
	}

	public List<Unit> removeCall(List<Unit> units) {
		units.removeIf(new Predicate<Unit>() {
			@Override
			public boolean test(Unit unit) {
				return unit.getKeyword().equals("call");
			}
		});
		return units;
	}

}
