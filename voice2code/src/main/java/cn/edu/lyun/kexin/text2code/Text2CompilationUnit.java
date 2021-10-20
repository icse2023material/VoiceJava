package cn.edu.lyun.kexin.text2code;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.server.ExportException;
import java.util.*;

import cn.edu.lyun.util.Pair;
import cn.edu.lyun.util.StringHelper;
import javassist.bytecode.analysis.ControlFlow.Block;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import cn.edu.lyun.kexin.text2ast.ASTManager;
import cn.edu.lyun.kexin.text2ast.FieldAST;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleAST;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleNode;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleType;
import cn.edu.lyun.kexin.text2code.astskeleton.TypeNameMap;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.nfa.State;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;

public class Text2CompilationUnit {

	private CompilationUnit compilationUnit;
	private HoleAST holeAST;

	public Text2CompilationUnit() {
		this.compilationUnit = new CompilationUnit();
		this.holeAST = new HoleAST();
	}

	public HoleAST getHoleAST() {
		return this.holeAST;
	}

	public Node getCompilationUnit() {
		return this.compilationUnit;
	}

	public CompilationUnit generate(String text) {
		Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text);
		Node node = ASTManager.generate(pattern);
		System.out.println("[log] matched pattern name: " + pattern.getName());

		Pair<Pair<HoleNode, HoleNode>, List<Integer>> holePosition = this.holeAST.getCurrentHole();
		List<Integer> path = holePosition.getSecond();
		Pair<Node, Integer> parentNodeAndIndex = this.getParentNodeOfHole(path);
		int holeIndex = parentNodeAndIndex.getSecond();
		Pair<HoleNode, HoleNode> parentAndCurrentHole = holePosition.getFirst();
		HoleNode parentHole = parentAndCurrentHole.getFirst();
		HoleNode currentHole = parentAndCurrentHole.getSecond();
		HoleType parentHoleType = parentHole.getHoleType();
		HoleType parentOfParentHoleType = this.holeAST.getParentOfNode(path).getHoleType();
		String parentNodeClassStr = parentNodeAndIndex.getFirst().getClass().toString();
		parentNodeClassStr = parentNodeClassStr.substring(parentNodeClassStr.lastIndexOf(".") + 1);
		HoleNode parentOfParentHole = this.holeAST.getParentOfNode(path);

