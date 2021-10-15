package cn.edu.lyun.kexin.text2pattern.pattern;

public class Pattern {
	private String name; // a specific name for the pattern
	private String str;
	private Unit[] units;

	public Pattern(String name, String str) {
		this.name = name;
		this.str = str;
		// TODO: String to Unit[]
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
}
