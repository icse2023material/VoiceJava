package lyun.voice2code.pattern;

public class Pattern {
	private String str;
	private Unit[] units;

	public Pattern(String str) {
		this.str = str;
		// TODO: String to Unit[]
	}

	public Pattern(String str, Unit[] units) {
		this.str = str;
		this.units = units;
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
}