		switch (pattern.getName()) {
			case "moveNext":
				// delete current hole, move to next one
				parentHole.deleteHole(holeIndex);
				// TODO: small step move. Not syntax-directed.
				HoleNode holeNode = new HoleNode(HoleType.Undefined, true);
				parentOfParentHole.addChild(holeNode);
				break;
			case "package":
				CompilationUnit parentNode = (CompilationUnit) parentNodeAndIndex.getFirst();
				parentNode.setPackageDeclaration((PackageDeclaration) node);
				// update current hole
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.PackageDeclaration);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration, HoleType.TypeDeclaration });
				parentHole.addChild(holeNode);
				break;
			case "import":
				parentNode = (CompilationUnit) parentNodeAndIndex.getFirst();
				if (parentNode.getImports().size() == 0) {
					NodeList<ImportDeclaration> importNodeList = new NodeList<ImportDeclaration>();
					importNodeList.add((ImportDeclaration) node);
					parentNode.setImports(importNodeList);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Wrapper);
					holeNode = new HoleNode(HoleType.ImportDeclaration, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration });
					currentHole.addChild(holeNode);

					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration });
					currentHole.addChild(holeNode);
				} else {
					parentNode.addImport((ImportDeclaration) node);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.ImportDeclaration);
					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration });
					parentHole.addChild(holeNode);
				}
				break;
			case "interface":
				parentNode = (CompilationUnit) parentNodeAndIndex.getFirst();
				break;
			case "class": // Note: class and interface belongs to TypeDeclaration.
				parentNode = (CompilationUnit) parentNodeAndIndex.getFirst();
				if (parentHole.getHoleType().equals(HoleType.Wrapper)) {
					parentNode.addType((ClassOrInterfaceDeclaration) node);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.TypeDeclaration);
					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
					parentHole.addChild(holeNode);
				} else {
					NodeList<TypeDeclaration<?>> nodeList = new NodeList<TypeDeclaration<?>>();
					nodeList.add((ClassOrInterfaceDeclaration) node);
					parentNode.setTypes(nodeList);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Wrapper);

					holeNode = new HoleNode(HoleType.TypeDeclaration, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeDeclaration });
					currentHole.addChild(holeNode);

					// class's body as a hole.
					HoleNode childHoleNode = new HoleNode(HoleType.Undefined, true);
					childHoleNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
					holeNode.addChild(childHoleNode);
				}
				break;
			case "constructor":
				break;
			case "method":
				ClassOrInterfaceDeclaration pNode = (ClassOrInterfaceDeclaration) parentNodeAndIndex.getFirst();
				pNode.addMember((BodyDeclaration<?>) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.MethodDeclaration);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeExtends });
				currentHole.addChild(holeNode);
				break;
			case "arrowFunction":
				break;
			case "field":
				if (parentHoleType.equals(HoleType.TypeDeclaration)) {
					// real field
					pNode = (ClassOrInterfaceDeclaration) parentNodeAndIndex.getFirst();
					pNode.addMember((BodyDeclaration<?>) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.FieldDeclaration);
					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					currentHole.addChild(holeNode);
				} else if (parentHoleType.equals(HoleType.MethodDeclaration)) {
					// variable declaration inside body. i.e. VariableDeclarationExpr
					// regenerate VariableDeclarationExpr.
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
					MethodDeclaration mNode = (MethodDeclaration) parentNodeAndIndex.getFirst();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Body);

					HoleNode anotherCurrentHole = new HoleNode();
					currentHole.addChild(anotherCurrentHole);

					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();

					if (statements.size() == 0) {
						statements.add((Statement) node);

						anotherCurrentHole.setIsHole(false);
						anotherCurrentHole.setHoleType(HoleType.Wrapper);

						holeNode = new HoleNode(HoleType.Statement, false);
						holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
						anotherCurrentHole.addChild(holeNode);

						HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
						holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
						holeNode.addChild(holeNodeChild);
					} else {
						System.out.println("Should not go to this branch");
					}
				} else if (parentHoleType.equals(HoleType.Wrapper) && parentOfParentHoleType.equals(HoleType.Body)) {
					// variable declaration inside body. i.e. VariableDeclarationExpr
					// regenerate VariableDeclarationExpr.
					// TODO: later may according to index to insert to specific location.
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
					BlockStmt blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					NodeList<Statement> statements = blockStmt.getStatements();

					statements.add((Statement) node);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Statement);
					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					currentHole.addChild(holeNodeChild);
				} else if (parentNodeClassStr.equals("ForStmt")) {
					ForStmt forStmt = (ForStmt) parentNodeAndIndex.getFirst();
					ExpressionStmt expressionStmt = (ExpressionStmt) new FieldAST().generateVariableDeclarationExpr(pattern);
					NodeList<Expression> initializationList = new NodeList<Expression>();
					initializationList.add(expressionStmt.getExpression());
					forStmt.setInitialization(initializationList);

					currentHole.setHoleType(HoleType.Wrapper);
					currentHole.setIsHole(false);

					HoleNode initializer = new HoleNode();
					initializer.setHoleType(HoleType.ForInitialization);
					initializer.setIsHole(false);
					currentHole.addChild(initializer);

					HoleNode childNode = new HoleNode();
					childNode.setHoleType(HoleType.Expression);
					childNode.setIsHole(true);
					initializer.addChild(childNode);
				}
				break;
			case "typeExtends":
				MethodDeclaration mNode = (MethodDeclaration) parentNodeAndIndex.getFirst();
				mNode.setType((Type) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.TypeExtends);
				holeNode = new HoleNode(HoleType.TypeVariale, true);
				holeNode.setHoleTypeOptions(new HoleType[] {});
				parentHole.addChild(holeNode);
				break;
			case "typeVariable":
				mNode = (MethodDeclaration) parentNodeAndIndex.getFirst();
				if (parentHole.getHoleType().equals(HoleType.Wrapper)) {
					NodeList<Parameter> nodeList = mNode.getParameters();
					nodeList.add((Parameter) node);
					mNode.setParameters(nodeList);

					currentHole.setIsHole(false);

					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeVariale, HoleType.BodyDeclaration });
					parentHole.addChild(holeNode);
				} else {
					NodeList<Parameter> nodeList = new NodeList<Parameter>();
					nodeList.add((Parameter) node);
					mNode.setParameters(nodeList);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Wrapper);

					holeNode = new HoleNode(HoleType.TypeVariale, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeVariale, HoleType.BodyDeclaration });
					currentHole.addChild(holeNode);

					holeNode = new HoleNode(HoleType.TypeVariale, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeVariale, HoleType.BodyDeclaration });
					currentHole.addChild(holeNode);

				}
				break;
			case "for":
				if (parentHoleType.equals(HoleType.MethodDeclaration)) {
					mNode = (MethodDeclaration) parentNodeAndIndex.getFirst();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Body);
					HoleNode anotherCurrentHole = new HoleNode();
					currentHole.addChild(anotherCurrentHole);
					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					if (statements.size() == 0) {
						statements.add((Statement) node);

						anotherCurrentHole.setIsHole(false);
						anotherCurrentHole.setHoleType(HoleType.Wrapper);

						holeNode = new HoleNode(HoleType.Statement, false);
						holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
						anotherCurrentHole.addChild(holeNode);

						HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
						holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
						holeNode.addChild(holeNodeChild);
					}
				} else if (parentHoleType.equals(HoleType.Wrapper) && parentOfParentHoleType.equals(HoleType.Body)) {
					BlockStmt blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					NodeList<Statement> statements = blockStmt.getStatements();
					statements.add((Statement) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Statement);
					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					currentHole.addChild(holeNodeChild);
				}
				break;
			case "while":
				BlockStmt blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
				NodeList<Statement> statements = blockStmt.getStatements();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Statement);
				HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
				holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(holeNodeChild);
				break;
			case "if":
				blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
				statements = blockStmt.getStatements();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Statement);
				holeNodeChild = new HoleNode(HoleType.Expression, true);
				holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(holeNodeChild);
				break;
			case "switch":
				break;
			case "tryCatch":
				break;
			case "override":
				break;
			case "subexpression":
				break;
			case "break":
				break;
			case "continue":
				break;
			case "newInstance":
				break;
			case "throw":
				break;
			case "let1":
				if (parentNodeClassStr.equals("WhileStmt")) {
					WhileStmt whileStmt = (WhileStmt) parentNodeAndIndex.getFirst();
					Statement body = whileStmt.getBody();
					String bodyClassStr = body.getClass().toString();
					bodyClassStr = bodyClassStr.substring(bodyClassStr.lastIndexOf(".") + 1);
					if (bodyClassStr.equals("ReturnStmt")) {
						blockStmt = new BlockStmt();
						statements = new NodeList<Statement>();
						ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
						statements.add(expressionStmt);
						blockStmt.setStatements(statements);
						whileStmt.setBody(blockStmt);

						currentHole.setHoleType(HoleType.Body);
						currentHole.setIsHole(false);

						HoleNode anotherCurrentHole = new HoleNode();
						anotherCurrentHole.setHoleType(HoleType.Wrapper);
						anotherCurrentHole.setIsHole(false);
						currentHole.addChild(anotherCurrentHole);

						HoleNode childNode = new HoleNode(HoleType.Statement, false);
						anotherCurrentHole.addChild(childNode);

						HoleNode newHole = new HoleNode(HoleType.Statement, true);
						anotherCurrentHole.addChild(newHole);
					} else {
					}
				}
				break;
			case "let2":
				if (parentNodeClassStr.equals("WhileStmt")) {
					WhileStmt whileStmt = (WhileStmt) parentNodeAndIndex.getFirst();
					Statement body = whileStmt.getBody();
					String bodyClassStr = body.getClass().toString();
					bodyClassStr = bodyClassStr.substring(bodyClassStr.lastIndexOf(".") + 1);
					if (bodyClassStr.equals("ReturnStmt")) {
						blockStmt = new BlockStmt();
						statements = new NodeList<Statement>();
						ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
						statements.add(expressionStmt);
						blockStmt.setStatements(statements);
						whileStmt.setBody(blockStmt);

						currentHole.setHoleType(HoleType.Body);
						currentHole.setIsHole(false);

						HoleNode anotherCurrentHole = new HoleNode();
						anotherCurrentHole.setHoleType(HoleType.Wrapper);
						anotherCurrentHole.setIsHole(false);
						currentHole.addChild(anotherCurrentHole);

						HoleNode childNode = new HoleNode(HoleType.Statement, false);
						anotherCurrentHole.addChild(childNode);

						HoleNode newHole = new HoleNode(HoleType.Statement, true);
						anotherCurrentHole.addChild(newHole);
					}
				} else if (parentNodeClassStr.equals("BlockStmt")) {
					blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					statements = blockStmt.getStatements();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);

					statements.add(expressionStmt);

					currentHole.setIsHole(false);
					holeNode = new HoleNode(HoleType.Statement, true);
					parentHole.addChild(holeNode);
				}
				break;
			case "let3":
				if (parentNodeClassStr.equals("BlockStmt")) {
					blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					statements = blockStmt.getStatements();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);

					statements.add(expressionStmt);

					currentHole.setIsHole(false);
					holeNode = new HoleNode(HoleType.Statement, true);
					parentHole.addChild(holeNode);
				}
				break;
			case "let4":
				if (parentNodeClassStr.equals("BlockStmt")) {
					blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					statements = blockStmt.getStatements();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);

					statements.add(expressionStmt);

					currentHole.setIsHole(false);
					holeNode = new HoleNode(HoleType.Statement, true);
					parentHole.addChild(holeNode);
				}
				break;
			case "let5":
				if (parentNodeClassStr.equals("BlockStmt")) {
					blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					statements = blockStmt.getStatements();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);

					statements.add(expressionStmt);

					currentHole.setIsHole(false);
					holeNode = new HoleNode(HoleType.Statement, true);
					parentHole.addChild(holeNode);
				}
				break;
			case "let6":
				if (parentNodeClassStr.equals("ForStmt")) {
					// Note: we only support BlockStmt.
					// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
					// sum = sum + i in for(; i < 10 ;){ ;sum = sum + i; }
					ForStmt forStmt = (ForStmt) parentNodeAndIndex.getFirst();
					Statement body = forStmt.getBody();
					String bodyClassStr = body.getClass().toString();
					bodyClassStr = bodyClassStr.substring(bodyClassStr.lastIndexOf(".") + 1);
					if (bodyClassStr.equals("ReturnStmt")) {
						blockStmt = new BlockStmt();
						statements = new NodeList<Statement>();
						ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
						statements.add(expressionStmt);
						blockStmt.setStatements(statements);
						forStmt.setBody(blockStmt);

						currentHole.setHoleType(HoleType.Body);
						currentHole.setIsHole(false);

						HoleNode anotherCurrentHole = new HoleNode();
						anotherCurrentHole.setHoleType(HoleType.Wrapper);
						anotherCurrentHole.setIsHole(false);
						currentHole.addChild(anotherCurrentHole);

						HoleNode childNode = new HoleNode();
						childNode.setHoleType(HoleType.Statement);
						childNode.setIsHole(false);
						anotherCurrentHole.addChild(childNode);

						HoleNode anotherChildNode = new HoleNode();
						anotherChildNode.setHoleType(HoleType.Expression);
						anotherChildNode.setIsHole(true);
						childNode.addChild(anotherChildNode);

					} else if (bodyClassStr.equals("BlockStmt")) {

					} else {
						System.out.println("Should not go to this branch");
					}
				} else if (parentNodeClassStr.equals("BlockStmt")) {
					blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					statements = blockStmt.getStatements();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.setIsHole(false);
					holeNode = new HoleNode(HoleType.Expression, true);
					currentHole.addChild(holeNode);
				}
				break;
			case "return1":
				break;
			case "return2":
				break;
			case "return3":
				if (parentNodeClassStr.equals("BlockStmt")) {
					blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					statements = blockStmt.getStatements();
					statements.add((Statement) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Statement);
					HoleNode childHoleNode = new HoleNode(HoleType.Undefined, true);
					parentHole.addChild(childHoleNode);
				}
				break;
			case "return4":
				break;
			case "return5":
				break;
			case "return6":
				break;
			case "expr1":
				break;
			case "expr2":
				break;
			case "expr3":
				break;
			case "expr4":
				break;
			case "expr5":
				if (parentNodeClassStr.equals("FieldDeclaration")) {
					NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parentNodeAndIndex.getFirst())
							.getVariables();
					VariableDeclarator vNode = variableDeclarators.get(0);
					vNode.setInitializer((Expression) node);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.VariableDeclarator);
					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
					parentOfParentHole.addChild(holeNode);
				} else if (parentNodeClassStr.equals("ExpressionStmt")) {
					ExpressionStmt expressionStmt = (ExpressionStmt) parentNodeAndIndex.getFirst();
					String expressionClassStr = StringHelper.getClassName(expressionStmt.getExpression().getClass().toString());
					if (expressionClassStr.equals("VariableDeclarationExpr")) {
						VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
						NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
						variableDeclarators.get(0).setInitializer((Expression) node);

						currentHole.setIsHole(false);
						currentHole.setHoleType(HoleType.Expression);

						holeNode = new HoleNode(HoleType.Undefined, true);
						holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
						parentOfParentHole.addChild(holeNode);

					} else if (expressionClassStr.equals("AssignExpr")) {
						AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
						assignExpr.setValue((Expression) node);
						currentHole.setIsHole(false);
						holeNode = new HoleNode(HoleType.Undefined, true);
						parentOfParentHole.addChild(holeNode);
					}
				} else if (parentNodeClassStr.equals("VariableDeclarationExpr")) {
					VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) parentNodeAndIndex.getFirst();
					NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
					variableDeclarators.get(0).setInitializer((Expression) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);

					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					parentOfParentHole.addChild(holeNode);
				}

				break;
			case "expr6":
				if (parentNodeClassStr.equals("ForStmt")) {
					// i++ in for(; ;i++){}
					ForStmt forStmt = (ForStmt) parentNodeAndIndex.getFirst();
					NodeList<Expression> expressions = new NodeList<Expression>();
					expressions.add((Expression) node);
					forStmt.setUpdate(expressions);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);
					holeNode = new HoleNode(HoleType.Undefined, true);
					parentHole.addChild(holeNode);
				} else if (parentNodeClassStr.equals("IfStmt")) {
					// i++ in if(){ i++;}
					IfStmt ifStmt = (IfStmt) parentNodeAndIndex.getFirst();
					Statement thenStmt = ifStmt.getThenStmt();
					String thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
					if (thenStmtStr.equals("ReturnStmt")) {
						blockStmt = new BlockStmt();
						statements = new NodeList<Statement>();
						ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
						statements.add(expressionStmt);
						blockStmt.setStatements(statements);
						ifStmt.setThenStmt(blockStmt);

						// [then, else, else], use a wrapper
						currentHole.setIsHole(false);
						currentHole.setHoleType(HoleType.Wrapper);

						holeNode = new HoleNode(HoleType.ThenStatement, false);
						currentHole.addChild(holeNode);
						holeNodeChild = new HoleNode(HoleType.Expression, false);
						holeNode.addChild(holeNodeChild);
						HoleNode holeNodeChildChild = new HoleNode(HoleType.Undefined, true);
						holeNode.addChild(holeNodeChildChild);
					} else if (thenStmtStr.equals("BlockStmt")) {
						blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
						statements = blockStmt.getStatements();
						ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
						statements.add(expressionStmt);
						currentHole.setIsHole(false);
						holeNode = new HoleNode(HoleType.Statement, true);
						parentHole.addChild(holeNode);
					}
				} else if (parentNodeClassStr.equals("BlockStmt")) {
					blockStmt = (BlockStmt) parentNodeAndIndex.getFirst();
					statements = blockStmt.getStatements();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.setIsHole(false);
					holeNode = new HoleNode(HoleType.Statement, true);
					parentHole.addChild(holeNode);
				}
				break;
			case "expr7":
				break;
			case "expr8":
				break;
			case "expr9":
				break;
			case "expr10":
				break;
			case "expr11":
				break;
			case "expr12":
				if (parentNodeClassStr.equals("ForStmt")) {
					// i < 10 in for(; i < 10 ;){}
					ForStmt forStmt = (ForStmt) parentNodeAndIndex.getFirst();
					forStmt.setCompare((Expression) node);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);

					holeNode = new HoleNode();
					holeNode.setIsHole(true);
					holeNode.setHoleType(HoleType.Undefined);

					parentHole.addChild(holeNode);
				} else if (parentNodeClassStr.equals("ExpressionStmt")) {
					// body part, expression
					ExpressionStmt expressionStmt = (ExpressionStmt) parentNodeAndIndex.getFirst();
					AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
					assignExpr.setValue((Expression) node);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);
					holeNode = new HoleNode();
					holeNode.setIsHole(true);
					holeNode.setHoleType(HoleType.Undefined);
					parentOfParentHole.addChild(holeNode);
				} else if (parentNodeClassStr.equals("WhileStmt")) {
					WhileStmt whileStmt = (WhileStmt) parentNodeAndIndex.getFirst();
					whileStmt.setCondition((Expression) node);
					currentHole.setIsHole(false);

					holeNode = new HoleNode(HoleType.Body, true);
					parentHole.addChild(holeNode);
				} else if (parentNodeClassStr.equals("IfStmt")) {
					IfStmt ifStmt = (IfStmt) parentNodeAndIndex.getFirst();

					if (parentHoleType.equals(HoleType.Wrapper) && (parentOfParentHoleType.equals(HoleType.Statement)
							|| parentOfParentHoleType.equals(HoleType.ThenStatement)
							|| parentOfParentHoleType.equals(HoleType.ElseStatement))) {
						// else branch
						IfStmt elseBranch = new IfStmt();
						elseBranch.setCondition((Expression) node);
						ifStmt.setElseStmt(elseBranch);
						currentHole.setIsHole(false);
						currentHole.setHoleType(HoleType.ElseStatement);

						holeNodeChild = new HoleNode(HoleType.Expression, false);
						currentHole.addChild(holeNodeChild);

						HoleNode holeNodeChild2 = new HoleNode(HoleType.Undefined, true);
						currentHole.addChild(holeNodeChild2);
					} else if (parentHoleType.equals(HoleType.Statement)) {
						// if condition
						ifStmt.setCondition((Expression) node);

						currentHole.setIsHole(false);
						currentHole.setHoleType(HoleType.Expression);
						holeNode = new HoleNode(HoleType.Undefined, true);
						parentHole.addChild(holeNode);

					}

				}
				break;
			case "subexpr1":
				break;
			case "subexpr2":
				break;
			case "subexpr3":
				break;
			case "subexpr4":
				break;
			case "subexpr5":
				break;
			case "subexpr6":
				break;
			case "subexpr7":
				break;
			case "subexpr8":
				break;
			case "subexpr9":
				break;
			case "subexpr10":
				break;
			case "subexpr11":
				break;
			case "subexpr12":

				break;
		}

		System.out.println("[log] write to file");
		this.lexicalPreseveToJavaFile();
		return this.compilationUnit;
	}

	public Pair<Node, Integer> getParentNodeOfHole(List<Integer> path) {
		int index;
		Node parent = this.compilationUnit;
		HoleNode parentHole = this.holeAST.getRoot();
		HoleNode parentOfParentHole = parentHole;
		for (index = 0; index < path.size() - 1; index++) {
			HoleNode temp = parentHole.getIthChild(path.get(index));
			parentOfParentHole = parentHole;
			parentHole = temp;

			HoleType holeType = parentHole.getHoleType();
			if (holeType.equals(HoleType.Wrapper)) {
				continue;
			}

			String name = TypeNameMap.map.get(holeType);

			Class parentClass = parent.getClass();
			Method method;
			try {
				int indexWithSameType = this.computeASTIndex(parentOfParentHole, parentHole, path.get(index));
				method = parentClass.getMethod(name);
				// Optional<NodeList> optional = (Optional<NodeList>) method.invoke(parent);
				try {
					NodeList nodeList = (NodeList) method.invoke(parent);
					parent = nodeList.get(indexWithSameType);

				} catch (Exception e) {
					try {
						List<?> nodeList = (List<?>) method.invoke(parent);
						parent = (Node) nodeList.get(indexWithSameType);
					} catch (Exception e2) {
						try {
							Optional<?> optionalData = (Optional<?>) method.invoke(parent);
							parent = (Node) optionalData.get();
						} catch (Exception e3) {
							parent = (Node) method.invoke(parent);
						}
					}
				}
				// if (optional.isEmpty()) {
				// System.out.println("something is wrong");
				// } else {
				// parent = optional.get().get(index);
				// }
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return new Pair<Node, Integer>(parent, path.get(index));
	}

	public void lexicalPreseveToJavaFile() {
		LexicalPreservingPrinter.setup(this.compilationUnit);
		FileWriter filewriter;
		try {
			filewriter = new FileWriter("Test.java");
			filewriter.write(compilationUnit.toString());
			filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int computeASTIndex(HoleNode parentOfParent, HoleNode parent, int currentIndex) {
		List<HoleNode> childList = parentOfParent.getChildList();
		int count = 0;
		for (int i = 0; i <= currentIndex; i++) {
			if (childList.get(i).getHoleType().equals(parent.getHoleType())) {
				count++;
			}
		}

		return --count;
	}
}
