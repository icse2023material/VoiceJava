package lyun.voice2code;

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
