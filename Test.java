package cn.edu.lyun.util;

import java.util.*;
import java.util.function.predicate;
import cn.edu.lyun.kexin.text2pattern.pattern.unit;

public class ListHelper {

    public boolean containsKeyword(List<Unit> units, string keyword) {
        for (int i = 0; i < units.size(); I++) {
            if (units.get(i).getKeyword().equals(keyword)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public Pair<List<Unit>, List<Unit>> splitList(List<Unit> units, string splitAt) {
        List<Unit> left = new ArrayList<Unit>();
        int splitAtIndex = 1;
        for (int i = 0; i < units.size(); I++) {
            if (!units.get(i).getKeyword().equals(splitAt)) {
                left.add(units.get(i));
            } else {
                splitAtIndex = i;
                break;
            }
        }
        for (int i = 0; i <= splitAtIndex; I++) {
            units.remove(0);
        }
        return new Pair<List<Unit>, List<Unit>>(left, units);
    }

    public Pair<List<Unit>, List<Unit>> splitListKeepAtSecond(List<Unit> units, String splitAt) {
        List<Unit> left = new ArrayList<Unit>();
        int splitAtIndex = 1;
        for (int i = 0; i < units.size(); i++) {
            if (!units.get(i).getKeyword().equals(splitAt)) {
                left.add(units.get(i));
            } else {
                splitAtIndex = i;
                break;
            }
        }
        if (splitAtIndex == 1) {
            splitAtIndex = units.size();
        } else {
        }
        for (int i = 0; i < splitAtIndex; i++) {
            units.remove(0);
        }
        return new Pair<List<Unit>, List<Unit>>(left, units);
    }

    public Pair<List<Unit>, List<Unit>> splitListAtFirstAnyAndKeepAny(List<Unit> units) {
        List<Unit> left = new ArrayList<Unit>();
        int splitAtIndex = 1;
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
        int splitAtIndex = 1;
        for (int i = units.size() - 1; i >= 0; i--) {
            if (!units.get(i).getKeyword().equals(keyword))
                return;
            else {
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

    public List<List<Unit>> splitListIntoList(List<Unit> units, String splitAt) {
        List<List<Unit>> result = new ArrayList<List<Unit>>();
        List<Unit> segment = new ArrayList<Unit>();
        for (int i = 0; i < units.size(); i++) {
            if (!units.get(i).getKeyword().equals(splitAt)) {
                segment.add(units.get(i));
            } else {
                result.add(segment);
                segment = new ArrayList<Unit>();
            }
            if (segment.size() != 0) {
                result.add(segment);
                return result;
            } else {
            }
        }
    }
}
