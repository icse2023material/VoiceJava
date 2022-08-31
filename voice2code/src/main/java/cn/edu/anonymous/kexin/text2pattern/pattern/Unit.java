package cn.edu.anonymous.kexin.text2pattern.pattern;

public class Unit {
	private String type; // any, plus, or, asterisk, keyword, question, normal, list
	private String keyword; // type = keyword
	private Unit first;
	private Unit second;
	private Unit[] list;

	// Initialize keyword
	public Unit(String keyword) {
		this.type = "keyword";
		this.keyword = keyword;
	}

	// Initialize any
	public Unit() {
		this.type = "any";
	}

	public void setAnyValue(String value) {
		if (this.type == "any") {
			this.keyword = value;
		} else {
			System.out.println("cannot set value except any type");
		}
	}

	// Initialize with one parameter
	public Unit(String type, Unit first) {
		this.type = type;
		this.first = first;
		this.second = null;
		this.list = null;
	}

	// Initialize with two parameter
	public Unit(String type, Unit first, Unit second) {
		this.type = type;
		this.first = first;
		this.second = second;
		this.list = null;
	}

	public Unit(Unit[] list) {
		this.type = "list";
		this.list = list;
		this.first = null;
		this.second = null;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public Unit getFirst() {
		return this.first;
	}

	public Unit getSecond() {
		return this.second;
	}

	public Unit[] getList() {
		return this.list;
	}

	@Override
	public String toString() {
		String str = "";
		if (type == "keyword") {
			str += "(Keyword " + keyword + ")";
			return str;
		} else if (type == "any") {
			str += "RegAnyExp ";
			return str;
		} else if (type == "or") {
			str += "RegOrExp ";
		} else if (type == "plus") {
			str += "RegPlusExp ";
		} else if (type == "asterisk") {
			str += "RegAsterRiskExp ";
		} else if (type == "question") {
			str += "RegQuestionExp ";
		} else if (type == "normal") {
			str += "";
		} else if (type == "list") {
			str += "";
		}
		if (first != null) {
			str += first.toString();
		}
		if (second != null) {
			str += " " + second.toString();
		}
		if (list != null) {
			for (Unit unit : list) {
				str += str != "" ? str + " " + unit.toString() : unit.toString();
			}
		}
		return "(" + str + ")";
	}

	public String toVoiceJavaPattern() {
		String str = "";
		switch (type) {
			case "keyword":
				str = str != "" ? str + " " + keyword : str + keyword;
				break;
			case "any":
				str = str != "" ? str + " _" : str + "_";
				break;
			case "or":
				String subStr = "[" + first.toVoiceJavaPattern() + " | " + second.toVoiceJavaPattern() + "]";
				str = str != "" ? str + " " + subStr : subStr;
				break;
			case "plus":
			case "asterisk":
			case "question":
			case "normal":
				subStr = "[" + (first != null ? first.toVoiceJavaPattern() : "")
						+ (second != null ? " " + second.toVoiceJavaPattern() : "") + "]";
				if (type == "star") {
					subStr += "*";
				} else if (type == "plus") {
					subStr += "+";
				} else if (type == "asterisk") {
					subStr += "*";
				} else if (type == "question") {
					subStr += "?";
				}
				str = str != "" ? str + " " + subStr : subStr;
				break;
			case "list":
				subStr = "";
				for (Unit unit : list) {
					subStr = subStr != "" ? subStr + " " + unit.toVoiceJavaPattern() : unit.toVoiceJavaPattern();
				}
				str = str != "" ? str + " " + subStr : subStr;
				break;
		}
		return str;
	}

	public String showInstance() {
		String str = "";
		switch (type) {
			case "keyword":
				str = str != "" ? str + " " + keyword : str + keyword;
				break;
			case "any":
				str = str != "" ? str + " " + keyword : str + keyword;
				break;
			case "or":
				String subStr = "[" + first.toVoiceJavaPattern() + " | " + second.toVoiceJavaPattern() + "]";
				str = str != "" ? str + " " + subStr : subStr;
				break;
			case "plus":
			case "asterisk":
			case "question":
			case "normal":
				subStr = "[" + (first != null ? first.toVoiceJavaPattern() : "")
						+ (second != null ? " " + second.toVoiceJavaPattern() : "") + "]";
				if (type == "star") {
					subStr += "*";
				} else if (type == "plus") {
					subStr += "+";
				} else if (type == "asterisk") {
					subStr += "*";
				} else if (type == "question") {
					subStr += "?";
				}
				str = str != "" ? str + " " + subStr : subStr;
				break;
			case "list":
				subStr = "";
				for (Unit unit : list) {
					subStr = subStr != "" ? subStr + " " + unit.toVoiceJavaPattern() : unit.toVoiceJavaPattern();
				}
				str = str != "" ? str + " " + subStr : subStr;
				break;
		}
		return str;
	}
}
