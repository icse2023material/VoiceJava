package cn.edu.lyun.kexin.text2pattern.pattern;

import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;

public class PatternSet {
	private ArrayList<Pattern> patSet;

	public PatternSet() {
		patSet = new ArrayList<Pattern>();

		Unit Name = new Unit("plus", new Unit());

		Pattern packagePat = new Pattern("package", "define package [_]+ [dot [_]+]*",
				new Unit[] { new Unit("define"), new Unit("package"), Name, new Unit("asterisk", new Unit("dot"), Name) });
		patSet.add(packagePat);

		Pattern importPat = new Pattern("import", "import static? [_]+ [dot [[_]+|star]]*",
				new Unit[] { new Unit("import"), new Unit("question", new Unit("static")), Name,
						new Unit("asterisk", new Unit("dot"), new Unit("or", Name, new Unit())) });
		patSet.add(importPat);

		Unit classModifier = new Unit("or", new Unit("annotation"),
				new Unit("or", new Unit("public"),
						new Unit("or", new Unit("protected"),
								new Unit("or", new Unit("private"), new Unit("or", new Unit("abstract"),
										new Unit("or", new Unit("static"), new Unit("or", new Unit("final"), new Unit("strictfp"))))))));

		Pattern interfacePat = new Pattern("interface", "define [public|private]? interface [_]+",
				new Unit[] { new Unit("define"), new Unit("question", classModifier), new Unit("interface"), Name });
		patSet.add(interfacePat);

		Pattern classPat = new Pattern("class",
				"define [Annotation|public|protected|private|abstract|static|final|strictfp]* class [_]+ [extends [_]+]? [implements [_]+]?",
				new Unit[] { new Unit("define"), new Unit("asterisk", classModifier), new Unit("class"), Name,
						new Unit("question", new Unit("extends"), Name), new Unit("question", new Unit("implements"), Name) });
		patSet.add(classPat);

		Pattern constructorPat = new Pattern("constructor", "define constructor",
				new Unit[] { new Unit("define"), new Unit("constructor") });
		patSet.add(constructorPat);

		Unit methodModifier = new Unit("or", new Unit("annotation"),
				new Unit("or", new Unit("public"),
						new Unit("or", new Unit("protected"),
								new Unit("or", new Unit("private"),
										new Unit("or", new Unit("abstract"),
												new Unit("or", new Unit("static"), new Unit("or", new Unit("final"), new Unit("or",
														new Unit("synchronized"), new Unit("or", new Unit("native"), new Unit("strictfp"))))))))));
		Pattern methodPat = new Pattern("method",
				"define [Annotation|public|protected|private|abstract|static|final|synchronized|native|strictfp]* function [_]+",
				new Unit[] { new Unit("define"), new Unit("asterisk", methodModifier), new Unit("function"), Name,
						 });
		patSet.add(methodPat);

		Pattern arrowFunctionPat = new Pattern("arrowFunction", "define arrow function",
				new Unit[] { new Unit("define"), new Unit("arrow"), new Unit("function") });
		patSet.add(arrowFunctionPat);

		// shoule be put before fieldPat
		Pattern forPat = new Pattern("for", "define [enhanced] for",
				new Unit[] { new Unit("define"), new Unit("question", new Unit("enhanced")), new Unit("for") });
		patSet.add(forPat);

		Pattern whilePat = new Pattern("while", "define [do]? while",
				new Unit[] { new Unit("define"), new Unit("question", new Unit("do")), new Unit("while") });
		patSet.add(whilePat);

		Pattern ifPat = new Pattern("if", "define if", new Unit[] { new Unit("define"), new Unit("if") });
		patSet.add(ifPat);

		Pattern switchPat = new Pattern("switch", "define switch", new Unit[] { new Unit("define"), new Unit("switch") });
		patSet.add(switchPat);

		Pattern tryCatchPat = new Pattern("tryCatch", "define try",
				new Unit[] { new Unit("define"), new Unit("try") });
		patSet.add(tryCatchPat);

    Pattern catchPat = new Pattern("Catch", "define catch", new Unit[]{new Unit("define"), new Unit("catch")});
    patSet.add(catchPat);

		Pattern atOverridePat = new Pattern("override", "define at override",
				new Unit[] { new Unit("define"), new Unit("at"), new Unit("override") });
		patSet.add(atOverridePat);

		Unit fieldModifier = new Unit("or", new Unit("annotation"),
				new Unit("or", new Unit("public"),
						new Unit("or", new Unit("protected"), new Unit("or", new Unit("private"), new Unit("or", new Unit("static"),
								new Unit("or", new Unit("final"), new Unit("or", new Unit("transient"), new Unit("volatile"))))))));
		Pattern fieldPat = new Pattern("field",
				"define [Annotation|public|protected|private|static|final|transient|volatile]* (list of [_]+ | [_]+ [dot [_]+]? [with [_]+ [and [_]+]*]?) variable [_]+ ",
				new Unit[] { new Unit("define"), new Unit("asterisk", fieldModifier), new Unit("variable"), Name });
		patSet.add(fieldPat);

    Unit typePatPat = 	new Unit("or", new Unit("normal", new Unit("list"), new Unit("normal", new Unit("of"), (Name))),
    new Unit(new Unit[] { Name, new Unit("question", new Unit("dot"), Name),
        new Unit("question", new Unit("with"),
            new Unit("or", 
              new Unit("normal", Name, new Unit("asterisk", new Unit("and"), Name)),
              new Unit("question", Name)))}));
		// 定义类型
		Pattern typePat = new Pattern("type",
				"type (list of [_]+  | [_]+ [dot [_]+]? [with [_]+ [and [_]+]*]?) [extends [_]+]?",
				new Unit[] { new Unit("type"),
				typePatPat,
				new Unit("question", new Unit("extends"), Name) });
		patSet.add(typePat);

		Pattern subExpressionPat = new Pattern("subexpression", "subexpression", new Unit[] { new Unit("subexpression") });
		patSet.add(subExpressionPat);

		Pattern breakPat = new Pattern("break", "break", new Unit[] { new Unit("break") });
		patSet.add(breakPat);

		Pattern continuePat = new Pattern("continue", "continue", new Unit[] { new Unit("continue") });
		patSet.add(continuePat);

		Pattern newInstancePat = new Pattern("newInstance", "new instance (list of [_]+  | [_]+ [dot [_]+]? [with [_]+ [and [_]+]*]?)",
				new Unit[] { new Unit("new"), new Unit("instance"), typePatPat });
		patSet.add(newInstancePat);

		Pattern throwDeclPat = new Pattern("throwDecl", "throw [_]+", new Unit[] { new Unit("throw"),  Name });
    patSet.add(throwDeclPat);

		Pattern throwPat = new Pattern("throw", "throw new [_]+", new Unit[] { new Unit("throw"), new Unit("new"), Name });
		patSet.add(throwPat);

		Pattern moveNextBodyPat = new Pattern("moveNextBody", "move next body", new Unit[] { new Unit("move"), new Unit("next"), new Unit("body") });
    patSet.add(moveNextBodyPat);

		Pattern moveNextPat = new Pattern("moveNext", "move next", new Unit[] { new Unit("move"), new Unit("next") });
		patSet.add(moveNextPat);

	Unit typeUnit = new Unit("or", new Unit("int"),
				new Unit("or", new Unit("byte"),
						new Unit("or", new Unit("short"),
								new Unit("or", new Unit("long"), new Unit("or", new Unit("char"), new Unit("or", new Unit("float"),
										new Unit("or", new Unit("double"), new Unit("or", new Unit("boolean"), new Unit("string")))))))));

		Unit[] letUnits = new Unit[] { new Unit("let"), Name, new Unit("question", new Unit("dot"), Name),
				new Unit("equal") };

		Pattern letPat = new Pattern("let", "let [_]+ [dot [_]+]? equal [expression]? ",
				ArrayUtils.addAll(letUnits, new Unit[] { new Unit("question", new Unit("expression")) }));
		patSet.add(letPat);

    Pattern returnPat = new Pattern("return", "return [expression]?",
				new Unit[] { new Unit("return"), new Unit("question", new Unit("expression"))});
		patSet.add(returnPat);

		Unit opUnit = new Unit("or", new Unit("plus"), new Unit("or", new Unit("minus"),
				new Unit("or", new Unit("times"), new Unit("or", new Unit("divide"), new Unit("mod")))));
		Unit compareUnit = new Unit("or", new Unit("normal", new Unit("less"), new Unit("than")),
				new Unit("or", new Unit("normal", new Unit("less"), new Unit("equal")),
						new Unit("or", new Unit("normal", new Unit("greater"), new Unit("than")),
								new Unit("or", new Unit("normal", new Unit("greater"), new Unit("equal")),
										new Unit("or", new Unit("normal", new Unit("double"), new Unit("equal")),
												new Unit("or", new Unit("and"), 
                          new Unit("or", new Unit("normal", new Unit("double"), new Unit("or")), new Unit("normal", new Unit("double"), new Unit("and")))))))));

                        
    Pattern expr16Pat = new Pattern("expr16", "expression? not expression",
                        new Unit[] { new Unit("question", new Unit("expression")), new Unit("not"), new Unit("expression") });
    patSet.add(expr16Pat);

    Pattern expr17Pat = new Pattern("expr17", "expression? lambda expression", new Unit[] { new Unit("question", new Unit("expression")), new Unit("lambda"), new Unit("expression") });
    patSet.add(expr17Pat);


		Pattern expr15Pat = new Pattern("expr15", "conditional expression",
				new Unit[] { new Unit("conditional"), new Unit("expression") });
		patSet.add(expr15Pat);

    Pattern expr18pat = new Pattern("expr18", "expression? Name instance of type",new Unit[]{new Unit("question", new Unit("expression")), Name, new Unit("instance"), new Unit("of")});
    patSet.add(expr18pat);




		Pattern expr1Pat = new Pattern("expr1", "[expression]? call [_]+",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("call"), Name });
		patSet.add(expr1Pat);

