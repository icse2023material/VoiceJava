package cn.edu.lyun.kexin.text2code;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.text.WrappedPlainView;

import cn.edu.lyun.util.ListHelper;
import cn.edu.lyun.util.Pair;
import cn.edu.lyun.util.StringHelper;

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
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import cn.edu.lyun.kexin.text2ast.ASTManager;
import cn.edu.lyun.kexin.text2ast.FieldAST;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleAST;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleNode;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleType;
import cn.edu.lyun.kexin.text2code.astskeleton.TypeNameMap;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
import cn.edu.lyun.kexin.text2pattern.pattern.Pattern;
import cn.edu.lyun.kexin.text2pattern.pattern.PatternSet;

import io.vavr.control.Either;

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
		Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer> parentAndIndex = this.getParentOfHole(path);
		int holeIndex = parentAndIndex.getSecond();
		Pair<HoleNode, HoleNode> parentAndCurrentHole = holePosition.getFirst();
		HoleNode parentHole = parentAndCurrentHole.getFirst();
		HoleNode currentHole = parentAndCurrentHole.getSecond();
		HoleType parentHoleType = parentHole.getHoleType();
		Either<Node, Either<List<?>, NodeList<?>>> parent = parentAndIndex.getFirst();
		HoleNode parentOfParentHole = this.holeAST.getParentOfNode(path);
		HoleNode parentOfParentOfParentHole = this.holeAST.getParentOfParentOfParentNode(path);

		// the following code can be optimized.
		String parentNodeClassStr = null;
		if (parent.isLeft()) {
			parentNodeClassStr = StringHelper.getClassName(parent.getLeft().getClass().toString());
		}

		switch (pattern.getName()) {
		case "moveNext":
			HoleNode holeNode = new HoleNode(HoleType.Undefined, true);
			if (currentHole.getHoleType().equals(HoleType.TypeVariables)) {
				// delete current hole, move to next one
				parentHole.deleteHole(holeIndex);
				parentHole.addChild(holeNode);
			} else if (parentHoleType.equals(HoleType.SwitchEntries)) {
				// generate SwitchEntry Sketch
				NodeList<SwitchEntry> switchEntries = (NodeList<SwitchEntry>) parent.get().get();
				SwitchEntry switchEntry = new SwitchEntry();
				switchEntries.add(switchEntry);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Wrapper);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.SwitchEntry });

				holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptionsOfOnlyOne(HoleType.MoveNext);
				currentHole.addChild(holeNode);

				HoleNode holeNodeChild = new HoleNode(HoleType.Undefined, true);
				currentHole.addChild(holeNodeChild);

			} else if (parentHoleType.equals(HoleType.ElseStatement)) {
				// default else case
				BlockStmt blockStmt = new BlockStmt();
				IfStmt ifStmt = (IfStmt) parent.getLeft();
				ifStmt.setElseStmt(blockStmt);

				currentHole.set(HoleType.ElseStatement, false);

				// HoleNode bodyHole = new HoleNode(HoleType.Body, false);
				// currentHole.addChild(bodyHole);

				HoleNode stmtsHole = new HoleNode();
				stmtsHole.set(HoleType.Statements, false);
				currentHole.addChild(stmtsHole);

				HoleNode statementHole = new HoleNode();
				stmtsHole.addChild(statementHole);
			} else {
				// TODO: small step move. Not syntax-directed.
				parentHole.deleteHole(holeIndex);
				parentOfParentHole.addChild(holeNode);
			}
			break;
		case "package":
			CompilationUnit parentNode = null;
			if (parent.isLeft()) {
				parentNode = (CompilationUnit) parent.getLeft();
			} else {
				// TODO: shall not be other case.
			}
			parentNode.setPackageDeclaration((PackageDeclaration) node);
			// update current hole
			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.PackageDeclaration);
			holeNode = new HoleNode(HoleType.Undefined, true);
			holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration, HoleType.TypeDeclaration });
			parentHole.addChild(holeNode);
			break;
		case "import":
			parentNode = null;
			if (parent.isLeft()) {
				parentNode = (CompilationUnit) parent.getLeft();
				if (parentNode.getImports().size() == 0) {
					NodeList<ImportDeclaration> importNodeList = new NodeList<ImportDeclaration>();
					importNodeList.add((ImportDeclaration) node);
					parentNode.setImports(importNodeList);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.ImportDeclarations);
					holeNode = new HoleNode(HoleType.ImportDeclaration, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration });
					currentHole.addChild(holeNode);
					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration }); // under wrapper: should only be
					currentHole.addChild(holeNode);
				}
			} else {
				NodeList<ImportDeclaration> importDeclarations = (NodeList<ImportDeclaration>) parent.get().get();
				importDeclarations.add((ImportDeclaration) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.ImportDeclaration);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration });
				parentHole.addChild(holeNode);
			}
			break;
		case "interface":
			parentNode = (CompilationUnit) parent.getLeft();
			parentNode.addType((ClassOrInterfaceDeclaration) node);
			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.TypeDeclarations);

			holeNode = new HoleNode(HoleType.Wrapper, false);
			holeNode.setHoleTypeOptions(new HoleType[] { HoleType.InterfaceDeclaration });
			currentHole.addChild(holeNode);

			HoleNode childHoleNode0 = new HoleNode(HoleType.Undefined, true);
			childHoleNode0.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
			holeNode.addChild(childHoleNode0);
			break;
		case "class": // Note: class and interface belongs to TypeDeclaration.
			parentNode = null;
			if (parent.isLeft()) {
				parentNode = (CompilationUnit) parent.getLeft();
				parentNode.addType((ClassOrInterfaceDeclaration) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.TypeDeclarations);

				holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ClassDeclaration });
				currentHole.addChild(holeNode);

				HoleNode childHoleNode = new HoleNode(HoleType.Undefined, true);
				childHoleNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
				holeNode.addChild(childHoleNode);
			} else {
				NodeList<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = (NodeList<ClassOrInterfaceDeclaration>) parent
						.get().get();
				classOrInterfaceDeclarations.add((ClassOrInterfaceDeclaration) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Wrapper);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.ClassDeclaration });

				HoleNode childHoleNode = new HoleNode(HoleType.Undefined, true);
				childHoleNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
				currentHole.addChild(childHoleNode);
			}
			break;
		case "constructor":
			break;
		case "method":
			if (parentHoleType.equals(HoleType.BodyDeclarations)) {
				NodeList<BodyDeclaration<?>> bodyDeclarations = (NodeList<BodyDeclaration<?>>) parent.get().get();
				// Interface method, no body.
				if (((MethodDeclaration) bodyDeclarations.get(0)).getBody().isEmpty()) {
					((MethodDeclaration) node).removeBody();
				}
				bodyDeclarations.add((BodyDeclaration<?>) node);

				currentHole.set(HoleType.Wrapper, false);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.MethodDeclaration });
				holeNode = new HoleNode();
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeExtends });
				currentHole.addChild(holeNode);
			}
			if (parentNodeClassStr != null && parentNodeClassStr.equals("ClassOrInterfaceDeclaration")) {
				ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) parent.getLeft();
				// If Interface, no body
				if (classOrInterfaceDeclaration.isInterface()) {
					((MethodDeclaration) node).removeBody();
				}
				classOrInterfaceDeclaration.addMember((BodyDeclaration<?>) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.BodyDeclarations);

				holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.MethodDeclaration });
				currentHole.addChild(holeNode);

				HoleNode chilHoleNode = new HoleNode();
				chilHoleNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeExtends });
				holeNode.addChild(chilHoleNode);
			}
			break;
		case "arrowFunction":
			break;
		case "field":
			if (parentNodeClassStr != null && parentNodeClassStr.equals("ClassOrInterfaceDeclaration")
					&& parentHoleType.equals(HoleType.Wrapper)) {
				// real field
				ClassOrInterfaceDeclaration pNode = (ClassOrInterfaceDeclaration) parent.getLeft();
				pNode.addMember((BodyDeclaration<?>) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.BodyDeclarations);

				holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.FieldDeclaration });
				currentHole.addChild(holeNode);

				HoleNode chilHoleNode = new HoleNode(HoleType.Undefined, true);
				chilHoleNode.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				holeNode.addChild(chilHoleNode);
			} else if (parentHoleType.equals(HoleType.BodyDeclarations)) {
				NodeList<BodyDeclaration<?>> bodyDeclarations = (NodeList<BodyDeclaration<?>>) parent.get().get();
				bodyDeclarations.add((BodyDeclaration<?>) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Wrapper);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.FieldDeclaration });

				HoleNode chilHoleNode = new HoleNode(HoleType.Undefined, true);
				chilHoleNode.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(chilHoleNode);

			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				// variable declaration inside body. i.e. VariableDeclarationExpr
				// Regenerate VariableDeclarationExpr.
				node = new FieldAST().generateVariableDeclarationExpr(pattern);
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Wrapper, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					holeNode.addChild(holeNodeChild);
				} else {
					System.out.println("Should not go to this branch");
				}
				// } else if (parentHoleType.equals(HoleType.Wrapper) &&
				// parentOfParentHoleType.equals(HoleType.Body)) {
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				// variable declaration inside body. i.e. VariableDeclarationExpr
				// regenerate VariableDeclarationExpr.
				// TODO: later may according to index to insert to specific location.
				node = new FieldAST().generateVariableDeclarationExpr(pattern);
				BlockStmt blockStmt = (BlockStmt) parent.getLeft();
				NodeList<Statement> statements = blockStmt.getStatements();

				statements.add((Statement) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Statement);

				HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
				holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(holeNodeChild);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				ForStmt forStmt = (ForStmt) parent.getLeft();
				ExpressionStmt expressionStmt = (ExpressionStmt) new FieldAST().generateVariableDeclarationExpr(pattern);
				NodeList<Expression> initializationList = new NodeList<Expression>();
				initializationList.add(expressionStmt.getExpression());
				forStmt.setInitialization(initializationList);

				currentHole.setHoleType(HoleType.ForInitialization);
				currentHole.setIsHole(false);
				HoleNode childNode = new HoleNode();
				childNode.setHoleType(HoleType.Expression);
				childNode.setIsHole(true);
				currentHole.addChild(childNode);
			} else if (parentHoleType.equals(HoleType.Statements)) {

				node = new FieldAST().generateVariableDeclarationExpr(pattern);
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				statements.add((Statement) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Wrapper);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.Statement });

				HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
				holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(holeNodeChild);
			}
			break;
		case "typeExtends":
			if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				mNode.setType((Type) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.TypeExtends);
				holeNode = new HoleNode(HoleType.TypeVariables, true);
				holeNode.setHoleTypeOptions(new HoleType[] {});
				parentHole.addChild(holeNode);
			}
			break;
		case "typeVariable":
			if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				NodeList<Parameter> nodeList = new NodeList<Parameter>();
				nodeList.add((Parameter) node);
				mNode.setParameters(nodeList);

				currentHole.setIsHole(false);

				holeNode = new HoleNode(HoleType.TypeVariable, false);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeVariable });
				currentHole.addChild(holeNode);

				HoleNode holeNodeChild = new HoleNode(HoleType.Undefined, true);
				currentHole.addChild(holeNodeChild);
			} else if (parentHoleType.equals(HoleType.TypeVariables)) {
				NodeList<Parameter> nodeList = (NodeList<Parameter>) parent.get().get();
				nodeList.add((Parameter) node);
				currentHole.setHoleType(HoleType.TypeVariable);
				currentHole.setIsHole(false);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.TypeVariable });
				parentHole.addChild(holeNode);
			}

			break;
		case "for":
			HoleType holeTypeFor = HoleType.ForStmt;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				statements.add((Statement) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Wrapper);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.ForStmt });
				holeNode = new HoleNode(HoleType.Expression, true);
				currentHole.addChild(holeNode);
			} else if (parentHoleType.equals(HoleType.MethodDeclaration)) {
				MethodDeclaration mNode = (MethodDeclaration) parentAndIndex.getFirst();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Statement, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					holeNode.addChild(holeNodeChild);
				}
				// } else if (parentHoleType.equals(HoleType.Wrapper) &&
				// parentOfParentHoleType.equals(HoleType.Body)) {
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				BlockStmt blockStmt = (BlockStmt) parent.getLeft();
				NodeList<Statement> statements = blockStmt.getStatements();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Statement);
				HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
				holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(holeNodeChild);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt whileStmt = (WhileStmt) parent.getLeft();
				Statement body = whileStmt.getBody();
				String bodyClassStr = StringHelper.getClassName(body.getClass().toString());
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					whileStmt.setBody(blockStmt);

					currentHole.set(HoleType.Body, false);
					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.set(HoleType.Statements, false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(holeTypeFor);
					anotherCurrentHole.addChild(childNode);

					HoleNode newHole = new HoleNode(HoleType.Expression, true);
					childNode.addChild(newHole);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				ForStmt stmt = (ForStmt) parent.getLeft();
				Statement body = stmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					stmt.setBody(blockStmt);

					currentHole.setHoleType(HoleType.Body);
					currentHole.setIsHole(false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.setHoleType(HoleType.Statements);
					anotherCurrentHole.setIsHole(false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(holeTypeFor);
					anotherCurrentHole.addChild(childNode);

					HoleNode newHole = new HoleNode(HoleType.Expression, true);
					childNode.addChild(newHole);
				} else {
					// TODO
				}
			}
			break;
		case "while":
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// insert at holeIndex
					statements.add(holeIndex, (Statement) node);
				} else {
					// append
					statements.add((Statement) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Wrapper);
					currentHole.setHoleTypeOptions(new HoleType[] { HoleType.WhileStmt });
					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					currentHole.addChild(holeNodeChild);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				ForStmt forStmt = (ForStmt) parent.getLeft();
				Statement body = forStmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					forStmt.setBody(blockStmt);

					currentHole.set(HoleType.Body, false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.set(HoleType.Statements, false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode();
					childNode.set(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(HoleType.WhileStmt);
					anotherCurrentHole.addChild(childNode);

					HoleNode childOfChildNode = new HoleNode(HoleType.Expression, true);
					childNode.addChild(childOfChildNode);
				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt whileStmt = (WhileStmt) parent.getLeft();
				Statement body = whileStmt.getBody();
				String bodyClassStr = StringHelper.getClassName(body.getClass().toString());
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					whileStmt.setBody(blockStmt);

					currentHole.setHoleType(HoleType.Body);
					currentHole.setIsHole(false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.setHoleType(HoleType.Statements);
					anotherCurrentHole.setIsHole(false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(HoleType.WhileStmt);
					anotherCurrentHole.addChild(childNode);

					HoleNode newHole = new HoleNode(HoleType.Expression, true);
					childNode.addChild(newHole);
				} else {
					// TODO
				}
			}
			break;
		case "if":
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// insert at holeIndex
					statements.add(holeIndex, (Statement) node);
				} else {
					// append
					statements.add((Statement) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Wrapper);
					currentHole.setHoleTypeOptions(new HoleType[] { HoleType.IfStmt });
					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					currentHole.addChild(holeNodeChild);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				ForStmt forStmt = (ForStmt) parent.getLeft();
				Statement body = forStmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					forStmt.setBody(blockStmt);

					currentHole.set(HoleType.Body, false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.set(HoleType.Statements, false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode();
					childNode.set(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(HoleType.IfStmt);
					anotherCurrentHole.addChild(childNode);

					HoleNode childOfChildNode = new HoleNode(HoleType.Expression, true);
					childNode.addChild(childOfChildNode);
				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt whileStmt = (WhileStmt) parent.getLeft();
				Statement body = whileStmt.getBody();
				String bodyClassStr = StringHelper.getClassName(body.getClass().toString());
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					whileStmt.setBody(blockStmt);

					currentHole.setHoleType(HoleType.Body);
					currentHole.setIsHole(false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.setHoleType(HoleType.Statements);
					anotherCurrentHole.setIsHole(false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(HoleType.IfStmt);
					anotherCurrentHole.addChild(childNode);

					HoleNode newHole = new HoleNode(HoleType.Expression, true);
					childNode.addChild(newHole);
				} else {
					// TODO
				}
			}
			break;
		case "switch":
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// insert at holeIndex
					statements.add(holeIndex, (Statement) node);
				} else {
					// append
					statements.add((Statement) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Wrapper);
					currentHole.setHoleTypeOptions(new HoleType[] { HoleType.SwitchStmt });
					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					currentHole.addChild(holeNodeChild);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				ForStmt forStmt = (ForStmt) parent.getLeft();
				Statement body = forStmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					forStmt.setBody(blockStmt);

					currentHole.set(HoleType.Body, false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.set(HoleType.Statements, false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode();
					childNode.set(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(HoleType.SwitchStmt);
					anotherCurrentHole.addChild(childNode);

					HoleNode childOfChildNode = new HoleNode(HoleType.Expression, true);
					childNode.addChild(childOfChildNode);
				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt whileStmt = (WhileStmt) parent.getLeft();
				Statement body = whileStmt.getBody();
				String bodyClassStr = StringHelper.getClassName(body.getClass().toString());
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					whileStmt.setBody(blockStmt);

					currentHole.set(HoleType.Body, false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.set(HoleType.Statements, false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode();
					childNode.set(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(HoleType.SwitchStmt);
					anotherCurrentHole.addChild(childNode);

					HoleNode childOfChildNode = new HoleNode(HoleType.Expression, true);
					childNode.addChild(childOfChildNode);
				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			}
			break;
		case "tryCatch":
			break;
		case "override":
			break;
		case "subexpression":
			break;
		case "break":
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					statements.add((Statement) node);
					currentHole.set(HoleType.Break, false);

					holeNode = new HoleNode();
					// not correct exactly
					parentHole.addChild(holeNode);
				}
			}
			break;
		case "continue":
			break;
		case "newInstance":
			if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				ExpressionStmt expressionStmt = (ExpressionStmt) parent.getLeft();
				VariableDeclarationExpr expression = (VariableDeclarationExpr) expressionStmt.getExpression();
				NodeList<VariableDeclarator> variableDeclarators = expression.getVariables();
				variableDeclarators.get(0).setInitializer((Expression) node);

				currentHole.set(HoleType.Expression, false);
				holeNode = new HoleNode(HoleType.VariableDeclarator, false);
				currentHole.addChild(holeNode);

				HoleNode childHoleNode = new HoleNode(HoleType.Wrapper, false);
				childHoleNode.setHoleTypeOptionsOfOnlyOne(HoleType.VariableDeclarator);
				holeNode.addChild(childHoleNode);

				HoleNode childOfChildNode = new HoleNode(HoleType.VariableInitializer, false);
				childHoleNode.addChild(childOfChildNode);

				HoleNode newHole = new HoleNode();
				childOfChildNode.addChild(newHole);
			}
			break;
		case "throw":
			break;
		case "let1":
			HoleType holeTypeExpr = HoleType.Let1Expr;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.setIsHole(false);
					holeNode = new HoleNode();
					parentHole.addChild(holeNode);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt whileStmt = (WhileStmt) parent.getLeft();
				Statement body = whileStmt.getBody();
				String bodyClassStr = StringHelper.getClassName(body.getClass().toString());
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					blockStmt.setStatements(statements);
					whileStmt.setBody(blockStmt);

					currentHole.setHoleType(HoleType.Body);
					currentHole.setIsHole(false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.setHoleType(HoleType.Statements);
					anotherCurrentHole.setHoleTypeOptions(new HoleType[] { HoleType.BlockStmt });
					anotherCurrentHole.setIsHole(false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode(HoleType.Statement, false);
					anotherCurrentHole.addChild(childNode);

					HoleNode newHole = new HoleNode(HoleType.Statement, true);
					anotherCurrentHole.addChild(newHole);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				Optional<BlockStmt> optionalBody = mNode.getBody();
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Body);

				HoleNode anotherCurrentHole = new HoleNode();
				currentHole.addChild(anotherCurrentHole);

				BlockStmt blockStmt = optionalBody.get();
				NodeList<Statement> statements = blockStmt.getStatements();
				if (statements.size() == 0) {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);

					anotherCurrentHole.setIsHole(false);
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Expression, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					anotherCurrentHole.addChild(holeNodeChild);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			}
			break;
		case "let2":
			holeTypeExpr = HoleType.Let2Expr;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.setIsHole(false);
					holeNode = new HoleNode();
					parentHole.addChild(holeNode);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt whileStmt = (WhileStmt) parentAndIndex.getFirst();
				Statement body = whileStmt.getBody();
				String bodyClassStr = StringHelper.getClassName(body.getChildNodes().toString());
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
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
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				BlockStmt blockStmt = (BlockStmt) parent.getLeft();
				NodeList<Statement> statements = blockStmt.getStatements();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);

				currentHole.setIsHole(false);
				holeNode = new HoleNode(HoleType.Statement, true);
				parentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				Optional<BlockStmt> optionalBody = mNode.getBody();
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Body);

				HoleNode anotherCurrentHole = new HoleNode();
				currentHole.addChild(anotherCurrentHole);

				BlockStmt blockStmt = optionalBody.get();
				NodeList<Statement> statements = blockStmt.getStatements();
				if (statements.size() == 0) {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);

					anotherCurrentHole.setIsHole(false);
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Expression, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			}
			break;
		case "let3":
			holeTypeExpr = HoleType.Let3Expr;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.setIsHole(false);
					holeNode = new HoleNode();
					parentHole.addChild(holeNode);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				BlockStmt blockStmt = (BlockStmt) parentAndIndex.getFirst();
				NodeList<Statement> statements = blockStmt.getStatements();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);

				statements.add(expressionStmt);

				currentHole.setIsHole(false);
				holeNode = new HoleNode(HoleType.Statement, true);
				parentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				Optional<BlockStmt> optionalBody = mNode.getBody();
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Body);

				HoleNode anotherCurrentHole = new HoleNode();
				currentHole.addChild(anotherCurrentHole);

				BlockStmt blockStmt = optionalBody.get();
				NodeList<Statement> statements = blockStmt.getStatements();
				if (statements.size() == 0) {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);

					anotherCurrentHole.setIsHole(false);
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Expression, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			}
			break;
		case "let4":
			holeTypeExpr = HoleType.Let4Expr;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.setIsHole(false);
					holeNode = new HoleNode();
					parentHole.addChild(holeNode);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				BlockStmt blockStmt = (BlockStmt) parentAndIndex.getFirst();
				NodeList<Statement> statements = blockStmt.getStatements();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);

				statements.add(expressionStmt);

				currentHole.setIsHole(false);
				holeNode = new HoleNode(HoleType.Statement, true);
				parentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {

				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				Optional<BlockStmt> optionalBody = mNode.getBody();
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Body);

				HoleNode anotherCurrentHole = new HoleNode();
				currentHole.addChild(anotherCurrentHole);

				BlockStmt blockStmt = optionalBody.get();
				NodeList<Statement> statements = blockStmt.getStatements();
				if (statements.size() == 0) {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);

					anotherCurrentHole.setIsHole(false);
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Expression, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}

			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			}
			break;
		case "let5":
			holeTypeExpr = HoleType.Let5Expr;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.setIsHole(false);
					holeNode = new HoleNode();
					parentHole.addChild(holeNode);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				BlockStmt blockStmt = (BlockStmt) parentAndIndex.getFirst();
				NodeList<Statement> statements = blockStmt.getStatements();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);

				statements.add(expressionStmt);

				currentHole.setIsHole(false);
				holeNode = new HoleNode(HoleType.Statement, true);
				parentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {

				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				Optional<BlockStmt> optionalBody = mNode.getBody();
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Body);

				HoleNode anotherCurrentHole = new HoleNode();
				currentHole.addChild(anotherCurrentHole);

				BlockStmt blockStmt = optionalBody.get();
				NodeList<Statement> statements = blockStmt.getStatements();
				if (statements.size() == 0) {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);

					anotherCurrentHole.setIsHole(false);
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Expression, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}

			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				if (holeIndex == 0) {
					ForStmt forStmt = (ForStmt) parent.getLeft();
					NodeList<Expression> initializationList = new NodeList<Expression>();
					initializationList.add((Expression) node);
					forStmt.setInitialization(initializationList);

					currentHole.setHoleType(HoleType.ForInitialization);
					currentHole.setIsHole(false);
					HoleNode childNode = new HoleNode();
					childNode.setHoleType(HoleType.Expression);
					childNode.setIsHole(true);
					currentHole.addChild(childNode);
				} else {
					this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			}
			break;
		case "let6":
			holeTypeExpr = HoleType.Let6Expr;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Wrapper);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
				holeNode = new HoleNode(HoleType.Expression, false);
				currentHole.addChild(holeNode);

				HoleNode holeNodeChild = new HoleNode();
				holeNode.addChild(holeNodeChild);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				// Note: we only support BlockStmt.
				// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
				// sum = sum + i in for(; i < 10 ;){ ;sum = sum + i; }
				ForStmt forStmt = (ForStmt) parent.getLeft();
				Statement body = forStmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					blockStmt.setStatements(statements);
					forStmt.setBody(blockStmt);

					currentHole.set(HoleType.Body, false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.set(HoleType.Statements, false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(HoleType.Let6Expr);
					anotherCurrentHole.addChild(childNode);

					HoleNode anotherChildNode = new HoleNode();
					anotherChildNode.setHoleType(HoleType.Expression);
					anotherChildNode.setIsHole(true);
					childNode.addChild(anotherChildNode);

				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				BlockStmt blockStmt = (BlockStmt) parent.getLeft();
				NodeList<Statement> statements = blockStmt.getStatements();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Statements);
				holeNode = new HoleNode(HoleType.Expression, true);
				currentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
				Optional<BlockStmt> optionalBody = mNode.getBody();
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Body);

				HoleNode anotherCurrentHole = new HoleNode();
				currentHole.addChild(anotherCurrentHole);

				BlockStmt blockStmt = optionalBody.get();
				NodeList<Statement> statements = blockStmt.getStatements();
				if (statements.size() == 0) {
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);

					anotherCurrentHole.setIsHole(false);
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Expression, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Let6Expr });
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, true);
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					holeNode.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmtForExpr10AndExpr11AndLet6(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntryForExpr10AndExpr11AndLet6(parent, node, holeIndex, currentHole, holeTypeExpr);
			}
			break;
		case "return1":
			HoleType holeType = HoleType.Return1;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Return1);
				holeNode = new HoleNode();
				parentOfParentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Return1, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode();
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateReturnStmtInForStmt(parent, node, currentHole, parentOfParentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateReturnStmtInWhiletmt(parent, node, currentHole, parentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateReturnStmtInIfThenStmt(parent, node, currentHole, parentHole, holeType);
			}
			break;
		case "return2":
			holeType = HoleType.Return2;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Return2);
				holeNode = new HoleNode();
				parentOfParentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Return2, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode();
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateReturnStmtInForStmt(parent, node, currentHole, parentOfParentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateReturnStmtInWhiletmt(parent, node, currentHole, parentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateReturnStmtInIfThenStmt(parent, node, currentHole, parentHole, holeType);
			}
			break;
		case "return3":
			holeType = HoleType.Return3;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					statements.add((Statement) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Return3);
					HoleNode childHoleNode = new HoleNode(HoleType.Undefined, true);
					parentHole.addChild(childHoleNode);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Return3, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode();
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				BlockStmt blockStmt = (BlockStmt) parentAndIndex.getFirst();
				NodeList<Statement> statements = blockStmt.getStatements();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Statement);
				HoleNode childHoleNode = new HoleNode(HoleType.Undefined, true);
				parentHole.addChild(childHoleNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateReturnStmtInForStmt(parent, node, currentHole, parentOfParentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateReturnStmtInWhiletmt(parent, node, currentHole, parentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateReturnStmtInIfThenStmt(parent, node, currentHole, parentHole, holeType);
			}
			break;
		case "return4":
			holeType = HoleType.Return4;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Return4);
				holeNode = new HoleNode();
				parentOfParentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Return4, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode();
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateReturnStmtInForStmt(parent, node, currentHole, parentOfParentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateReturnStmtInWhiletmt(parent, node, currentHole, parentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateReturnStmtInIfThenStmt(parent, node, currentHole, parentHole, holeType);
			}
			break;
		case "return5":
			holeType = HoleType.Return5;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Return5);
				holeNode = new HoleNode();
				parentOfParentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Return5, false);
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode();
					anotherCurrentHole.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateReturnStmtInForStmt(parent, node, currentHole, parentOfParentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateReturnStmtInWhiletmt(parent, node, currentHole, parentHole, holeType);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateReturnStmtInIfThenStmt(parent, node, currentHole, parentHole, holeType);
			}
			break;
		case "return6":
			holeType = HoleType.Return6;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				statements.add((Statement) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Wrapper);
				currentHole.setHoleTypeOptions(new HoleType[] { HoleType.Return6 });
				HoleNode holeNodeChild = new HoleNode();
				holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(holeNodeChild);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
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
					anotherCurrentHole.setHoleType(HoleType.Statements);

					holeNode = new HoleNode(HoleType.Wrapper, false);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Return6 });
					anotherCurrentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode();
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					holeNode.addChild(holeNodeChild);
				} else {
					// TODO
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				ForStmt forStmt = (ForStmt) parent.getLeft();
				Statement body = forStmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					statements.add((Statement) node);
					blockStmt.setStatements(statements);
					forStmt.setBody(blockStmt);

					currentHole.set(HoleType.Body, false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.set(HoleType.Statements, false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
					childNode.setHoleTypeOptionsOfOnlyOne(holeType);
					anotherCurrentHole.addChild(childNode);

					HoleNode anotherChildNode = new HoleNode();
					anotherChildNode.setHoleType(HoleType.Expression);
					anotherChildNode.setIsHole(true);
					childNode.addChild(anotherChildNode);

				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt stmt = (WhileStmt) parent.getLeft();
				Statement body = this.generateReturnStmt6(stmt.getBody(), node, currentHole, parentHole, holeType);
				stmt.setBody(body);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				IfStmt ifStmt = (IfStmt) parent.getLeft();
				Statement stmt = ifStmt.getThenStmt();

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.ThenStatement);

				String bodyClassStr = stmt.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					ifStmt.setThenStmt((Statement) node);

					HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false);
					anotherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeType);
					currentHole.addChild(anotherCurrentHole);

					HoleNode newHole = new HoleNode();
					newHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
					anotherCurrentHole.addChild(newHole);

				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			}
			break;
		case "expr1":
			holeTypeExpr = HoleType.Expr1;
			this.generateCallFunctionExpr(parent, node, currentHole, parentHole, parentOfParentHole, parentHoleType,
					holeIndex, parentNodeClassStr, holeTypeExpr);
			break;
		case "expr2":
			holeTypeExpr = HoleType.Expr2;
			this.generateCallFunctionExpr(parent, node, currentHole, parentHole, parentOfParentHole, parentHoleType,
					holeIndex, parentNodeClassStr, holeTypeExpr);
			break;
		case "expr3":
			holeTypeExpr = HoleType.Expr3;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
			}
			break;
		case "expr4":
			holeTypeExpr = HoleType.Expr4;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchStmt")) {
				SwitchStmt switchStmt = (SwitchStmt) parent.getLeft();
				switchStmt.setSelector((Expression) node);
				currentHole.setIsHole(false);
				holeNode = new HoleNode();
				parentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr5":
			holeTypeExpr = HoleType.Expr5;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.SwitchEntries)) {
				NodeList<SwitchEntry> switchEntries = (NodeList<SwitchEntry>) parent.get().get();
				if (holeIndex < switchEntries.size()) {
					// TODO
				} else {
					SwitchEntry switchEntry = new SwitchEntry();
					NodeList<Expression> labels = new NodeList<Expression>();
					labels.add((Expression) node);
					switchEntry.setLabels(labels);
					switchEntries.add(switchEntry);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Wrapper);
					currentHole.setHoleTypeOptions(new HoleType[] { HoleType.SwitchEntry });

					holeNode = new HoleNode(HoleType.Expression, false);
					holeNode.setHoleTypeOptionsOfOnlyOne(HoleType.Expr5);
					currentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Undefined, true);
					currentHole.addChild(holeNodeChild);

				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
				NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parent.getLeft()).getVariables();
				VariableDeclarator vNode = variableDeclarators.get(0);
				vNode.setInitializer((Expression) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.VariableDeclarator);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
				parentOfParentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarationExpr")) {
				VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) parent.getLeft();
				NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
				variableDeclarators.get(0).setInitializer((Expression) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Expression);

				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				parentOfParentHole.addChild(holeNode);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchStmt")) {
				SwitchStmt switchStmt = (SwitchStmt) parent.getLeft();
				NodeList<SwitchEntry> switchEntries = switchStmt.getEntries();
				if (switchEntries.size() == 0) {
					SwitchEntry switchEntry = new SwitchEntry();
					NodeList<Expression> labels = new NodeList<Expression>();
					labels.add((Expression) node);
					switchEntry.setLabels(labels);
					switchEntries.add(switchEntry);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.SwitchEntries);

					HoleNode wrapperNode = new HoleNode(HoleType.Wrapper, false);
					wrapperNode.setHoleTypeOptions(new HoleType[] { HoleType.SwitchEntry });
					currentHole.addChild(wrapperNode);

					holeNode = new HoleNode(HoleType.Expression, false);
					wrapperNode.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode(HoleType.Undefined, true);
					wrapperNode.addChild(holeNodeChild);
				} else {

				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ObjectCreationExpr")) {
				ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) parent.getLeft();
				NodeList<Expression> arguments = objectCreationExpr.getArguments();
				arguments.add((Expression) node);

				currentHole.set(HoleType.Arguments, false);
				holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptionsOfOnlyOne(HoleType.Argument);
				currentHole.addChild(holeNode);

				HoleNode childHoleNode = new HoleNode(holeTypeExpr, false);
				holeNode.addChild(childHoleNode);

				HoleNode newHole = new HoleNode();
				currentHole.addChild(newHole);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr6":
			holeTypeExpr = HoleType.Expr6;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmtForPlusPLus(parent, currentHole, node, holeTypeExpr, holeIndex, parentHole);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr7":
			holeTypeExpr = HoleType.Expr7;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmtForPlusPLus(parent, currentHole, node, holeTypeExpr, holeIndex, parentHole);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr8":
			holeTypeExpr = HoleType.Expr8;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmtForPlusPLus(parent, currentHole, node, holeTypeExpr, holeIndex, parentHole);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr9":
			holeTypeExpr = HoleType.Expr9;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmtForPlusPLus(parent, currentHole, node, holeTypeExpr, holeIndex, parentHole);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr10":
			holeTypeExpr = HoleType.Expr10;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					statements.add(new ExpressionStmt((Expression) node));

					currentHole.set(HoleType.Expression, false);

					HoleNode holdeNodeChild0 = new HoleNode(HoleType.Wrapper, false);
					holdeNodeChild0.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					currentHole.addChild(holdeNodeChild0);

					HoleNode holeNodeChild = new HoleNode();
					holeNodeChild.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
					holdeNodeChild0.addChild(holeNodeChild);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, HoleType.Expr10);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				BinaryExpr binaryExpr = (BinaryExpr) parent.getLeft();
				if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))
						|| parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11)) {
					binaryExpr.setRight((Expression) node);

					currentHole.set(HoleType.RightSubExpr, false);

					holeNode = new HoleNode(HoleType.Wrapper, false);
					holeNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					currentHole.addChild(holeNode);

					HoleNode holeNodeChild = new HoleNode();
					holeNode.addChild(holeNodeChild);
				} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))
						|| parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10)) {
					if (holeIndex == 0) {
						// left
						binaryExpr.setLeft((Expression) node);
						currentHole.set(HoleType.LeftSubExpr, false);
						HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false);
						anotherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
						currentHole.addChild(anotherCurrentHole);
						holeNode = new HoleNode();
						anotherCurrentHole.addChild(holeNode);
					} else {
						// right
						binaryExpr.setRight((Expression) node);
						currentHole.set(HoleType.RightSubExpr, false);
						HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false);
						anotherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
						currentHole.addChild(anotherCurrentHole);
						holeNode = new HoleNode();
						anotherCurrentHole.addChild(holeNode);
					}
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				// Note: we only support BlockStmt.
				// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
				// _ op _ in for(; i < 10 ;){ ;_ op _; }
				ForStmt forStmt = (ForStmt) parent.getLeft();
				Statement body = forStmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					blockStmt.setStatements(statements);
					forStmt.setBody(blockStmt);

					currentHole.setHoleType(HoleType.Body);
					currentHole.setIsHole(false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.setHoleType(HoleType.Statements);
					anotherCurrentHole.setIsHole(false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode();
					childNode.setHoleType(HoleType.Wrapper);
					childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
					childNode.setIsHole(false);
					anotherCurrentHole.addChild(childNode);

					HoleNode anotherChildNode = new HoleNode();
					anotherChildNode.setHoleType(HoleType.Expression);
					anotherChildNode.setIsHole(false);
					childNode.addChild(anotherChildNode);

					HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
					childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					anotherChildNode.addChild(childOfanotherChildNode);

					HoleNode newHole = new HoleNode();
					newHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
					childOfanotherChildNode.addChild(newHole);
				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt stmt = (WhileStmt) parent.getLeft();
				Statement body = stmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					blockStmt.setStatements(statements);
					stmt.setBody(blockStmt);

					currentHole.setHoleType(HoleType.Body);
					currentHole.setIsHole(false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.setHoleType(HoleType.Statements);
					anotherCurrentHole.setIsHole(false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode();
					childNode.setHoleType(HoleType.Wrapper);
					childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
					childNode.setIsHole(false);
					anotherCurrentHole.addChild(childNode);

					HoleNode anotherChildNode = new HoleNode();
					anotherChildNode.setHoleType(HoleType.Expression);
					anotherChildNode.setIsHole(false);
					childNode.addChild(anotherChildNode);

					HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
					childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					anotherChildNode.addChild(childOfanotherChildNode);

					HoleNode newHole = new HoleNode();
					newHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
					childOfanotherChildNode.addChild(newHole);
				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				IfStmt ifStmt = (IfStmt) parent.getLeft();
				if (parentHoleType.equals(HoleType.Wrapper) && parentHole.getChildList().size() == 1) {
					// parentHole.getChildList().size() == 1 means one child hole, it shall be
					// condition for the if.
					// If condition
					ifStmt.setCondition((Expression) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);
					currentHole.setHoleTypeOptions(new HoleType[] { HoleType.IfCondition });
					holeNode = new HoleNode(HoleType.Undefined, true);
					parentHole.addChild(holeNode);
				} else if (parentHole.getChildList().size() == 2) {
					// then branch
					this.generateThenStmtInIfStmtForExpr10AndExpr11AndLet6(parent, node, currentHole, holeTypeExpr);
				} else if (parentHole.getChildList().size() > 2) {
					// else branch
					IfStmt elseBranch = new IfStmt();
					elseBranch.setCondition((Expression) node);
					ifStmt.setElseStmt(elseBranch);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.ElseStatement);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
					currentHole.addChild(holeNodeChild);

					HoleNode holeNodeChild2 = new HoleNode(HoleType.Undefined, true);
					currentHole.addChild(holeNodeChild2);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntryForExpr10AndExpr11AndLet6(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6ForExpr10AndExpr11(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExprForExpr10AndExpr11(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr11":
			holeTypeExpr = HoleType.Expr11;
			if (parentHoleType.equals(HoleType.Statements)) {
				NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
				if (holeIndex < statements.size()) {
					// TODO
				} else {
					statements.add(new ExpressionStmt((Expression) node));

					currentHole.set(HoleType.Wrapper, false);
					currentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					HoleNode holdeNodeChild0 = new HoleNode(HoleType.Expression, false);
					currentHole.addChild(holdeNodeChild0);

					HoleNode holeNodeChild = new HoleNode();
					holeNodeChild.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
					holdeNodeChild0.addChild(holeNodeChild);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				BinaryExpr binaryExpr = (BinaryExpr) parent.getLeft();
				if (parentHole.getHoleType().equals(HoleType.LeftSubExpr)) {
					binaryExpr.setLeft((Expression) node);
					currentHole.set(holeTypeExpr, false);
					holeNode = new HoleNode();
					parentOfParentOfParentHole.addChild(holeNode);
				} else if (parentHole.getHoleType().equals(HoleType.RightSubExpr)) {
					binaryExpr.setRight((Expression) node);
					currentHole.set(HoleType.Expression, false);
					holeNode = new HoleNode();
					parentOfParentOfParentHole.addChild(holeNode);
				} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))
						|| parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11)) {
					binaryExpr.setRight((Expression) node);
					currentHole.set(HoleType.Wrapper, false);
					currentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);

					HoleNode holeNodeChild = new HoleNode();
					currentHole.addChild(holeNodeChild);
				} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))
						|| parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10)) {
					if (holeIndex == 0) {
						// left
						binaryExpr.setLeft((Expression) node);
						currentHole.set(HoleType.LeftSubExpr, false);
						HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false);
						anotherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
						currentHole.addChild(anotherCurrentHole);
						holeNode = new HoleNode();
						anotherCurrentHole.addChild(holeNode);
					} else {
						// right
						binaryExpr.setRight((Expression) node);
						currentHole.set(HoleType.RightSubExpr, false);
						HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false);
						anotherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
						currentHole.addChild(anotherCurrentHole);
						holeNode = new HoleNode();
						anotherCurrentHole.addChild(holeNode);
					}
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				if (holeIndex == 1) {
					// i < subexpression in for(; i < subexpression ;){}
					ForStmt forStmt = (ForStmt) parent.getLeft();
					forStmt.setCompare((Expression) node);

					currentHole.set(HoleType.ForCompare, false);

					HoleNode anOtherCurrentHole = new HoleNode(HoleType.Wrapper, false);
					anOtherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					currentHole.addChild(anOtherCurrentHole);

					HoleNode holeNodeChild = new HoleNode();
					holeNodeChild.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
					anOtherCurrentHole.addChild(holeNodeChild);
				} else {
					// Note: we only support BlockStmt.
					// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
					// _ op _ in for(; i < 10 ;){ ;_ op _; }
					ForStmt forStmt = (ForStmt) parent.getLeft();
					Statement body = forStmt.getBody();
					String bodyClassStr = body.getClass().toString();
					bodyClassStr = StringHelper.getClassName(bodyClassStr);
					if (bodyClassStr.equals("ReturnStmt")) {
						BlockStmt blockStmt = new BlockStmt();
						NodeList<Statement> statements = new NodeList<Statement>();
						ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
						statements.add(expressionStmt);
						blockStmt.setStatements(statements);
						forStmt.setBody(blockStmt);

						currentHole.setHoleType(HoleType.Body);
						currentHole.setIsHole(false);

						HoleNode anotherCurrentHole = new HoleNode();
						anotherCurrentHole.setHoleType(HoleType.Statements);
						anotherCurrentHole.setIsHole(false);
						currentHole.addChild(anotherCurrentHole);

						HoleNode childNode = new HoleNode();
						childNode.setHoleType(HoleType.Wrapper);
						childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
						childNode.setIsHole(false);
						anotherCurrentHole.addChild(childNode);

						HoleNode anotherChildNode = new HoleNode();
						anotherChildNode.setHoleType(HoleType.Expression);
						anotherChildNode.setIsHole(false);
						childNode.addChild(anotherChildNode);

						HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
						childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
						anotherChildNode.addChild(childOfanotherChildNode);

						HoleNode newHole = new HoleNode();
						newHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
						childOfanotherChildNode.addChild(newHole);
					} else if (bodyClassStr.equals("BlockStmt")) {

					} else {
						System.out.println("Should not go to this branch");
					}
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				WhileStmt stmt = (WhileStmt) parent.getLeft();
				Statement body = stmt.getBody();
				String bodyClassStr = body.getClass().toString();
				bodyClassStr = StringHelper.getClassName(bodyClassStr);
				if (bodyClassStr.equals("ReturnStmt")) {
					BlockStmt blockStmt = new BlockStmt();
					NodeList<Statement> statements = new NodeList<Statement>();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					blockStmt.setStatements(statements);
					stmt.setBody(blockStmt);

					currentHole.setHoleType(HoleType.Body);
					currentHole.setIsHole(false);

					HoleNode anotherCurrentHole = new HoleNode();
					anotherCurrentHole.setHoleType(HoleType.Statements);
					anotherCurrentHole.setIsHole(false);
					currentHole.addChild(anotherCurrentHole);

					HoleNode childNode = new HoleNode();
					childNode.setHoleType(HoleType.Wrapper);
					childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
					childNode.setIsHole(false);
					anotherCurrentHole.addChild(childNode);

					HoleNode anotherChildNode = new HoleNode();
					anotherChildNode.setHoleType(HoleType.Expression);
					anotherChildNode.setIsHole(false);
					childNode.addChild(anotherChildNode);

					HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
					childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					anotherChildNode.addChild(childOfanotherChildNode);

					HoleNode newHole = new HoleNode();
					newHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
					childOfanotherChildNode.addChild(newHole);
				} else if (bodyClassStr.equals("BlockStmt")) {

				} else {
					System.out.println("Should not go to this branch");
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				IfStmt ifStmt = (IfStmt) parent.getLeft();
				if (parentHoleType.equals(HoleType.Wrapper) && parentHole.getChildList().size() == 1) {
					// parentHole.getChildList().size() == 1 means one child hole, it shall be
					// condition for the if.
					// If condition
					ifStmt.setCondition((Expression) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);
					currentHole.setHoleTypeOptions(new HoleType[] { HoleType.IfCondition });
					holeNode = new HoleNode(HoleType.Undefined, true);
					parentHole.addChild(holeNode);
				} else if (parentHole.getChildList().size() == 2) {
					// then branch
					this.generateThenStmtInIfStmtForExpr10AndExpr11AndLet6(parent, node, currentHole, holeTypeExpr);
				} else if (parentHole.getChildList().size() > 2) {
					// else branch
					IfStmt elseBranch = new IfStmt();
					elseBranch.setCondition((Expression) node);
					ifStmt.setElseStmt(elseBranch);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.ElseStatement);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
					currentHole.addChild(holeNodeChild);

					HoleNode holeNodeChild2 = new HoleNode(HoleType.Undefined, true);
					currentHole.addChild(holeNodeChild2);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntryForExpr10AndExpr11AndLet6(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6ForExpr10AndExpr11(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExprForExpr10AndExpr11(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				ExpressionStmt expressionStmt = (ExpressionStmt) parent.getLeft();
				String expressionClassStr = StringHelper.getClassName(expressionStmt.getExpression().getClass().toString());
				if (expressionClassStr.equals("VariableDeclarationExpr")) {
					VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
					NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
					variableDeclarators.get(0).setInitializer((Expression) node);

					currentHole.set(HoleType.Expression, false);

					HoleNode variablesHole = new HoleNode(HoleType.VariableDeclarator, false);
					currentHole.addChild(variablesHole);

					HoleNode variableHole = new HoleNode(HoleType.VariableInitializer, false);
					variablesHole.addChild(variableHole);

					HoleNode exprHole = new HoleNode(HoleType.Wrapper, false);
					exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					variableHole.addChild(exprHole);

					holeNode = new HoleNode();
					exprHole.addChild(holeNode);

				} else if (expressionClassStr.equals("AssignExpr")) {
					AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
					assignExpr.setValue((Expression) node);

					currentHole.set(HoleType.Expression, false);
					HoleNode valueHole = new HoleNode(HoleType.AssignExprValue, false);
					currentHole.addChild(valueHole);

					HoleNode exprHole = new HoleNode(HoleType.Wrapper, false);
					exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					valueHole.addChild(exprHole);

					holeNode = new HoleNode();
					exprHole.addChild(holeNode);
				}
			}
			break;
		case "expr12":
			holeTypeExpr = HoleType.Expr12;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				if (holeIndex == 1) {
					// i < 10 in for(; i < 10 ;){}
					ForStmt forStmt = (ForStmt) parent.getLeft();
					forStmt.setCompare((Expression) node);

					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);

					holeNode = new HoleNode();
					holeNode.setIsHole(true);
					holeNode.setHoleType(HoleType.Undefined);

					parentHole.addChild(holeNode);
				} else {
					this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				if (holeIndex == 0) {
					// i < 10 in while(i < 10){}
					WhileStmt whileStmt = (WhileStmt) parent.getLeft();
					whileStmt.setCondition((Expression) node);
					currentHole.setIsHole(false);
					holeNode = new HoleNode(HoleType.Body, true);
					parentHole.addChild(holeNode);
				} else {
					this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				IfStmt ifStmt = (IfStmt) parent.getLeft();
				if (parentHoleType.equals(HoleType.Wrapper) && parentHole.getChildList().size() == 1) {
					// parentHole.getChildList().size() == 1 means one child hole, it shall be
					// condition for the if.
					// If condition
					ifStmt.setCondition((Expression) node);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.Expression);
					currentHole.setHoleTypeOptions(new HoleType[] { HoleType.IfCondition });
					holeNode = new HoleNode(HoleType.Undefined, true);
					parentHole.addChild(holeNode);
				} else if (parentHole.getChildList().size() == 2) {
					// then branch
					this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
				} else if (parentHole.getChildList().size() > 2) {
					// else branch
					IfStmt elseBranch = new IfStmt();
					elseBranch.setCondition((Expression) node);
					ifStmt.setElseStmt(elseBranch);
					currentHole.setIsHole(false);
					currentHole.setHoleType(HoleType.ElseStatement);

					HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
					currentHole.addChild(holeNodeChild);

					HoleNode holeNodeChild2 = new HoleNode(HoleType.Undefined, true);
					currentHole.addChild(holeNodeChild2);
				}
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				// body part, expression
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			}
			break;
		case "expr13":
			holeTypeExpr = HoleType.Expr13;
			if (parentHoleType.equals(HoleType.Statements)) {
				this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
				this.generateExpInMethodBody(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
				this.generateBinarExprInExpr(parent, holeIndex, node, currentHole, parentHole, parentOfParentHole,
						parentOfParentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
				this.generateExprStmtInForStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
				this.generateThenStmtInIfStmt(parent, node, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
				this.generateBlockStmt(parent, node, currentHole, holeIndex, parentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
				this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
				this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
				this.generateReturn6(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
				this.generateExprInAssignExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
			} else if (parentHoleType.equals(HoleType.Arguments)) {
				this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
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

	public Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer> getParentOfHole(List<Integer> path,
			HoleType holeTypeOfHole) {
		int index;
		Node parent = this.compilationUnit;
		HoleNode parentHole = this.holeAST.getRoot();
		for (index = 0; index < path.size() - 1; index++) {
			HoleNode temp = parentHole.getIthChild(path.get(index));
			parentHole = temp;

			HoleType holeType = parentHole.getHoleType();
			if (holeType.equals(HoleType.Wrapper)) {
				continue;
			}

			String name = TypeNameMap.map.get(holeType);

			Class parentClass = parent.getClass();
			Method method;
			try {
				int indexWithSameType = this.computeASTIndex(parentHole, holeTypeOfHole, path.get(index));
				method = parentClass.getMethod(name);
				try {
					NodeList nodeList = (NodeList) method.invoke(parent);
					if (indexWithSameType < nodeList.size()) {
						parent = nodeList.get(indexWithSameType);
					} else {
						Either<List<?>, NodeList<?>> either = Either.right(nodeList);
						return new Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer>(Either.right(either), path.get(index));
					}

				} catch (Exception e) {
					try {
						List<?> nodeList = (List<?>) method.invoke(parent);
						if (indexWithSameType < nodeList.size()) {
							parent = (Node) nodeList.get(indexWithSameType);
						} else {
							Either<List<?>, NodeList<?>> either = Either.left(nodeList);
							return new Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer>(Either.right(either),
									path.get(index));
						}
					} catch (Exception e2) {
						try {
							Optional<?> optionalData = (Optional<?>) method.invoke(parent);
							parent = (Node) optionalData.get();
						} catch (Exception e3) {
							parent = (Node) method.invoke(parent);
						}
					}
				}
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
		Either<Node, Either<List<?>, NodeList<?>>> either = Either.left(parent);
		return new Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer>(either, path.get(index));
	}

	public Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer> getParentOfHole(List<Integer> path) {
		int index;
		Node parent = this.compilationUnit;
		HoleNode parentHole = this.holeAST.getRoot();
		for (index = 0; index < path.size() - 1; index++) {
			HoleNode temp = parentHole.getIthChild(path.get(index));
			parentHole = temp;

			HoleType holeType = parentHole.getHoleType();
			if (holeType.equals(HoleType.Wrapper)) {
				continue;
			}

			int indexOfHole = path.get(index + 1);
			String name = TypeNameMap.map.get(holeType);
			Class parentClass = parent.getClass();
			Method method;
			try {
				method = parentClass.getMethod(name);
				try {

					NodeList nodeList = (NodeList) method.invoke(parent);
					if (indexOfHole < nodeList.size()) {
						parent = nodeList.get(indexOfHole);
					} else {
						Either<List<?>, NodeList<?>> either = Either.right(nodeList);
						return new Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer>(Either.right(either), indexOfHole);
					}

				} catch (Exception e) {
					try {
						List<?> nodeList = (List<?>) method.invoke(parent);
						if (indexOfHole < nodeList.size()) {
							parent = (Node) nodeList.get(indexOfHole);
						} else {
							Either<List<?>, NodeList<?>> either = Either.left(nodeList);
							return new Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer>(Either.right(either), indexOfHole);
						}
					} catch (Exception e2) {
						try {
							Optional<?> optionalData = (Optional<?>) method.invoke(parent);
							parent = (Node) optionalData.get();
						} catch (Exception e3) {
							parent = (Node) method.invoke(parent);
						}
					}
				}
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
		Either<Node, Either<List<?>, NodeList<?>>> either = Either.left(parent);
		return new Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer>(either, path.get(index));
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

	private int computeASTIndex(HoleNode parent, HoleType holeTypeOfHole, int currentIndex) {
		List<HoleNode> childList = parent.getChildList();
		int count = 0;
		for (int i = 0; i <= currentIndex; i++) {
			if (childList.get(i).getHoleType().equals(holeTypeOfHole)) {
				count++;
			}
		}

		return --count;
	}

	private HoleType computeHoleType(String classStr) {
		switch (classStr) {
		case "IntegerLiteralExpr":
			return HoleType.Expression;

		case "PackageDeclaration":
			return HoleType.PackageDeclaration;
		case "ImportDeclaration":
			return HoleType.ImportDeclaration;
		}
		return null;
	}

	private void generateExpInMethodBody(Either<Node, Either<List<?>, NodeList<?>>> parent, HoleNode currentHole,
			Node node, HoleType exprHoleType) {
		MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
		Optional<BlockStmt> optionalBody = mNode.getBody();
		currentHole.setIsHole(false);
		currentHole.setHoleType(HoleType.Body);

		HoleNode anotherCurrentHole = new HoleNode();
		currentHole.addChild(anotherCurrentHole);

		BlockStmt blockStmt = optionalBody.get();
		NodeList<Statement> statements = blockStmt.getStatements();
		if (statements.size() == 0) {
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add(expressionStmt);

			anotherCurrentHole.setIsHole(false);
			anotherCurrentHole.setHoleType(HoleType.Statements);

			HoleNode holeNode = new HoleNode(HoleType.Wrapper, false);
			holeNode.setHoleTypeOptions(new HoleType[] { exprHoleType });
			anotherCurrentHole.addChild(holeNode);

			HoleNode holdeNodeChild0 = new HoleNode(HoleType.Expression, false);
			holeNode.addChild(holdeNodeChild0);

			HoleNode holeNodeChild = new HoleNode();
			holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
			anotherCurrentHole.addChild(holeNodeChild);
		} else {
			// TODO
		}
	}

	private void generateExpInStatements(Either<Node, Either<List<?>, NodeList<?>>> parent, int holeIndex, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleType exprHoleType) {
		NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
		if (holeIndex < statements.size()) {
			// TODO
		} else {
			statements.add(new ExpressionStmt((Expression) node));

			currentHole.set(HoleType.Expression, false);
			HoleNode holdeNodeChild0 = new HoleNode(HoleType.Wrapper, false);
			holdeNodeChild0.setHoleTypeOptionsOfOnlyOne(exprHoleType);
			currentHole.addChild(holdeNodeChild0);

			HoleNode holeNodeChild = new HoleNode();
			parentHole.addChild(holeNodeChild);
		}
	}

	private void generateBinarExprInExpr(Either<Node, Either<List<?>, NodeList<?>>> parent, int holeIndex, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole, HoleNode parentOfParentOfParentHole,
			HoleType exprHoleType) {
		BinaryExpr binaryExpr = (BinaryExpr) parent.getLeft();
		if (parentHole.getHoleType().equals(HoleType.LeftSubExpr)) {
			binaryExpr.setLeft((Expression) node);
			currentHole.set(exprHoleType, false);
			HoleNode holeNode = new HoleNode();
			parentOfParentOfParentHole.addChild(holeNode);
		} else if (parentHole.getHoleType().equals(HoleType.RightSubExpr)) {
			binaryExpr.setRight((Expression) node);
			currentHole.set(HoleType.Expression, false);
			HoleNode holeNode = new HoleNode();
			parentOfParentOfParentHole.addChild(holeNode);
		} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
				&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))
				|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))) {
			binaryExpr.setRight((Expression) node);
			currentHole.set(exprHoleType, false);
			HoleNode holeNode = new HoleNode();
			parentOfParentOfParentHole.addChild(holeNode);
		} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
				&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))
				|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))) {
			if (holeIndex == 0) {
				// left
				binaryExpr.setLeft((Expression) node);

				currentHole.set(HoleType.LeftSubExpr, false);
				HoleNode anotherCurrentHole = new HoleNode(exprHoleType, false);
				currentHole.addChild(anotherCurrentHole);
				HoleNode holeNode = new HoleNode();
				parentHole.addChild(holeNode);
			} else {
				// right
				binaryExpr.setRight((Expression) node);

				currentHole.set(HoleType.RightSubExpr, false);
				HoleNode anotherCurrentHole = new HoleNode(exprHoleType, false);
				currentHole.addChild(anotherCurrentHole);

				HoleNode holeNode = new HoleNode();
				parentOfParentOfParentHole.addChild(holeNode);
			}
		}
	}

	private void generateExprStmtInForStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, HoleNode currentHole,
			Node node, HoleType exprHoleType) {
		// Note: we only support BlockStmt.
		// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
		// notify() in for(; i < 10 ;){ ;notify(); }
		ForStmt forStmt = (ForStmt) parent.getLeft();
		Statement body = this.generateExprStmtBodyForStmts(forStmt.getBody(), currentHole, node, exprHoleType);
		forStmt.setBody(body);
	}

	private void generateExprStmtInForStmtForPlusPLus(Either<Node, Either<List<?>, NodeList<?>>> parent,
			HoleNode currentHole, Node node, HoleType exprHoleType, int holeIndex, HoleNode parentHole) {
		if (holeIndex == 2) {
			ForStmt forStmt = (ForStmt) parent.getLeft();
			NodeList<Expression> expressions = new NodeList<Expression>();
			expressions.add((Expression) node);
			forStmt.setUpdate(expressions);

			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.Expression);
			HoleNode holeNode = new HoleNode(HoleType.Undefined, true);
			parentHole.addChild(holeNode);
		} else {
			// case i++ in for(){; i++; }
			this.generateExprStmtInForStmt(parent, currentHole, node, exprHoleType);
		}
	}

	private void generateExprStmtInWhileStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, HoleNode currentHole,
			Node node, HoleType exprHoleType) {
		WhileStmt stmt = (WhileStmt) parent.getLeft();
		Statement body = this.generateExprStmtBodyForStmts(stmt.getBody(), currentHole, node, exprHoleType);
		stmt.setBody(body);
	}

	private Statement generateExprStmtBodyForStmts(Statement body, HoleNode currentHole, Node node,
			HoleType exprHoleType) {
		// Note: we only support BlockStmt.
		// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
		String bodyClassStr = body.getClass().toString();
		bodyClassStr = StringHelper.getClassName(bodyClassStr);
		if (bodyClassStr.equals("ReturnStmt")) {
			BlockStmt blockStmt = new BlockStmt();
			NodeList<Statement> statements = new NodeList<Statement>();
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add(expressionStmt);
			blockStmt.setStatements(statements);

			currentHole.set(HoleType.Body, false);

			HoleNode anotherCurrentHole = new HoleNode();
			anotherCurrentHole.set(HoleType.Statements, false);
			currentHole.addChild(anotherCurrentHole);

			HoleNode childNode = new HoleNode();
			childNode.set(HoleType.Wrapper, false);
			childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
			anotherCurrentHole.addChild(childNode);

			HoleNode anotherChildNode = new HoleNode();
			anotherChildNode.set(HoleType.Expression, false);
			childNode.addChild(anotherChildNode);

			HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
			childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(exprHoleType);
			anotherChildNode.addChild(childOfanotherChildNode);

			HoleNode newHole = new HoleNode();
			anotherCurrentHole.addChild(newHole);
			return blockStmt;
		} else if (bodyClassStr.equals("BlockStmt")) {

		} else {
			System.out.println("Should not go to this branch");
		}

		return body;
	}

	private void generateThenStmtInIfStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleType holeTypeExpr) {
		IfStmt ifStmt = (IfStmt) parent.getLeft();
		Statement thenStmt = ifStmt.getThenStmt();
		String thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
		if (thenStmtStr.equals("ReturnStmt")) {
			BlockStmt blockStmt = new BlockStmt();
			NodeList<Statement> statements = new NodeList<Statement>();
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add(expressionStmt);
			blockStmt.setStatements(statements);
			ifStmt.setThenStmt(blockStmt);

			// [condition,then, else, else]
			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.ThenStatement);
			HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
			currentHole.addChild(holeNodeChild);

			HoleNode childOfHoleNodeChild = new HoleNode(holeTypeExpr, false);
			holeNodeChild.addChild(childOfHoleNodeChild);

			HoleNode holeNodeChildChild = new HoleNode(HoleType.Undefined, true);
			currentHole.addChild(holeNodeChildChild);
		} else if (thenStmtStr.equals("BlockStmt")) {
		}
	}

	private void generateBlockStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, HoleNode currentHole,
			int holeIndex, HoleNode parentHole, HoleType holeTypeExpr) {
		BlockStmt blockStmt = (BlockStmt) parent.getLeft();
		NodeList<Statement> statements = blockStmt.getStatements();
		if (holeIndex < statements.size()) {
			// TODO
		} else {
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add(expressionStmt);
			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.Expression);

			HoleNode exprHole = new HoleNode(holeTypeExpr, false);
			currentHole.addChild(exprHole);

			HoleNode holeNode = new HoleNode(HoleType.Statement, true);
			parentHole.addChild(holeNode);
		}
	}

	private void generateThenStmtInIfStmtForExpr10AndExpr11AndLet6(Either<Node, Either<List<?>, NodeList<?>>> parent,
			Node node, HoleNode currentHole, HoleType holeTypeExpr) {
		IfStmt ifStmt = (IfStmt) parent.getLeft();
		Statement thenStmt = ifStmt.getThenStmt();
		String thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
		if (thenStmtStr.equals("ReturnStmt")) {
			BlockStmt blockStmt = new BlockStmt();
			NodeList<Statement> statements = new NodeList<Statement>();
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add(expressionStmt);
			blockStmt.setStatements(statements);
			ifStmt.setThenStmt(blockStmt);

			// [condition,then, else, else]
			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.ThenStatement);

			HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsNode);

			HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
			stmtsNode.addChild(holeNodeChild);

			HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
			childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
			holeNodeChild.addChild(childOfanotherChildNode);

			HoleNode newHole = new HoleNode();
			newHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
			childOfanotherChildNode.addChild(newHole);

		} else if (thenStmtStr.equals("BlockStmt")) {
		}

	}

	private void generateSwitchEntry(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, int holeIndex,
			HoleNode currentHole, HoleType holeTypeExpr) {
		SwitchEntry switchEntry = (SwitchEntry) parent.getLeft();
		NodeList<Statement> statements = switchEntry.getStatements();
		if (holeIndex < statements.size()) {
			// TODO
		} else {
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add((Statement) expressionStmt);
			currentHole.setHoleType(HoleType.Statements);
			currentHole.setIsHole(false);

			HoleNode holeNode = new HoleNode(HoleType.Expression, false);
			currentHole.addChild(holeNode);

			HoleNode childHoleNode = new HoleNode(holeTypeExpr, false);
			holeNode.addChild(childHoleNode);

			HoleNode newHole = new HoleNode(HoleType.Undefined, true);
			currentHole.addChild(newHole);

		}
	}

	private void generateSwitchEntryForExpr10AndExpr11AndLet6(Either<Node, Either<List<?>, NodeList<?>>> parent,
			Node node, int holeIndex, HoleNode currentHole, HoleType holeTypeExpr) {
		SwitchEntry switchEntry = (SwitchEntry) parent.getLeft();
		NodeList<Statement> statements = switchEntry.getStatements();
		if (holeIndex < statements.size()) {
			// TODO
		} else {
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add((Statement) expressionStmt);
			currentHole.setHoleType(HoleType.Statements);
			currentHole.setIsHole(false);

			HoleNode holeNode = new HoleNode(HoleType.Expression, false);
			currentHole.addChild(holeNode);

			HoleNode childHoleNode = new HoleNode(HoleType.Wrapper, false);
			childHoleNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
			holeNode.addChild(childHoleNode);

			HoleNode newHole = new HoleNode(HoleType.Undefined, true);
			childHoleNode.addChild(newHole);
		}
	}

	private void generateExpForExpressionStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, int holeIndex,
			HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeTypeExpr) {
		ExpressionStmt expressionStmt = (ExpressionStmt) parent.getLeft();
		String expressionClassStr = StringHelper.getClassName(expressionStmt.getExpression().getClass().toString());
		if (expressionClassStr.equals("VariableDeclarationExpr")) {
			VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
			NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
			variableDeclarators.get(0).setInitializer((Expression) node);

			currentHole.set(holeTypeExpr, false);
			currentHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);

			HoleNode holeNode = new HoleNode(HoleType.Undefined, true);
			holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
			parentOfParentHole.addChild(holeNode);

		} else if (expressionClassStr.equals("AssignExpr")) {
			AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
			assignExpr.setValue((Expression) node);
			currentHole.set(holeTypeExpr, false);
			currentHole.setHoleTypeOptionsOfOnlyOne(HoleType.Expression);
			HoleNode holeNode = new HoleNode(HoleType.Undefined, true);
			parentOfParentHole.addChild(holeNode);
		}
	}

	private void generateReturnStmtInForStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleType holeType) {
		ForStmt forStmt = (ForStmt) parent.getLeft();
		Statement body = this.generateReturnStmt(forStmt.getBody(), node, currentHole, parentHole, holeType);
		forStmt.setBody(body);
	}

	private void generateReturnStmtInWhiletmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleType holeType) {
		WhileStmt stmt = (WhileStmt) parent.getLeft();
		Statement body = this.generateReturnStmt(stmt.getBody(), node, currentHole, parentHole, holeType);
		stmt.setBody(body);
	}

	private Statement generateReturnStmt(Statement body, Node node, HoleNode currentHole, HoleNode parentHole,
			HoleType holeType) {
		String bodyClassStr = body.getClass().toString();
		bodyClassStr = StringHelper.getClassName(bodyClassStr);
		if (bodyClassStr.equals("ReturnStmt")) {
			BlockStmt blockStmt = new BlockStmt();
			NodeList<Statement> statements = new NodeList<Statement>();
			statements.add((Statement) node);
			blockStmt.setStatements(statements);

			currentHole.set(HoleType.Body, false);

			HoleNode anotherCurrentHole = new HoleNode();
			anotherCurrentHole.set(HoleType.Statements, false);
			currentHole.addChild(anotherCurrentHole);

			HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
			childNode.setHoleTypeOptionsOfOnlyOne(holeType);
			anotherCurrentHole.addChild(childNode);

			HoleNode anotherChildNode = new HoleNode();
			anotherChildNode.setHoleType(HoleType.Expression);
			anotherChildNode.setIsHole(true);
			parentHole.addChild(anotherChildNode);
			return body;

		} else if (bodyClassStr.equals("BlockStmt")) {

		} else {
			System.out.println("Should not go to this branch");
		}
		return body;
	}

	private Statement generateReturnStmt6(Statement body, Node node, HoleNode currentHole, HoleNode parentHole,
			HoleType holeType) {
		String bodyClassStr = body.getClass().toString();
		bodyClassStr = StringHelper.getClassName(bodyClassStr);
		if (bodyClassStr.equals("ReturnStmt")) {
			BlockStmt blockStmt = new BlockStmt();
			NodeList<Statement> statements = new NodeList<Statement>();
			statements.add((Statement) node);
			blockStmt.setStatements(statements);

			currentHole.set(HoleType.Body, false);

			HoleNode anotherCurrentHole = new HoleNode();
			anotherCurrentHole.set(HoleType.Statements, false);
			currentHole.addChild(anotherCurrentHole);

			HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
			childNode.setHoleTypeOptionsOfOnlyOne(holeType);
			anotherCurrentHole.addChild(childNode);

			HoleNode anotherChildNode = new HoleNode(HoleType.Expression, false);
			childNode.addChild(anotherChildNode);

			HoleNode newHole = new HoleNode();
			anotherChildNode.addChild(newHole);
			return body;

		} else if (bodyClassStr.equals("BlockStmt")) {

		} else {
			System.out.println("Should not go to this branch");
		}
		return body;

	}

	private void generateReturnStmtInIfThenStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleType holeType) {
		IfStmt ifStmt = (IfStmt) parent.getLeft();
		Statement thenStmt = ifStmt.getThenStmt();
		String thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
		if (thenStmtStr.equals("ReturnStmt")) {
			BlockStmt blockStmt = new BlockStmt();
			NodeList<Statement> statements = new NodeList<Statement>();
			statements.add((Statement) node);
			blockStmt.setStatements(statements);
			ifStmt.setThenStmt(blockStmt);

			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.ThenStatement);

			HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsNode);

			HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
			stmtsNode.addChild(holeNodeChild);

			HoleNode childOfanotherChildNode = new HoleNode(holeType, false);
			holeNodeChild.addChild(childOfanotherChildNode);

			HoleNode newHole = new HoleNode();
			parentHole.addChild(newHole);
		} else if (thenStmtStr.equals("BlockStmt")) {
		}
	}

	private void generateReturn6(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, HoleNode currentHole,
			HoleNode parentHole, HoleNode parentOfParentHole, HoleType holeType) {
		ReturnStmt stmt = (ReturnStmt) parent.getLeft();
		stmt.setExpression((Expression) node);
		currentHole.set(holeType, false);
		HoleNode newHole = new HoleNode();
		parentOfParentHole.addChild(newHole);
	}

	private void generateReturn6ForExpr10AndExpr11(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole, HoleType holeType) {
		ReturnStmt stmt = (ReturnStmt) parent.getLeft();
		stmt.setExpression((Expression) node);
		currentHole.set(HoleType.Expression, false);
		HoleNode anotherHole = new HoleNode(HoleType.Wrapper, false);
		anotherHole.setHoleTypeOptionsOfOnlyOne(holeType);
		currentHole.addChild(anotherHole);
		HoleNode newHole = new HoleNode();
		anotherHole.addChild(newHole);
	}

	private void generateExprInAssignExpr(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeType) {
		AssignExpr assignExpr = (AssignExpr) parent.getLeft();
		assignExpr.setValue((Expression) node);
		currentHole.set(holeType, false);
		HoleNode holeNode = new HoleNode();
		parentOfParentHole.addChild(holeNode);
	}

	private void generateExprInAssignExprForExpr10AndExpr11(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeType) {
		AssignExpr assignExpr = (AssignExpr) parent.getLeft();
		assignExpr.setValue((Expression) node);

		currentHole.set(HoleType.AssignExprValue, false);
		HoleNode anotherHole = new HoleNode(HoleType.Wrapper, false);
		anotherHole.setHoleTypeOptionsOfOnlyOne(holeType);
		currentHole.addChild(anotherHole);
		HoleNode newHole = new HoleNode();
		anotherHole.addChild(newHole);
	}

	private void generateExprInArguments(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleType holeTypeExpr) {
		NodeList<Expression> arguments = (NodeList<Expression>) parent.get().get();
		arguments.add((Expression) node);

		currentHole.set(HoleType.Wrapper, false);
		currentHole.setHoleTypeOptionsOfOnlyOne(HoleType.Argument);
		HoleNode holeNode = new HoleNode(holeTypeExpr, false);
		currentHole.addChild(holeNode);
		parentHole.addChild(new HoleNode());
	}

	private void generateCallFunctionExpr(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole, HoleType parentHoleType, int holeIndex,
			String parentNodeClassStr, HoleType holeTypeExpr) {
		String nodeClassStr = StringHelper.getClassName(node.getClass().toString());
		if (parentHoleType.equals(HoleType.Statements)) {
			NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
			if (holeIndex < statements.size()) {
				// TODO
			} else {
				statements.add(new ExpressionStmt((Expression) node));

				currentHole.set(HoleType.Expression, false);
				HoleNode holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				currentHole.addChild(holeNode);

				HoleNode holdeNodeChild0 = new HoleNode(HoleType.Arguments, false);
				holeNode.addChild(holdeNodeChild0);

				HoleNode holeNodeChild = new HoleNode();
				holdeNodeChild0.addChild(holeNodeChild);
			}

		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
			MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
			Optional<BlockStmt> optionalBody = mNode.getBody();
			currentHole.setIsHole(false);
			currentHole.setHoleType(HoleType.Body);

			HoleNode anotherCurrentHole = new HoleNode();
			currentHole.addChild(anotherCurrentHole);

			BlockStmt blockStmt = optionalBody.get();
			NodeList<Statement> statements = blockStmt.getStatements();
			if (statements.size() == 0) {
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);

				anotherCurrentHole.setIsHole(false);
				anotherCurrentHole.setHoleType(HoleType.Statements);

				HoleNode holdeNodeChild0 = new HoleNode(HoleType.Expression, false);
				anotherCurrentHole.addChild(holdeNodeChild0);

				HoleNode holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptions(new HoleType[] { holeTypeExpr });
				holdeNodeChild0.addChild(holeNode);

				HoleNode holeNodeChild = new HoleNode(HoleType.Arguments, false);
				holeNode.addChild(holeNodeChild);

				HoleNode newHole = new HoleNode();
				holeNodeChild.addChild(newHole);
			} else {
				// TODO
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
			BinaryExpr binaryExpr = (BinaryExpr) parent.getLeft();
			if (parentHole.getHoleType().equals(HoleType.LeftSubExpr)) {
				System.out.println("Shall not go this branch");
			} else if (parentHole.getHoleType().equals(HoleType.RightSubExpr)) {
				binaryExpr.setRight((Expression) node);
				currentHole.set(HoleType.Expression, false);
				HoleNode holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				currentHole.addChild(holeNode);
				HoleNode anotherHoleNode = new HoleNode(HoleType.Arguments, false);
				holeNode.addChild(anotherHoleNode);
				HoleNode newHole = new HoleNode();
				anotherHoleNode.addChild(newHole);
			} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
					&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))
					|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
							&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))) {
				binaryExpr.setRight((Expression) node);
				currentHole.set(HoleType.Expression, false);
				HoleNode holeNode = new HoleNode(HoleType.Wrapper, false);
				holeNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				currentHole.addChild(holeNode);
				HoleNode anotherHoleNode = new HoleNode(HoleType.Arguments, false);
				holeNode.addChild(anotherHoleNode);
				HoleNode newHole = new HoleNode();
				anotherHoleNode.addChild(newHole);
			} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
					&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))
					|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
							&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))) {
				if (holeIndex == 0) {
					// left
					System.out.println("Shall not go to this branch");
				} else {
					// right
					binaryExpr.setRight((Expression) node);
					currentHole.set(HoleType.Expression, false);
					HoleNode holeNode = new HoleNode(HoleType.Wrapper, false);
					holeNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					currentHole.addChild(holeNode);
					HoleNode anotherHoleNode = new HoleNode(HoleType.Arguments, false);
					holeNode.addChild(anotherHoleNode);
					HoleNode newHole = new HoleNode();
					anotherHoleNode.addChild(newHole);
				}
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
			ForStmt forStmt = (ForStmt) parent.getLeft();
			String bodyClassStr = forStmt.getBody().getClass().toString();
			bodyClassStr = StringHelper.getClassName(bodyClassStr);
			if (bodyClassStr.equals("ReturnStmt")) {
				BlockStmt blockStmt = new BlockStmt();
				NodeList<Statement> statements = new NodeList<Statement>();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);
				blockStmt.setStatements(statements);
				forStmt.setBody(blockStmt);

				currentHole.set(HoleType.Body, false);

				HoleNode anotherCurrentHole = new HoleNode();
				anotherCurrentHole.set(HoleType.Statements, false);
				currentHole.addChild(anotherCurrentHole);

				HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
				childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
				anotherCurrentHole.addChild(childNode);

				HoleNode anotherChildNode = new HoleNode(HoleType.Expression, false);
				childNode.addChild(anotherChildNode);

				HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
				childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				anotherChildNode.addChild(childOfanotherChildNode);

				HoleNode argsWrapper = new HoleNode(HoleType.Arguments, false);
				childOfanotherChildNode.addChild(argsWrapper);

				HoleNode newHole = new HoleNode();
				argsWrapper.addChild(newHole);
			} else {
				System.out.println("Should not go to this branch");
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
			WhileStmt stmt = (WhileStmt) parent.getLeft();
			String bodyClassStr = stmt.getBody().getClass().toString();
			bodyClassStr = StringHelper.getClassName(bodyClassStr);
			if (bodyClassStr.equals("ReturnStmt")) {
				BlockStmt blockStmt = new BlockStmt();
				NodeList<Statement> statements = new NodeList<Statement>();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);
				blockStmt.setStatements(statements);
				stmt.setBody(blockStmt);

				currentHole.set(HoleType.Body, false);

				HoleNode anotherCurrentHole = new HoleNode();
				anotherCurrentHole.set(HoleType.Statements, false);
				currentHole.addChild(anotherCurrentHole);

				HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
				childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
				anotherCurrentHole.addChild(childNode);

				HoleNode anotherChildNode = new HoleNode(HoleType.Expression, false);
				childNode.addChild(anotherChildNode);

				HoleNode childOfanotherChildNode = new HoleNode(HoleType.Wrapper, false);
				childOfanotherChildNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				anotherChildNode.addChild(childOfanotherChildNode);

				HoleNode argsWrapper = new HoleNode(HoleType.Arguments, false);
				childOfanotherChildNode.addChild(argsWrapper);

				HoleNode newHole = new HoleNode();
				argsWrapper.addChild(newHole);
			} else {
				System.out.println("Should not go to this branch");
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
			IfStmt ifStmt = (IfStmt) parent.getLeft();
			Statement thenStmt = ifStmt.getThenStmt();
			String thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
			if (thenStmtStr.equals("ReturnStmt")) {
				BlockStmt blockStmt = new BlockStmt();
				NodeList<Statement> statements = new NodeList<Statement>();
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);
				blockStmt.setStatements(statements);
				ifStmt.setThenStmt(blockStmt);

				// [condition,then, else, else]
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.ThenStatement);

				HoleNode statementsHole = new HoleNode(HoleType.Statements, false);
				currentHole.addChild(statementsHole);

				HoleNode statementHole = new HoleNode(HoleType.Wrapper, false);
				statementHole.setHoleTypeOptionsOfOnlyOne(HoleType.Statement);
				statementsHole.addChild(statementHole);

				HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
				statementHole.addChild(holeNodeChild);

				HoleNode childOfHoleNodeChild = new HoleNode(HoleType.Wrapper, false);
				childOfHoleNodeChild.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				holeNodeChild.addChild(childOfHoleNodeChild);

				HoleNode holeNodeChildChild = new HoleNode(HoleType.Arguments, false);
				childOfHoleNodeChild.addChild(holeNodeChildChild);

				HoleNode newHole = new HoleNode();
				holeNodeChildChild.addChild(newHole);
			} else {
				System.out.println("Should not go to this branch");
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
			BlockStmt blockStmt = (BlockStmt) parent.getLeft();
			NodeList<Statement> statements = blockStmt.getStatements();
			if (holeIndex < statements.size()) {
				// TODO
			} else {
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.Expression);

				HoleNode exprHole = new HoleNode(HoleType.Wrapper, false);
				exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				currentHole.addChild(exprHole);

				HoleNode holeNode = new HoleNode(HoleType.Arguments, false);
				parentHole.addChild(holeNode);

				HoleNode newHole = new HoleNode();
				holeNode.addChild(newHole);
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
			SwitchEntry switchEntry = (SwitchEntry) parent.getLeft();
			NodeList<Statement> statements = switchEntry.getStatements();
			if (holeIndex < statements.size()) {
				// TODO
			} else {
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add((Statement) expressionStmt);
				currentHole.setHoleType(HoleType.Statements);
				currentHole.setIsHole(false);

				HoleNode holeNode = new HoleNode(HoleType.Expression, false);
				currentHole.addChild(holeNode);

				HoleNode childHoleNode = new HoleNode(HoleType.Wrapper, false);
				childHoleNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				holeNode.addChild(childHoleNode);

				HoleNode argsNode = new HoleNode(HoleType.Arguments, false);
				childHoleNode.addChild(argsNode);

				HoleNode newHole = new HoleNode();
				argsNode.addChild(newHole);

			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
			ExpressionStmt expressionStmt = (ExpressionStmt) parent.getLeft();
			String expressionClassStr = StringHelper.getClassName(expressionStmt.getExpression().getClass().toString());
			if (expressionClassStr.equals("VariableDeclarationExpr")) {
				VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expressionStmt.getExpression();
				NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
				variableDeclarators.get(0).setInitializer((Expression) node);

				currentHole.set(HoleType.Expression, false);

				HoleNode variables = new HoleNode(HoleType.VariableDeclarator, false);
				currentHole.addChild(variables);

				HoleNode variable = new HoleNode(HoleType.Wrapper, false);
				variable.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				variables.addChild(variable);

				HoleNode args = new HoleNode(HoleType.Arguments, false);
				variable.addChild(args);

				HoleNode holeNode = new HoleNode();
				args.addChild(holeNode);

			} else if (expressionClassStr.equals("AssignExpr")) {
				AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
				assignExpr.setValue((Expression) node);
				currentHole.set(HoleType.Expression, false);

				HoleNode exprHole = new HoleNode(HoleType.Wrapper, false);
				exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
				currentHole.addChild(exprHole);

				HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
				exprHole.addChild(argsHole);

				HoleNode holeNode = new HoleNode();
				argsHole.addChild(holeNode);
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
			ReturnStmt stmt = (ReturnStmt) parent.getLeft();
			stmt.setExpression((Expression) node);
			currentHole.set(HoleType.Expression, false);

			HoleNode exprHole = new HoleNode(HoleType.Wrapper, false);
			exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
			currentHole.addChild(exprHole);

			HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
			exprHole.addChild(argsHole);

		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
			AssignExpr assignExpr = (AssignExpr) parent.getLeft();
			assignExpr.setValue((Expression) node);
			currentHole.set(HoleType.AssignExprValue, false);

			HoleNode exprHole = new HoleNode(HoleType.Wrapper, false);
			exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
			currentHole.addChild(exprHole);

			HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
			exprHole.addChild(argsHole);

		} else if (parentHoleType.equals(HoleType.Arguments)) {
			this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
		}
	}
}
