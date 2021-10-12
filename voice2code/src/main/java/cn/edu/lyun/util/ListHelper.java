package cn.edu.lyun.util;

import java.util.*;
import cn.edu.lyun.kexin.text2pattern.pattern.Unit;

public class ListHelper {

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
}