		Pattern expr2Pat = new Pattern("expr2", "[expression]? [_]+ [dot [_]+]* [call [_]+]",
				new Unit[] { new Unit("question", new Unit("expression")), Name, new Unit("asterisk", new Unit("dot"), Name), new Unit("call"), Name });
		patSet.add(expr2Pat);

		Pattern expr14Pat = new Pattern("expr14", "[expression]? string [_]+",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("string"), Name });
		patSet.add(expr14Pat);

		Pattern expr10Pat = new Pattern("expr10", "[expression]? expression (op | compare) expression",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("expression"),
						new Unit("or", opUnit, compareUnit), new Unit("expression") });
		patSet.add(expr10Pat);

		Pattern expr5Pat = new Pattern("expr5",
				"[expression]? (int | byte | short | long | char | float | double | boolean | String) [_]+",
				new Unit[] { new Unit("question", new Unit("expression")), typeUnit, Name });
		patSet.add(expr5Pat);

 		Pattern expr19Pat = new Pattern("expr19cast", "cast expression",
		new Unit[] { new Unit("cast"), new Unit("expression") });
		patSet.add(expr19Pat);


		// expr13 must be put before expr4
		Pattern expr13Pat = new Pattern("expr13", "[expression]? variable [_]+ index [_]+", new Unit[] {
				new Unit("question", new Unit("expression")), new Unit("variable"), Name, new Unit("index"), Name });
		patSet.add(expr13Pat);

		Pattern expr4Pat = new Pattern("expr4", "[expression]? variable [_]+ ",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("normal", new Unit("variable")), Name });
		patSet.add(expr4Pat);

		Pattern expr6Pat = new Pattern("expr6", "[expression]? [_]+ plus plus",
				new Unit[] { new Unit("question", new Unit("expression")), Name, new Unit("plus"), new Unit("plus") });
		patSet.add(expr6Pat);

		Pattern expr7Pat = new Pattern("expr7", "[expression]? [_]+ minus minus",
				new Unit[] { new Unit("question", new Unit("expression")), Name, new Unit("minus"), new Unit("minus") });
		patSet.add(expr7Pat);

		Pattern expr8Pat = new Pattern("expr8", "[expression]? plus plus [_]+",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("plus"), new Unit("plus"), Name });
		patSet.add(expr8Pat);

		Pattern expr9Pat = new Pattern("expr9", "[expression]? minus minus [_]+",
				new Unit[] { new Unit("question", new Unit("expression")), new Unit("minus"), new Unit("minus"), Name });
		patSet.add(expr9Pat);

		Pattern expr0Pat = new Pattern("expr0", "[expression]? [dot [_]+]",
				new Unit[] { new Unit("dot"), Name });
		patSet.add(expr0Pat);


		Pattern expr3Pat = new Pattern("expr3", "[expression]? [_]+ [dot [_]+]*",
				new Unit[] { new Unit("question", new Unit("expression")), Name, new Unit("asterisk", new Unit("dot"), Name) });
		patSet.add(expr3Pat);

		// Pattern jumpOutPat = new Pattern("jumpOut", "jump out", new Unit[] { new Unit("jump"), new Unit("out") });
		// patSet.add(jumpOutPat);

		// Pattern jumpBeforePat = new Pattern("jumpBefore", "jump before _",
		// 		new Unit[] { new Unit("jump"), new Unit("before"), new Unit() });
		// patSet.add(jumpBeforePat);

		// Pattern jumpAfterPat = new Pattern("jumpAfter", "jump after _",
		// 		new Unit[] { new Unit("jump"), new Unit("after"), new Unit() });
		// patSet.add(jumpAfterPat);

		// Pattern jumpToPat = new Pattern("jumpToLine", "jump to line [_]? [start | end]?",
		// 		new Unit[] { new Unit("jump"), new Unit("to"), new Unit("line"), new Unit("question", new Unit()),
		// 				new Unit("question", new Unit("or", new Unit("start"), new Unit("end"))) });
		// patSet.add(jumpToPat);
		// Pattern upPat = new Pattern("up", "up [_ lines]?",
		// 		new Unit[] { new Unit("up"), new Unit("question", new Unit("normal", new Unit(), new Unit("lines"))) });
		// patSet.add(upPat);
		// Pattern downPat = new Pattern("down", "down [_ lines]?",
		// 		new Unit[] { new Unit("down"), new Unit("question", new Unit("normal", new Unit(), new Unit("lines"))) });
		// patSet.add(downPat);
		// Pattern leftPat = new Pattern("left", "left", new Unit[] { new Unit("left") });
		// patSet.add(leftPat);
		// Pattern rightPat = new Pattern("right", "right", new Unit[] { new Unit("right") });
		// patSet.add(rightPat);
		// Pattern selectLinePat = new Pattern("selectLine", "select line",
		// 		new Unit[] { new Unit("select"), new Unit("line") });
		// patSet.add(selectLinePat);
		// Pattern selectBodyPat = new Pattern("selectBody", "select body",
		// 		new Unit[] { new Unit("select"), new Unit("body") });
		// patSet.add(selectBodyPat);
		// Pattern selectNamePat = new Pattern("selectName", "select _", new Unit[] { new Unit("select"), new Unit() });
		// patSet.add(selectNamePat);
		// Pattern selectFunctionPat = new Pattern("selectFunction", "select function [_]?",
		// 		new Unit[] { new Unit("select"), new Unit("function"), new Unit("question", new Unit()) });
		// patSet.add(selectFunctionPat);
		// Pattern replacePat = new Pattern("replace", "replace _ to _",
		// 		new Unit[] { new Unit("replace"), new Unit(), new Unit("to"), new Unit() });
		// patSet.add(replacePat);
		// Pattern deletePat = new Pattern("delete", "delete", new Unit[] { new Unit("delete") });
		// patSet.add(deletePat);
	}

	public void addToSet(Pattern pat) {
		patSet.add(pat);
	}

	public int size() {
		return patSet.size();
	}

	public ArrayList<Pattern> getPatternSet() {
		return this.patSet;
	}

	@Override
	public String toString() {
		String str = "";
		for (Pattern pattern : patSet) {
			str = str != "" ? str + "\n" + pattern.toVoiceJavaPattern() : str + pattern.toVoiceJavaPattern();
		}

		return str;
	}

}
