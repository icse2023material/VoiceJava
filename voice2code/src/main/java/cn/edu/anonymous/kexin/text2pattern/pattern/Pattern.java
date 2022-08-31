package cn.edu.anonymous.kexin.text2pattern.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pattern {
	private String name; // a specific name for the pattern
	private String str;
	private Unit[] units;

	public Pattern(String name, String str) {
		this.name = name;
		this.str = str;
	}

	public Pattern(String name, String str, Unit[] units) {
		this.name = name;
		this.str = str;
		this.units = units;
	}

	public String getName() {
		return this.name;
	}

	public String getPattern() {
		return this.str;
	}

	public Unit[] getUnits() {
		return this.units;
	}

	@Override
	public String toString() {
		return this.str;
	}

	public String toVoiceJavaPattern() {
		String str = "";
		for (Unit unit : units) {
			str += str != "" ? " " + unit.toVoiceJavaPattern() : unit.toVoiceJavaPattern();
		}
		return str;
	}

	public String showInstance() {
		String str = "";
		for (Unit unit : units) {
			str += str != "" ? " " + unit.showInstance() : unit.showInstance();
		}
		return str;

	}

	private Unit createUnit(List<Unit> temp) {
		if (name.equals("package") || name.equals("import")) {
			return createUnitWithNotCamelString(temp);
		} else if (name.equals("type") || name.equals("class") || name.equals("interface") || name.equals("newInstance") || name.equals("throw")) {
			return createUnitWithCamelStringForClassOrInterface(temp);
		} else {
			return createUnitWithCamelString(temp);
		}
	}

	public Pattern concatNames() {
		List<String> shouldNotConcatKeywords = new ArrayList<String>(
				Arrays.asList(new String[] { "return5", "let5", "expr5", "expr14" }));
		if (shouldNotConcatKeywords.contains(this.name)) {
			return this;
		}
		List<Unit> unitList = new ArrayList<Unit>(Arrays.asList(units));
    
    // temporary workaround 
    if(this.name.equals("expr4")){
      List<Unit> result = forExpr4(unitList);
		  units = result.toArray(new Unit[result.size()]);
      return this;
    } else if (this.name.equals("expr1")){
      List<Unit> result = forExpr1(unitList);
		  units = result.toArray(new Unit[result.size()]);
      return this;
    }

		List<Unit> result = new ArrayList<Unit>();

		List<Unit> temp = new ArrayList<Unit>();
		while (unitList.size() != 0) {
			Unit unit = unitList.remove(0);
			if (unit.getType().equals("keyword") || (TypeWordSet.isTypeWord(unit.getKeyword())&& (!this.getName().equals("class")&&!this.getName().equals("method")))) {
				if (temp.size() != 0) {
					result.add(createUnit(temp));
					temp.clear();
				}
				result.add(unit);
			} else {
				temp.add(unit);
			}
		}
		if (temp.size() != 0) {
			result.add(createUnit(temp));
		}

		units = result.toArray(new Unit[result.size()]);
		return this;
	}

  private List<Unit> forExpr4(List<Unit> unitList){
    List<Unit> result = new ArrayList<Unit>();
    Unit unit = unitList.remove(0);
    if(unit.getKeyword().equals("expression")){
      result.add(unit);
      unit = unitList.remove(0);
    }
    if(unit.getKeyword().equals("variable")){
      result.add(unit);
    }
    result.add(createUnit(unitList));
    return result;
  }

  private List<Unit> forExpr1(List<Unit> unitList){
    List<Unit> result = new ArrayList<Unit>();
    Unit unit = unitList.remove(0);
    if(unit.getKeyword().equals("expression")){
      result.add(unit);
      unit = unitList.remove(0);
    }
    if(unit.getKeyword().equals("call")){
      result.add(unit);
    }
    result.add(createUnit(unitList));
    return result;
  }

	private Unit createUnitWithCamelString(List<Unit> units) {
		Unit any = new Unit();
		any.setAnyValue(concatCamelString(units));
		return any;
	}

	private String concatCamelString(List<Unit> units) {
		String str = "";
		for (int i = 0; i < units.size(); i++) {
			if (i == 0) {
				str += units.get(i).getKeyword();
			} else {
				str += toCamelString(units.get(i).getKeyword());
			}
		}
		return str;
	}

	private Unit createUnitWithCamelStringForClassOrInterface(List<Unit> units) {
    if(units.size()==1 && units.get(0).getKeyword().equals("void")){
      return units.get(0);
    } else {
		  Unit any = new Unit();
		  any.setAnyValue(concatFullCamelString(units));
		  return any;
    }
	}

	private String concatFullCamelString(List<Unit> units) {
		String str = "";
		for (Unit unit : units) {
			str += toCamelString(unit.getKeyword());
		}
		return str;
	}

	private Unit createUnitWithNotCamelString(List<Unit> units) {
		Unit any = new Unit();
		any.setAnyValue(concatStringNoCamel(units));
		return any;
	}

	private String concatStringNoCamel(List<Unit> units) {
		String str = "";
		for (Unit unit : units) {
			str += unit.getKeyword();
		}
		return str;
	}

	private String toCamelString(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
