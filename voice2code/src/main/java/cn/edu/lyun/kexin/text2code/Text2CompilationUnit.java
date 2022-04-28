package cn.edu.lyun.kexin.text2code;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
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
	private boolean isDebug;

	public Text2CompilationUnit() {
		this.compilationUnit = new CompilationUnit();
		this.holeAST = new HoleAST();
		this.isDebug = false;
	}

	public HoleAST getHoleAST() {
		return this.holeAST;
	}

	public Node getCompilationUnit() {
		return this.compilationUnit;
	}

	public void generatePNGofHoleAST() {
		this.holeAST.generateDotAndPNGOfHoleAST();
	}

	public CompilationUnit generate(String text) {
		Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text).concatNames();
		if (pattern == null) {
			System.out.println("Match failed");
			return null;
		}
		if (this.isDebug) {
			System.out.println("[log] matched pattern name: " + pattern.getName());
		}

		Node node = ASTManager.generate(pattern);

		Pair<Pair<HoleNode, HoleNode>, List<Integer>> holePosition = this.holeAST.getCurrentHole();
		List<Integer> path = holePosition.getSecond();
		Pair<HoleNode, HoleNode> parentAndCurrentHole = holePosition.getFirst();
		HoleNode parentHole = parentAndCurrentHole.getFirst();
		HoleNode currentHole = parentAndCurrentHole.getSecond();
		HoleType parentHoleType = parentHole.getHoleType();
		HoleNode parentOfParentHole = parentHole.getParent();
		HoleNode parentOfParentOfParentHole = null;
		if (parentOfParentHole != null) {
			parentOfParentOfParentHole = parentOfParentHole.getParent();
		}

		Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer> parentAndIndex = this.getParentOfHole(path);
		Either<Node, Either<List<?>, NodeList<?>>> parent = parentAndIndex.getFirst();
		int holeIndex = parentAndIndex.getSecond();
		String parentNodeClassStr = null;
		if (parent.isLeft()) {
			parentNodeClassStr = StringHelper.getClassName(parent.getLeft().getClass().toString());
		}

		switch (pattern.getName()) {
			case "moveNext":
				HoleNode exprHole = new HoleNode(HoleType.Undefined, true);
				if (currentHole.getHoleTypeOfOptionsIfOnlyOne() != null) {
					HoleType holeType = currentHole.getHoleTypeOfOptionsIfOnlyOne();
					if (holeType.equals(HoleType.Parameters)) {
						currentHole.set(HoleType.Parameters, false);
						parentHole.addChild(exprHole);
					} else if (holeType.equals(HoleType.ForInitialization)) {
						currentHole.set(HoleType.ForInitialization, false);
						exprHole.setHoleTypeOptionsOfOnlyOne(HoleType.ForCompare);
						parentHole.addChild(exprHole);
					} else if (holeType.equals(HoleType.ForCompare)) {
						ForStmt forStmt = (ForStmt) parent.getLeft();
						forStmt.setCompare(new BooleanLiteralExpr(true));
						currentHole.set(HoleType.ForCompare, false);
						exprHole.setHoleTypeOptionsOfOnlyOne(HoleType.ForExpression);
						parentHole.addChild(exprHole);
					} else if (holeType.equals(HoleType.ForExpression)) {
						currentHole.set(HoleType.ForExpression, false);
						parentHole.addChild(exprHole);
					} else {
						// TODO: small step move. Not syntax-directed.
						parentHole.deleteHole(holeIndex);
						parentOfParentHole.addChild(exprHole);
					}
				} else if (parentHoleType.equals(HoleType.SwitchEntries)) {
					HoleNode elderBrother = parentHole.getIthChild(holeIndex - 1);
					// Not has default case yet, then add a default case
					if (elderBrother.getIthChild(0).getIthChild(0).getHoleType().equals(HoleType.Expression)) {
						// generate SwitchEntry Sketch
						NodeList<SwitchEntry> switchEntries = (NodeList<SwitchEntry>) parent.get().get();
						SwitchEntry switchEntry = new SwitchEntry();
						switchEntries.add(switchEntry);

						currentHole.set(HoleType.Wrapper, false, HoleType.SwitchEntry);
						HoleNode labelsHole = new HoleNode(HoleType.SwitchEntryLabels, false);
						currentHole.addChild(labelsHole);
						exprHole = new HoleNode(HoleType.Wrapper, false, HoleType.MoveNext);
						labelsHole.addChild(exprHole);
						currentHole.addChild(new HoleNode());
					} else {
						parentHole.deleteHole(holeIndex);
						parentOfParentHole.addChild(exprHole);
					}
				} else if (parentHoleType.equals(HoleType.ElseStatement) || (parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.IfStmt))) {
					// default else case
					BlockStmt blockStmt = new BlockStmt();
					IfStmt ifStmt = (IfStmt) parent.getLeft();
					ifStmt.setElseStmt(blockStmt);

					currentHole.set(HoleType.ElseStatement, false);
					HoleNode stmtsHole = new HoleNode();
					stmtsHole.set(HoleType.Statements, false);
					currentHole.addChild(stmtsHole);
					stmtsHole.addChild(new HoleNode());
				} else if (parentHole.getHoleTypeOfOptionsIfOnlyOne()!=null && parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.ClassDeclaration)){
          if(parentHole.getChildListSize()==1){
            currentHole.set(HoleType.TypeParameters, false);
            parentHole.addChild(new HoleNode());
          } else if(parentHole.getChildListSize()==2){
            currentHole.set(HoleType.ExtendedTypes, false);
            parentHole.addChild(new HoleNode());
          } else if (parentHole.getChildListSize()==3){
            currentHole.set(HoleType.ImplementedTypes, false);
            parentHole.addChild(new HoleNode());
          }
        }
        else {
					// TODO: small step move. Not syntax-directed.
					parentHole.deleteHole(holeIndex);
					parentOfParentHole.addChild(exprHole);
				}
				break;
      case "moveNextBody":
        if(parentNodeClassStr!=null && parentNodeClassStr.equals("ClassOrInterfaceDeclaration")){
          if(parentHole.getChildListSize()==1){
            currentHole.set(HoleType.TypeParameters, false);
            parentHole.addChild(new HoleNode(HoleType.ExtendedTypes, false));
            parentHole.addChild(new HoleNode(HoleType.ImplementedTypes, false));
          } else if(parentHole.getChildListSize()==2){
            currentHole.set(HoleType.ExtendedTypes, false);
            parentHole.addChild(new HoleNode(HoleType.ImplementedTypes, false));
          } else if(parentHole.getChildListSize()==3){
            currentHole.set(HoleType.ImplementedTypes, false);
            parentHole.addChild(new HoleNode(HoleType.ImplementedTypes, false));
          }
         parentHole.addChild(new HoleNode());
        }  
        break;
      case "package":
				CompilationUnit parentNode = (CompilationUnit) parent.getLeft();
				parentNode.setPackageDeclaration((PackageDeclaration) node);
				currentHole.set(HoleType.PackageDeclaration, false);
				exprHole = new HoleNode();
				exprHole.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration, HoleType.TypeDeclaration });
				parentHole.addChild(exprHole);
				break;
			case "import":
				parentNode = null;
				if (parent.isLeft()) {
					parentNode = (CompilationUnit) parent.getLeft();
					if (parentNode.getImports().size() == 0) {
						NodeList<ImportDeclaration> importNodeList = new NodeList<ImportDeclaration>();
						importNodeList.add((ImportDeclaration) node);
						parentNode.setImports(importNodeList);
						currentHole.set(HoleType.ImportDeclarations, false);
						currentHole.addChild(new HoleNode(HoleType.ImportDeclaration, false, HoleType.ImportDeclaration));
						currentHole.addChild(new HoleNode(HoleType.ImportDeclaration, true, HoleType.ImportDeclaration));
					}
				} else {
					NodeList<ImportDeclaration> importDeclarations = (NodeList<ImportDeclaration>) parent.get().get();
					importDeclarations.add((ImportDeclaration) node);
					currentHole.set(HoleType.ImportDeclaration, false);
					parentHole.addChild(new HoleNode(HoleType.ImportDeclaration, true, HoleType.ImportDeclaration));
				}
				break;
			case "interface":
				parentNode = (CompilationUnit) parent.getLeft();
				parentNode.addType((ClassOrInterfaceDeclaration) node);

				currentHole.set(HoleType.TypeDeclarations, false);
				exprHole = new HoleNode(HoleType.Wrapper, false, HoleType.InterfaceDeclaration);
				currentHole.addChild(exprHole);
				exprHole.addChild(new HoleNode(HoleType.BodyDeclaration));
				break;
			case "class": // Note: class and interface belongs to TypeDeclaration.
				parentNode = null;
				if (parent.isLeft()) {
					parentNode = (CompilationUnit) parent.getLeft();
					parentNode.addType((ClassOrInterfaceDeclaration) node);

					currentHole.set(HoleType.TypeDeclarations, false);
					exprHole = new HoleNode(HoleType.Wrapper, false, HoleType.ClassDeclaration);
					currentHole.addChild(exprHole);
					exprHole.addChild(new HoleNode());
				} else {
					NodeList<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = (NodeList<ClassOrInterfaceDeclaration>) parent
							.get().get();
					classOrInterfaceDeclarations.add((ClassOrInterfaceDeclaration) node);

					currentHole.set(HoleType.Wrapper, false, HoleType.ClassDeclaration);
					currentHole.addChild(new HoleNode(HoleType.BodyDeclaration));
				}
				break;
			case "constructor":
				break;
			case "method":
				if (parentNodeClassStr != null && parentNodeClassStr.equals("ClassOrInterfaceDeclaration")) {
					ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) parent.getLeft();
					// If Interface, no body
					if (classOrInterfaceDeclaration.isInterface()) {
						((MethodDeclaration) node).removeBody();
					}
					classOrInterfaceDeclaration.addMember((BodyDeclaration<?>) node);

					currentHole.set(HoleType.BodyDeclarations, false);
					exprHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodDeclaration);
					currentHole.addChild(exprHole);
					exprHole.addChild(new HoleNode(HoleType.Type));
				} else if (parentHoleType.equals(HoleType.BodyDeclarations)) {
					NodeList<BodyDeclaration<?>> bodyDeclarations = (NodeList<BodyDeclaration<?>>) parent.get().get();
					// Interface method, no body.
					BodyDeclaration<?> bodyDeclaration0 = bodyDeclarations.get(0);
					String firstBodyDeclarationType = StringHelper.getClassName(bodyDeclaration0.getClass().toString());
					if (firstBodyDeclarationType.equals("MethodDeclaration")
							&& ((MethodDeclaration) bodyDeclaration0).getBody().isEmpty()) {
						((MethodDeclaration) node).removeBody();
					}
					bodyDeclarations.add((BodyDeclaration<?>) node);

					currentHole.set(HoleType.Wrapper, false, HoleType.BodyDeclaration);
					currentHole.addChild(new HoleNode(HoleType.Type));
				}
				break;
			case "arrowFunction":
				break;
			case "field":
				if (parentNodeClassStr != null && parentNodeClassStr.equals("ClassOrInterfaceDeclaration")
						&& parentHoleType.equals(HoleType.Wrapper)) {
					// real field
					ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) parent.getLeft();
					classOrInterfaceDeclaration.addMember((BodyDeclaration<?>) node);

					currentHole.set(HoleType.BodyDeclarations, false);
					HoleNode fieldDeclarationHole = new HoleNode(HoleType.Wrapper, false, HoleType.FieldDeclaration);
					currentHole.addChild(fieldDeclarationHole);
          HoleNode variableDeclaratorsHole = new HoleNode(HoleType.VariableDeclarators, false);
          fieldDeclarationHole.addChild(variableDeclaratorsHole);
          HoleNode variableDeclaratorHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
          variableDeclaratorsHole.addChild(variableDeclaratorHole);
          variableDeclaratorHole.addChild(new HoleNode());
				} else if (parentHoleType.equals(HoleType.BodyDeclarations)) {
					NodeList<BodyDeclaration<?>> bodyDeclarations = (NodeList<BodyDeclaration<?>>) parent.get().get();
					bodyDeclarations.add((BodyDeclaration<?>) node);
          currentHole.set(HoleType.Wrapper, false, HoleType.FieldDeclaration);
          HoleNode variableDeclaratorsHole = new HoleNode(HoleType.VariableDeclarators,false);
					currentHole.addChild(variableDeclaratorsHole);
          HoleNode wrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
          variableDeclaratorsHole.addChild(wrapperHole);
          wrapperHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					// variable declaration inside body. i.e. VariableDeclarationExpr
					// Regenerate VariableDeclarationExpr.
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();

					currentHole.set(HoleType.Body, false);
					HoleNode anotherCurrentHole = new HoleNode();
					currentHole.addChild(anotherCurrentHole);

					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					if (statements.size() == 0) {
						statements.add((Statement) node);

						anotherCurrentHole.set(HoleType.Statements, false);
						HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
						anotherCurrentHole.addChild(exprWrapperHole);
            HoleNode exprHoleNode = new HoleNode(HoleType.Expression, false);
            exprWrapperHole.addChild(exprHoleNode);
            HoleNode variableDeclaratorsHole = new HoleNode(HoleType.VariableDeclarators, false);
						exprHoleNode.addChild(variableDeclaratorsHole);
            HoleNode variableDeclaratorHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
            variableDeclaratorsHole.addChild(variableDeclaratorHole);
            variableDeclaratorHole.addChild(new HoleNode());
					} else {
						System.out.println("Should not go to this branch");
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
					// variable declaration inside body. i.e. VariableDeclarationExpr
					// regenerate VariableDeclarationExpr.
					// TODO: later may according to index to insert to specific location.
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
					BlockStmt blockStmt = (BlockStmt) parent.getLeft();
					NodeList<Statement> statements = blockStmt.getStatements();
					statements.add((Statement) node);

					currentHole.set(HoleType.Statement, false);
					currentHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
					ForStmt forStmt = (ForStmt) parent.getLeft();
					ExpressionStmt expressionStmt = (ExpressionStmt) new FieldAST().generateVariableDeclarationExpr(pattern);
					NodeList<Expression> initializationList = new NodeList<Expression>();
					initializationList.add(expressionStmt.getExpression());
					forStmt.setInitialization(initializationList);

					currentHole.set(HoleType.ForInitialization, false);
          HoleNode variableDeclarationExprHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarationExpr);
					currentHole.addChild(variableDeclarationExprHole);
          HoleNode variablesHole = new HoleNode(HoleType.VariableDeclarators, false);
          variableDeclarationExprHole.addChild(variablesHole);
          HoleNode variableHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
          variablesHole.addChild(variableHole);
          variableHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
          IfStmt ifStmt = (IfStmt)parent.getLeft();
          Statement thenStmt = ifStmt.getThenStmt();
          String thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
          if (thenStmtStr.equals("ReturnStmt")) {
            BlockStmt blockStmt = new BlockStmt();
            NodeList<Statement> statements = new NodeList<Statement>();
            statements.add((Statement) node);
            blockStmt.setStatements(statements);
            ifStmt.setThenStmt(blockStmt);

            currentHole.set(HoleType.ThenStatement, false);
            HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
            currentHole.addChild(stmtsHole);
            HoleNode stmtWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
            stmtsHole.addChild(stmtWrapperHole);
            HoleNode exprHoleNode = new HoleNode(HoleType.Expression, false);
            stmtWrapperHole.addChild(exprHoleNode);
            HoleNode variableDeclaratorsHole = new HoleNode(HoleType.VariableDeclarators, false);
						exprHoleNode.addChild(variableDeclaratorsHole);
            HoleNode variableDeclaratorHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
            variableDeclaratorsHole.addChild(variableDeclaratorHole);
            variableDeclaratorHole.addChild(new HoleNode());
          }
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
          WhileStmt whileStmt = (WhileStmt)parent.getLeft();
          Statement body = whileStmt.getBody();
					String bodyClassStr = StringHelper.getClassName(body.getClass().toString());
					if (bodyClassStr.equals("ReturnStmt")) {
						BlockStmt blockStmt = new BlockStmt();
						NodeList<Statement> statements = new NodeList<Statement>();
						statements.add((Statement) node);
						blockStmt.setStatements(statements);
						whileStmt.setBody(blockStmt);
						currentHole.set(HoleType.Body, false);
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
						stmtsHole.addChild(stmtHole);
            HoleNode exprHoleNode = new HoleNode(HoleType.Expression, false);
            stmtHole.addChild(exprHoleNode);
            HoleNode variableDeclaratorsHole = new HoleNode(HoleType.VariableDeclarators, false);
						exprHoleNode.addChild(variableDeclaratorsHole);
            HoleNode variableDeclaratorHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
            variableDeclaratorsHole.addChild(variableDeclaratorHole);
            variableDeclaratorHole.addChild(new HoleNode());
					} else {
						System.out.println("Shall not go into this branch");
					} 
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
          SwitchEntry switchEntry = (SwitchEntry)parent.getLeft();
          NodeList<Statement> statements = switchEntry.getStatements();
          statements.add((Statement) node);

          currentHole.set(HoleType.Statements, false);
          HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
          currentHole.addChild(stmtHole);
          HoleNode exprHoleNode = new HoleNode(HoleType.Expression, false);
          stmtHole.addChild(exprHoleNode);
          HoleNode variableDeclaratorsHole = new HoleNode(HoleType.VariableDeclarators, false);
				  exprHoleNode.addChild(variableDeclaratorsHole);
          HoleNode variableDeclaratorHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
          variableDeclaratorsHole.addChild(variableDeclaratorHole);
          variableDeclaratorHole.addChild(new HoleNode());
				} else if (parentHoleType.equals(HoleType.Statements)) {
					node = new FieldAST().generateVariableDeclarationExpr(pattern);
					NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
					statements.add((Statement) node);

					currentHole.set(HoleType.Wrapper, false, HoleType.Statement);
          HoleNode exprHoleNode = new HoleNode(HoleType.Expression, false);
          currentHole.addChild(exprHoleNode);
            HoleNode variableDeclaratorsHole = new HoleNode(HoleType.VariableDeclarators, false);
						exprHoleNode.addChild(variableDeclaratorsHole);
            HoleNode variableDeclaratorHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
            variableDeclaratorsHole.addChild(variableDeclaratorHole);
            variableDeclaratorHole.addChild(new HoleNode());
				}
				break;
			case "type":
				if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
          if(parentHole.getChildListSize()==1){
  					mNode.setType((Type) node);
	  				currentHole.set(HoleType.Type, false);
		  			// arguments
			  		parentHole.addChild(new HoleNode(HoleType.Parameters));
          } else {
           	NodeList<Parameter> nodeList = new NodeList<Parameter>();
             Parameter parameter = new Parameter();
             parameter.setType((Type)(node));
             nodeList.add(parameter);
					   mNode.setParameters(nodeList);
					   currentHole.set(HoleType.Parameters, false);
             HoleNode parameterHole = new HoleNode(HoleType.Wrapper, false, HoleType.Parameter);
					   currentHole.addChild(parameterHole);
             parameterHole.addChild(new HoleNode(HoleType.Type, false));
             parameterHole.addChild(new HoleNode());
          }
			  } else if (parentHoleType.equals(HoleType.Parameters)) {
        		NodeList<Parameter> nodeList = (NodeList<Parameter>) parent.get().get();
            Parameter parameter = new Parameter();
            parameter.setType((Type)(node));
        		nodeList.add(parameter);
        		currentHole.set(HoleType.Wrapper, false, HoleType.Parameter);
            currentHole.addChild(new HoleNode(HoleType.Type, false));
            currentHole.addChild(new HoleNode());
        }
				else if(parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          VariableDeclarator variableDeclarator= (VariableDeclarator)parent.getLeft();
          variableDeclarator.setType((Type)node);
          // currentHole.set(HoleType.Wrapper, false, HoleType.VariableDeclarator);
          currentHole.set(HoleType.Type, false);
          parentHole.addChild(new HoleNode());
        } else if(parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarationExpr")){
        } else if(parentNodeClassStr != null && parentNodeClassStr.equals("ClassOrInterfaceDeclaration")){
          ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration)parent.getLeft();
          if(parentHole.getChildListSize()==1){ // TypeParameters
            NodeList<TypeParameter> typeParameters = classOrInterfaceDeclaration.getTypeParameters();
            ClassOrInterfaceType t = (ClassOrInterfaceType)node;
            TypeParameter typeParameter = new TypeParameter(t.getNameAsString());
            typeParameters.add(typeParameter);
            currentHole.set(HoleType.TypeParameters, false);
            HoleNode typeParameterHole = new HoleNode(HoleType.Type, false);
            currentHole.addChild(typeParameterHole);
            currentHole.addChild(new HoleNode());
          } else if (parentHole.getChildListSize()==2){ // extends part
            NodeList<ClassOrInterfaceType> extendedTypes = classOrInterfaceDeclaration.getExtendedTypes();
            ClassOrInterfaceType t = (ClassOrInterfaceType)node;
            extendedTypes.add(t);
            currentHole.set(HoleType.ExtendedTypes, false);
            HoleNode extendedTypeHole = new HoleNode(HoleType.Type, false);
            currentHole.addChild(extendedTypeHole);
            currentHole.addChild(new HoleNode());
          } else if (parentHole.getChildListSize()==3){ // implements part
            NodeList<ClassOrInterfaceType> implementedType = classOrInterfaceDeclaration.getImplementedTypes();
            ClassOrInterfaceType t = (ClassOrInterfaceType)node;
            implementedType.add(t);
            currentHole.set(HoleType.ImplementedTypes, false);
            HoleNode implementedTypeHole = new HoleNode(HoleType.Type, false);
            currentHole.addChild(implementedTypeHole);
            currentHole.addChild(new HoleNode());
          }

			  } else if (parentHoleType.equals(HoleType.TypeParameters)) {
          NodeList<TypeParameter> typeParameters =(NodeList<TypeParameter>)parent.get().get();
          ClassOrInterfaceType t = (ClassOrInterfaceType)node;
          TypeParameter typeParameter = new TypeParameter(t.getNameAsString());
          typeParameters.add(typeParameter);
          currentHole.set(HoleType.Type, false);
          parentHole.addChild(new HoleNode());
			  } else if (parentHoleType.equals(HoleType.ExtendedTypes)) {
          NodeList<ClassOrInterfaceType> extendedTypes = (NodeList<ClassOrInterfaceType>)parent.get().get();
          extendedTypes.add((ClassOrInterfaceType)node);
          currentHole.set(HoleType.Type, false);
          currentHole.addChild(new HoleNode());
        } else if(parentNodeClassStr != null && parentNodeClassStr.equals("InstanceOfExpr")){
          InstanceOfExpr instanceOfExpr = (InstanceOfExpr)parent.getLeft();
          instanceOfExpr.setType((ReferenceType)node);
          currentHole.set(HoleType.Type, false);
          parentOfParentHole.addChild(new HoleNode());
        }
        break;
			case "for":
				HoleType holeTypeFor = HoleType.ForStmt;
				if (parentHoleType.equals(HoleType.Statements)) {
					NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
					statements.add((Statement) node);
					currentHole.set(HoleType.Wrapper, false, HoleType.ForStmt);
					currentHole.addChild(new HoleNode(HoleType.ForInitialization));
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					currentHole.set(HoleType.Body, false);
					HoleNode stmtsHole = new HoleNode();
					currentHole.addChild(stmtsHole);
					if (statements.size() == 0) {
						statements.add((Statement) node);
						stmtsHole.set(HoleType.Statements, false);
						exprHole = new HoleNode(HoleType.Wrapper, false, HoleType.ForStmt);
						stmtsHole.addChild(exprHole);
						exprHole.addChild(new HoleNode(HoleType.ForInitialization));
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
					// TODO: not tested.
					BlockStmt blockStmt = (BlockStmt) parent.getLeft();
					NodeList<Statement> statements = blockStmt.getStatements();
					statements.add((Statement) node);
					currentHole.set(HoleType.Statement, false);
					HoleNode holeNodeChild = new HoleNode(HoleType.Undefined, true, HoleType.ForInitialization);
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
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode wrapperExprHole = new HoleNode(HoleType.Wrapper, false, holeTypeFor);
						stmtsHole.addChild(wrapperExprHole);
						wrapperExprHole.addChild(new HoleNode(HoleType.ForInitialization));
					} else {
						System.out.println("Shall not go into this branch");
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

						currentHole.set(HoleType.Body, false);
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeFor);
						stmtsHole.addChild(exprWrapperHole);
						exprWrapperHole.addChild(new HoleNode(HoleType.ForInitialization));
					} else {
						// TODO
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					IfStmt ifStmt = (IfStmt) parent.getLeft();
					if (parentHole.getChildList().size() == 2) {
						// then branch
						Statement thenStmt = ifStmt.getThenStmt();
						String thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
						if (thenStmtStr.equals("ReturnStmt")) {
							BlockStmt blockStmt = new BlockStmt();
							NodeList<Statement> statements = new NodeList<Statement>();
							statements.add((Statement) node);
							blockStmt.setStatements(statements);
							ifStmt.setThenStmt(blockStmt);

							currentHole.set(HoleType.ThenStatement, false);
							HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
							currentHole.addChild(stmtsHole);
							HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeFor);
							stmtsHole.addChild(exprWrapperHole);
							exprWrapperHole.addChild(new HoleNode(HoleType.ForInitialization));
						} else if (thenStmtStr.equals("BlockStmt")) {
							System.out.println("Shall not go this branch");
						}
					} else if (parentHole.getChildList().size() > 2) {
						// else branch
						IfStmt elseBranch = new IfStmt();
						ifStmt.setElseStmt(elseBranch);

						currentHole.set(HoleType.ElseStatement, false);
						HoleNode thenStmtHole = new HoleNode(HoleType.ThenStatement, false);
						currentHole.addChild(thenStmtHole);
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						thenStmtHole.addChild(stmtsHole);
						HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeFor);
						stmtsHole.addChild(exprWrapperHole);
						exprWrapperHole.addChild(new HoleNode(HoleType.ForInitialization));
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
						currentHole.set(HoleType.Wrapper, false, HoleType.WhileStmt);
						currentHole.addChild(new HoleNode(HoleType.Expression));
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					currentHole.set(HoleType.Body, false);
					HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
					currentHole.addChild(stmtsHole);
					if (statements.size() == 0) {
						statements.add((Statement) node);
						HoleNode whileStmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.WhileStmt);
						stmtsHole.addChild(whileStmtHole);
						whileStmtHole.addChild(new HoleNode());
					} else {
						System.out.println("Shall not go to this branch");
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
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode whileWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.WhileStmt);
						stmtsHole.addChild(whileWrapperHole);
						whileWrapperHole.addChild(new HoleNode());
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
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode whileWrapper = new HoleNode(HoleType.Wrapper, false, HoleType.WhileStmt);
						stmtsHole.addChild(whileWrapper);
						whileWrapper.addChild(new HoleNode(HoleType.Expression));
					} else {
						// TODO
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					IfStmt ifStmt = (IfStmt) parent.getLeft();
					Statement stmt = ifStmt.getThenStmt();

					currentHole.set(HoleType.ThenStatement, false);

					String bodyClassStr = stmt.getClass().toString();
					bodyClassStr = StringHelper.getClassName(bodyClassStr);
					if (bodyClassStr.equals("ReturnStmt")) {
						BlockStmt blockStmt = new BlockStmt();
						NodeList<Statement> statements = new NodeList<Statement>();
						statements.add((Statement) node);
						blockStmt.setStatements(statements);
						ifStmt.setThenStmt(blockStmt);

						currentHole.set(HoleType.ThenStatement, false);
						HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsNode);
						HoleNode whileWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.WhileStmt);
						stmtsNode.addChild(whileWrapperHole);
						whileWrapperHole.addChild(new HoleNode(HoleType.Expression));
					} else if (bodyClassStr.equals("BlockStmt")) {
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
						currentHole.set(HoleType.Wrapper, false, HoleType.IfStmt);
						currentHole.addChild(new HoleNode(HoleType.Expression));
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
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode ifStmtWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.IfStmt);
						stmtsHole.addChild(ifStmtWrapperHole);
						ifStmtWrapperHole.addChild(new HoleNode());
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
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode ifStmtWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.IfStmt);
						stmtsHole.addChild(ifStmtWrapperHole);
						ifStmtWrapperHole.addChild(new HoleNode());
					} else {
						// TODO
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					currentHole.set(HoleType.Body, false);
					HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
					currentHole.addChild(stmtsHole);
					if (statements.size() == 0) {
						statements.add((Statement) node);
						HoleNode ifStmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.IfStmt);
						stmtsHole.addChild(ifStmtHole);
						ifStmtHole.addChild(new HoleNode());
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					IfStmt ifStmt = (IfStmt) parent.getLeft();
					Statement stmt = ifStmt.getThenStmt();
					currentHole.set(HoleType.ThenStatement, false);
					String bodyClassStr = stmt.getClass().toString();
					bodyClassStr = StringHelper.getClassName(bodyClassStr);
					if (bodyClassStr.equals("ReturnStmt")) {
						ifStmt.setThenStmt((Statement) node);
						HoleNode ifStmtWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.IfStmt);
						currentHole.addChild(ifStmtWrapperHole);
						ifStmtWrapperHole.addChild(new HoleNode(HoleType.Expression));
					} else if (bodyClassStr.equals("BlockStmt")) {

					} else {
						System.out.println("Should not go to this branch");
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
						currentHole.set(HoleType.Wrapper, false, HoleType.SwitchStmt);
						currentHole.addChild(new HoleNode());
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
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode switchWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.SwitchStmt);
						stmtsHole.addChild(switchWrapperHole);
						switchWrapperHole.addChild(new HoleNode());
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
						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode switchWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.SwitchStmt);
						stmtsHole.addChild(switchWrapperHole);
						switchWrapperHole.addChild(new HoleNode());
					} else if (bodyClassStr.equals("BlockStmt")) {

					} else {
						System.out.println("Should not go to this branch");
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					currentHole.set(HoleType.Body, false);
					HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
					currentHole.addChild(stmtsHole);
					if (statements.size() == 0) {
						statements.add((Statement) node);
						HoleNode switchWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.SwitchStmt);
						stmtsHole.addChild(switchWrapperHole);
						switchWrapperHole.addChild(new HoleNode());
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					IfStmt ifStmt = (IfStmt) parent.getLeft();
					Statement stmt = ifStmt.getThenStmt();
					currentHole.set(HoleType.ThenStatement, false);
					String bodyClassStr = stmt.getClass().toString();
					bodyClassStr = StringHelper.getClassName(bodyClassStr);
					if (bodyClassStr.equals("ReturnStmt")) {
						ifStmt.setThenStmt((Statement) node);
						HoleNode switchWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.SwitchStmt);
						currentHole.addChild(switchWrapperHole);
						switchWrapperHole.addChild(new HoleNode());
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
				HoleType holeTypeExpr = HoleType.InnerExpr;
				if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parent.getLeft()).getVariables();
					VariableDeclarator vNode = variableDeclarators.get(0);
					vNode.setInitializer((Expression) node);

					currentHole.set(HoleType.VariableDeclarator, false);

					HoleNode variableHole = new HoleNode(HoleType.Wrapper, false);
					variableHole.setHoleTypeOptionsOfOnlyOne(HoleType.VariableDeclarator);
					currentHole.addChild(variableHole);

					HoleNode initializerHole = new HoleNode(HoleType.VariableInitializer, false);
					variableHole.addChild(initializerHole);

					HoleNode innerHole = new HoleNode(HoleType.Wrapper, false);
					innerHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					initializerHole.addChild(innerHole);
					innerHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
					this.generateBinarExprInExprForSubExpression(parent, holeIndex, node, currentHole, parentHole,
							parentOfParentHole, parentOfParentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
					this.generateExprInAssignExprForExpr10AndExpr11(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
					this.generateReturn6ForExpr10AndExpr11(parent, node, currentHole, parentHole, parentOfParentHole,
							holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					EnclosedExpr enclosedExpr = (EnclosedExpr) parent.getLeft();
					enclosedExpr.setInner((Expression) node);
					currentHole.set(HoleType.InnerExpr, false);
					HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					currentHole.addChild(exprWrapper);
					exprWrapper.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
					ExpressionStmt expressionStmt = (ExpressionStmt) parent.getLeft();
					VariableDeclarationExpr expression = (VariableDeclarationExpr) expressionStmt.getExpression();
					NodeList<VariableDeclarator> variableDeclarators = expression.getVariables();
					variableDeclarators.get(0).setInitializer((Expression) node);

					currentHole.set(HoleType.Expression, false);
					exprHole = new HoleNode(HoleType.VariableDeclarator, false);
					currentHole.addChild(exprHole);
					HoleNode variableDeclaratorHole = new HoleNode(HoleType.Wrapper, false, HoleType.VariableDeclarator);
					exprHole.addChild(variableDeclaratorHole);
					HoleNode variableInitializerHole = new HoleNode(HoleType.VariableInitializer, false);
					variableDeclaratorHole.addChild(variableInitializerHole);
					HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					variableInitializerHole.addChild(exprWrapper);
					exprWrapper.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")) {
          VariableDeclarator variableDeclarator = (VariableDeclarator)parent.getLeft();
          variableDeclarator.setInitializer((Expression)node);

          currentHole.set(HoleType.VariableInitializer, false);
					HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					currentHole.addChild(exprWrapper);
					exprWrapper.addChild(new HoleNode());
				}
				break;
			case "break":
				if (parentHoleType.equals(HoleType.Statements)) {
					NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
					if (holeIndex < statements.size()) {
						// TODO
					} else {
						statements.add((Statement) node);
						currentHole.set(HoleType.Break, false);
						parentOfParentHole.addChild(new HoleNode());
					}
				}
				break;
			case "continue":
				if (parentHoleType.equals(HoleType.Statements)) {
					NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
					if (holeIndex < statements.size()) {
						// TODO
					} else {
						statements.add((Statement) node);
						currentHole.set(HoleType.Continue, false);
						parentOfParentHole.addChild(new HoleNode());
					}
				}
				break;
			case "newInstance":
        holeTypeExpr=HoleType.NewInstance;
				if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          VariableDeclarator variableDeclarator = (VariableDeclarator)parent.getLeft();
          variableDeclarator.setInitializer((Expression)node);
          currentHole.set(HoleType.VariableInitializer, false);
          HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(exprWrapperHole);
          HoleNode argumentsHole = new HoleNode(HoleType.Arguments, false);
          exprWrapperHole.addChild(argumentsHole);
          argumentsHole.addChild(new HoleNode());
        } else if(parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")){
          AssignExpr assignExpr = (AssignExpr)parent.getLeft();
          assignExpr.setValue((Expression)node);
          currentHole.set(HoleType.AssignExprValue, false);
          HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(exprWrapperHole);
          HoleNode argumentsHole = new HoleNode(HoleType.Arguments, false);
          exprWrapperHole.addChild(argumentsHole);
          argumentsHole.addChild(new HoleNode());
        } else if(parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")){
          MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					currentHole.set(HoleType.Body, false);

					HoleNode stmtsHole = new HoleNode();
					currentHole.addChild(stmtsHole);

					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					if (statements.size() == 0) { 
            statements.add(new ExpressionStmt((Expression)node));
						stmtsHole.set(HoleType.Statements, false);
            HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
            stmtsHole.addChild(stmtHole);
            HoleNode expressionHole = new HoleNode(HoleType.Expression, false);
            stmtHole.addChild(expressionHole);
            HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
            expressionHole.addChild(exprWrapperHole);
            HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
            exprWrapperHole.addChild(argsHole);
            argsHole.addChild(new HoleNode());
          }
        } else if (parentHoleType.equals(HoleType.Statements)) {
          NodeList<Statement> statements = (NodeList<Statement>)parent.get().get();
          if (holeIndex < statements.size()) {
						// TODO
					} else {
						statements.add(new ExpressionStmt((Expression)node));
            currentHole.set(HoleType.Wrapper, false, HoleType.Statement);
            HoleNode expressionHole = new HoleNode(HoleType.Expression, false);
            currentHole.addChild(expressionHole);
            HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
            expressionHole.addChild(exprWrapperHole);
            HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
            exprWrapperHole.addChild(argsHole);
            argsHole.addChild(new HoleNode());
					} 
        }
        
        break;
			case "throw":
				holeTypeExpr = HoleType.Throw;
				if (parentHoleType.equals(HoleType.Statements)) {
					NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
					if (holeIndex < statements.size()) {
						// TODO
					} else {
						statements.add((Statement) node);
						currentHole.set(holeTypeExpr, false);
						parentHole.addChild(new HoleNode());
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					currentHole.set(HoleType.Body, false);

					HoleNode anotherCurrentHole = new HoleNode();
					currentHole.addChild(anotherCurrentHole);

					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					if (statements.size() == 0) {
						statements.add((Statement) node);

						anotherCurrentHole.set(HoleType.Statements, false);
						exprHole = new HoleNode(holeTypeExpr, false);
						anotherCurrentHole.addChild(exprHole);
						parentOfParentHole.addChild(new HoleNode());
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

						HoleNode anotherCurrentHole = new HoleNode(HoleType.Statements, false);
						anotherCurrentHole.setHoleTypeOptions(new HoleType[] { HoleType.BlockStmt });
						currentHole.addChild(anotherCurrentHole);

						HoleNode childNode = new HoleNode(HoleType.Statement, false);
						anotherCurrentHole.addChild(childNode);

						HoleNode exprNode = new HoleNode(HoleType.Throw, false);
						childNode.addChild(exprNode);
						anotherCurrentHole.addChild(new HoleNode());
					} else {
						// TODO
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
					ForStmt forStmt = (ForStmt) parent.getLeft();
					Statement body = this.generateTotalStmt(forStmt.getBody(), node, currentHole, parentHole, holeTypeExpr);
					forStmt.setBody(body);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					this.generateTotalStmtInIfThenStmt(parent, node, currentHole, parentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
					this.generateSwitchEntryForTotalReturnStmt(parent, node, holeIndex, currentHole, holeTypeExpr);
				}
				break;
			case "let":
				holeTypeExpr = HoleType.LetExpr;
				if (parentHoleType.equals(HoleType.Statements)) {
					NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.set(HoleType.Wrapper, false, HoleType.Statement);
					exprHole = new HoleNode(HoleType.Expression, false);
					currentHole.addChild(exprHole);
					HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					exprHole.addChild(exprWrapperHole);
					exprWrapperHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
					// Note: we only support BlockStmt.
				ForStmt forStmt = (ForStmt) parent.getLeft();
					Statement body = forStmt.getBody();
					String bodyClassStr = body.getClass().toString();
					bodyClassStr = StringHelper.getClassName(bodyClassStr);
          if (holeIndex == 0) {
						NodeList<Expression> initializationList = new NodeList<Expression>();
						initializationList.add((Expression) node);
						forStmt.setInitialization(initializationList);
						currentHole.set(HoleType.ForInitialization, false);
						currentHole.addChild(new HoleNode());
					} else {
					// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
					// sum = sum + i in for(; i < 10 ;){ ;sum = sum + i; }
            if (bodyClassStr.equals("ReturnStmt")) {
              BlockStmt blockStmt = new BlockStmt();
              NodeList<Statement> statements = new NodeList<Statement>();
              ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
              statements.add(expressionStmt);
              blockStmt.setStatements(statements);
              forStmt.setBody(blockStmt);
  
              currentHole.set(HoleType.Body, false);
  
              HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
              currentHole.addChild(stmtsHole);
  
              HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
              stmtsHole.addChild(stmtHole);
  
              HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
              stmtHole.addChild(exprWrapperHole);
  
              exprWrapperHole.addChild(new HoleNode());
            } else if (bodyClassStr.equals("BlockStmt")) {
  
            } else {
              System.out.println("Should not go to this branch");
            }
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BlockStmt")) {
					BlockStmt blockStmt = (BlockStmt) parent.getLeft();
					NodeList<Statement> statements = blockStmt.getStatements();
					ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
					statements.add(expressionStmt);
					currentHole.set(HoleType.Statements, false);
					currentHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();
					currentHole.set(HoleType.Body, false);
					HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
					currentHole.addChild(stmtsHole);
					if (statements.size() == 0) {
						ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
						statements.add(expressionStmt);
						HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
						stmtsHole.addChild(stmtHole);
						HoleNode exprNode = new HoleNode(HoleType.Expression, false);
						stmtHole.addChild(exprNode);
						HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
						exprNode.addChild(exprWrapper);
						exprWrapper.addChild(new HoleNode());
					} else {
						// TODO
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					this.generateThenStmtInIfStmtForExpr10AndExpr11AndLet6(parent, node, currentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
					this.generateSwitchEntryForExpr10AndExpr11AndLet6(parent, node, holeIndex, currentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
					WhileStmt stmt = (WhileStmt) parent.getLeft();
					if (holeIndex == 0) {
						System.out.println("Shall not go to this branch");
					} else {
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

							currentHole.set(HoleType.Body, false);
							HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
							currentHole.addChild(stmtsHole);
							HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
							stmtsHole.addChild(stmtHole);
							HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
							stmtHole.addChild(exprWrapperHole);
							exprHole = new HoleNode(HoleType.Expression, false);
							exprWrapperHole.addChild(exprHole);
							exprHole.addChild(new HoleNode());
						} else if (bodyClassStr.equals("BlockStmt")) {

						} else {
							System.out.println("Should not go to this branch");
						}
					}
				}
				else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldAccessExpr")){
          this.inefficientMove(text);
        }
        break;
			case "return":
				holeTypeExpr = HoleType.Return;
				if (parentHoleType.equals(HoleType.Statements)) {
					NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
					statements.add((Statement) node);
					currentHole.set(HoleType.Wrapper, false);
					currentHole.setHoleTypeOptions(new HoleType[] { holeTypeExpr });
					HoleNode holeNodeChild = new HoleNode();
					holeNodeChild.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
					currentHole.addChild(holeNodeChild);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
					MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
					Optional<BlockStmt> optionalBody = mNode.getBody();
					BlockStmt blockStmt = optionalBody.get();
					NodeList<Statement> statements = blockStmt.getStatements();

					currentHole.set(HoleType.Body, false);
					HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
					currentHole.addChild(stmtsHole);

					if (statements.size() == 0) {
						statements.add((Statement) node);

						exprHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
						stmtsHole.addChild(exprHole);
						exprHole.addChild(new HoleNode(HoleType.Expression));
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

						HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
						currentHole.addChild(stmtsHole);
						HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
						stmtsHole.addChild(stmtHole);
						HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
						stmtHole.addChild(exprWrapperHole);
						exprWrapperHole.addChild(new HoleNode());
					} else if (bodyClassStr.equals("BlockStmt")) {

					} else {
						System.out.println("Should not go to this branch");
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
					WhileStmt stmt = (WhileStmt) parent.getLeft();
					currentHole.set(HoleType.Body, false);
					HoleNode newCurrentHole = new HoleNode();
					currentHole.addChild(newCurrentHole);
					Statement body = this.generateReturnStmt6(stmt.getBody(), node, newCurrentHole, parentHole, holeTypeExpr);
					stmt.setBody(body);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
					IfStmt ifStmt = (IfStmt) parent.getLeft();
					Statement stmt = ifStmt.getThenStmt();

					currentHole.set(HoleType.ThenStatement, false);

					String bodyClassStr = stmt.getClass().toString();
					bodyClassStr = StringHelper.getClassName(bodyClassStr);
					if (bodyClassStr.equals("ReturnStmt")) {
            BlockStmt blockStmt = new BlockStmt();
            NodeList<Statement> statements = new NodeList<Statement>();
            statements.add((Statement) node);
            blockStmt.setStatements(statements);
            ifStmt.setThenStmt(blockStmt);
      
            currentHole.set(HoleType.ThenStatement, false);
            HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
            currentHole.addChild(stmtsNode);
            HoleNode holeNodeChild = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
            stmtsNode.addChild(holeNodeChild);
            holeNodeChild.addChild(new HoleNode());
					} else if (bodyClassStr.equals("BlockStmt")) {

					} else {
						System.out.println("Should not go to this branch");
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
					SwitchEntry switchEntry = (SwitchEntry) parent.getLeft();
					NodeList<Statement> statements = switchEntry.getStatements();
					if (holeIndex < statements.size()) {
						// TODO
					} else {
						statements.add((Statement) node);
						currentHole.set(HoleType.Statements, false);

						HoleNode childHoleNode = new HoleNode(HoleType.Wrapper, false);
						childHoleNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
						currentHole.addChild(childHoleNode);
						childHoleNode.addChild(new HoleNode());
					}
				}
				else if (parentNodeClassStr != null && parentNodeClassStr.equals("LambdaExpr")){
          LambdaExpr lambdaExpr = (LambdaExpr)parent.getLeft();
          Statement body = lambdaExpr.getBody();
    
          String bodyClassStr = body.getClass().toString();
          bodyClassStr = StringHelper.getClassName(bodyClassStr);
          if (bodyClassStr.equals("ReturnStmt")) {
            BlockStmt blockStmt = new BlockStmt();
            NodeList<Statement> statements = new NodeList<Statement>();
            statements.add((Statement)node);
            blockStmt.setStatements(statements);
            lambdaExpr.setBody(blockStmt);
    
            currentHole.set(HoleType.Body, false);
            HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
            currentHole.addChild(stmtsNode);
            HoleNode holeNodeChild = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
            stmtsNode.addChild(holeNodeChild);
            holeNodeChild.addChild(new HoleNode());
          } else if (bodyClassStr.equals("BlockStmt")) {
    
          } else {
            System.out.println("Should not go to this branch");
          } 
        }
        break;
      case "expr0":  
        holeTypeExpr = HoleType.Expr0;
        if(parentHole.getHoleTypeOfOptionsIfOnlyOne()!=null && parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.NameDotChain)){
          FieldAccessExpr fieldAccessExpr = (FieldAccessExpr)parent.getLeft();
          FieldAccessExpr newScope = new FieldAccessExpr(fieldAccessExpr.getScope(), fieldAccessExpr.getName().getIdentifier());
          fieldAccessExpr.setScope(newScope);
          NameExpr nameExpr = (NameExpr)node;
          fieldAccessExpr.setName(nameExpr.getName().getIdentifier());
          currentHole.set(holeTypeExpr, false);
          parentHole.addChild(new HoleNode());
        } else {
          System.out.println("dot Name only follows Name dot Name");
        }
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
					this.generateExprInAssignExprForExpr3(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentHoleType.equals(HoleType.Arguments)) {
					this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					this.generateExprForFieldDeclaration(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")){
          ConditionalExpr conditionalExpr = (ConditionalExpr)parent.getLeft();
          int childListSize = parentHole.getChildListSize();
          if(childListSize==1){
            conditionalExpr.setCondition((Expression)node);
            currentHole.set(HoleType.ConditionalExprCondition, false);
          } else if (childListSize==2){
            conditionalExpr.setThenExpr((Expression)node);
            currentHole.set(HoleType.ConditionalExprThen, false);
          } else {
            conditionalExpr.setElseExpr((Expression)node);
            currentHole.set(HoleType.ConditionalExprElse, false);
          }
          HoleNode dotChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.NameDotChain);
          currentHole.addChild(dotChainHole);
          dotChainHole.addChild(new HoleNode(holeTypeExpr, false));
          dotChainHole.addChild(new HoleNode());
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")){
          UnaryExpr unaryExpr = (UnaryExpr)parent.getLeft();
          unaryExpr.setExpression((Expression)node);
          currentHole.set(HoleType.Expression, false);
          HoleNode dotChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.NameDotChain);
          currentHole.addChild(dotChainHole);
          dotChainHole.addChild(new HoleNode(holeTypeExpr, false));
          dotChainHole.addChild(new HoleNode());
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
					currentHole.set(HoleType.SwitchSelector, false);
					currentHole.addChild(new HoleNode(holeTypeExpr, false));
					parentHole.addChild(new HoleNode());
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
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					this.generateExprForFieldDeclaration(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentHoleType.equals(HoleType.Parameters)){
          NodeList<Parameter> parameters = (NodeList<Parameter>)parent.get().get();
          Parameter parameter = new Parameter();
          NameExpr nameExpr = (NameExpr)node;
          parameter.setName(nameExpr.getName());
          parameters.add(parameter);
          currentHole.set(holeTypeExpr, false);
          parentHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("Parameter")){
          Parameter parameter = (Parameter)(parent.getLeft());
          NameExpr nameExpr = (NameExpr)node;
          parameter.setName(nameExpr.getName());
          currentHole.set(HoleType.Name, false);
          parentOfParentHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        }
        else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")) {
          this.generateConditionExprThenElseSimple(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")) {
          this.generateExprInUnaryExpr(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("LambdaExpr")) {
          LambdaExpr lambdaExpr = (LambdaExpr)parent.getLeft();

        }
				break;
			case "expr5":
				holeTypeExpr = HoleType.Expr5;
				this.generateExprForExpr5AndExpr14AndExpr15(parent, node, parentNodeClassStr, holeIndex, currentHole,
						parentHole,
						parentOfParentHole, parentOfParentOfParentHole, parentHoleType, holeTypeExpr);
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
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					this.generateExprForFieldDeclaration(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
      } else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
        this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
      } else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")) {
        this.generateConditionExprThenElseSimple(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
      } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")) {
        this.generateExprInUnaryExpr(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr); 
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
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					this.generateExprForFieldDeclaration(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
        this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
      } else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")) {
        this.generateConditionExprThenElseSimple(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
      } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")) {
          this.generateExprInUnaryExpr(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
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
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					this.generateExprForFieldDeclaration(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")) {
          this.generateConditionExprThenElseSimple(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")) {
          this.generateExprInUnaryExpr(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr); 
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
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					this.generateExprForFieldDeclaration(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")) {
          this.generateConditionExprThenElseSimple(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")) {
          this.generateExprInUnaryExpr(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr); 
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
							|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
									&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))) {
						binaryExpr.setRight((Expression) node);

						currentHole.set(HoleType.RightSubExpr, false);
						exprHole = new HoleNode(HoleType.Wrapper, false);
						exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
						currentHole.addChild(exprHole);
						exprHole.addChild(new HoleNode());
					} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
							&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))
							|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
									&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))) {
						if (holeIndex == 0) {
							// left
							binaryExpr.setLeft((Expression) node);
							currentHole.set(HoleType.LeftSubExpr, false);
							HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false);
							anotherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
							currentHole.addChild(anotherCurrentHole);
							anotherCurrentHole.addChild(new HoleNode());
						} else {
							// right
							binaryExpr.setRight((Expression) node);
							currentHole.set(HoleType.RightSubExpr, false);
							HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false);
							anotherCurrentHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
							currentHole.addChild(anotherCurrentHole);
							anotherCurrentHole.addChild(new HoleNode());
						}
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ForStmt")) {
					// Note: we only support BlockStmt.
					// https://www.javadoc.io/static/com.github.javaparser/javaparser-core/3.23.1/com/github/javaparser/ast/stmt/ForStmt.html
					// _ op _ in for(; i < 10 ;){ ;_ op _; }
					if (holeIndex == 1) {
						// i < subexpression in for(; i < subexpression ;){}
						ForStmt forStmt = (ForStmt) parent.getLeft();
						forStmt.setCompare((Expression) node);

						currentHole.set(HoleType.ForCompare, false);
						HoleNode wrapperHoleForExpr = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
						currentHole.addChild(wrapperHoleForExpr);
						wrapperHoleForExpr.addChild(new HoleNode(HoleType.Expression));
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

							currentHole.set(HoleType.Body, false);
							HoleNode anotherCurrentHole = new HoleNode(HoleType.Statements, false);
							currentHole.addChild(anotherCurrentHole);

							HoleNode childNode = new HoleNode(HoleType.Wrapper, false);
							childNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
							anotherCurrentHole.addChild(childNode);

							HoleNode anotherChildNode = new HoleNode(HoleType.Expression, false);
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
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("IfStmt")) {
          this.generateCondExprInIfForExpr10AndExpr16(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr, parentHoleType);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("WhileStmt")) {
					WhileStmt stmt = (WhileStmt) parent.getLeft();
					if (holeIndex == 0) {
						// counter < expression in while(counter < 10){}
						WhileStmt whileStmt = (WhileStmt) parent.getLeft();
						whileStmt.setCondition((Expression) node);
						currentHole.set(HoleType.WhileCondition, false);
						HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
						currentHole.addChild(exprWrapperHole);
						exprWrapperHole.addChild(new HoleNode(HoleType.Expression));
					} else {
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

							currentHole.set(HoleType.Body, false);
							HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
							currentHole.addChild(stmtsHole);
							HoleNode stmtHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
							stmtsHole.addChild(stmtHole);
							exprHole = new HoleNode(HoleType.Expression, false);
							stmtHole.addChild(exprHole);
							HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
							exprHole.addChild(exprWrapperHole);
							exprWrapperHole.addChild(new HoleNode(HoleType.Expression));
						} else if (bodyClassStr.equals("BlockStmt")) {

						} else {
							System.out.println("Should not go to this branch");
						}
					}
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
					this.generateSwitchEntryForExpr10AndExpr11AndLet6(parent, node, holeIndex, currentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
					this.generateReturn6ForExpr10AndExpr11(parent, node, currentHole, parentHole, parentOfParentHole,
							holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
					this.generateExprInAssignExprForExpr10AndExpr11(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					EnclosedExpr enclosedExpr = (EnclosedExpr) parent.getLeft();
					enclosedExpr.setInner((Expression) node);
					currentHole.set(HoleType.InnerExpr, false);

					exprHole = new HoleNode(HoleType.Wrapper, false);
					exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
					currentHole.addChild(exprHole);
					exprHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
					NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parent.getLeft()).getVariables();
					VariableDeclarator vNode = variableDeclarators.get(0);
					vNode.setInitializer((Expression) node);

					currentHole.set(HoleType.VariableDeclarator, false);
					HoleNode variableHole = new HoleNode(HoleType.VariableInitializer, false);
					currentHole.addChild(variableHole);

					exprHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					variableHole.addChild(exprHole);
					exprHole.addChild(new HoleNode());
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
					this.generateExpreStmtForExpr10AndExpr11(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
				} else if (parentHoleType.equals(HoleType.Arguments)) {
					NodeList<Expression> arguments = (NodeList<Expression>) parent.get().get();
					arguments.add((Expression) node);

					currentHole.set(HoleType.Wrapper, false, HoleType.Argument);
					exprHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					currentHole.addChild(exprHole);
					exprHole.addChild(new HoleNode());
				}
				else if(parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          VariableDeclarator variableDeclarator = (VariableDeclarator)parent.getLeft();
          variableDeclarator.setInitializer((Expression)node);
          currentHole.set(HoleType.VariableInitializer, false);
          HoleNode wrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(wrapperHole);
          wrapperHole.addChild(new HoleNode());
        }
        else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")){
          ConditionalExpr conditionalExpr = (ConditionalExpr)parent.getLeft();
          // condition part
          if(parentHole.getChildListSize()==1){
            conditionalExpr.setCondition((Expression)node);
            currentHole.set(HoleType.ConditionalExprCondition, false);
            HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
            currentHole.addChild(exprWrapperHole);
            exprWrapperHole.addChild(new HoleNode());
          } else if (parentHole.getChildListSize()==2){
            // then part
            conditionalExpr.setThenExpr((Expression)node);
            currentHole.set(HoleType.ConditionalExprThen, false);
            HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
            currentHole.addChild(exprWrapperHole);
            exprWrapperHole.addChild(new HoleNode());
          } else {
            // else part
            conditionalExpr.setElseExpr((Expression)node);
            currentHole.set(HoleType.ConditionalExprElse, false);
            HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
            currentHole.addChild(exprWrapperHole);
            exprWrapperHole.addChild(new HoleNode());
          }
        }
        break;
			case "expr13":
				holeTypeExpr = HoleType.Expr13;
				if (parentHoleType.equals(HoleType.Statements)) {
					this.generateExpInStatements(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr);
				} else if (parentHoleType.equals(HoleType.Arguments)) {
					this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
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
				} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
					this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
          this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")) {
          this.generateConditionExprThenElseSimple(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
        } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")) {
          this.generateExprInUnaryExpr(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr); 
        }
				break;
			case "expr14":
				holeTypeExpr = HoleType.Expr14;
				this.generateExprForExpr5AndExpr14AndExpr15(parent, node, parentNodeClassStr, holeIndex, currentHole,
						parentHole,
						parentOfParentHole, parentOfParentOfParentHole, parentHoleType, holeTypeExpr);
				break;
			case "expr15":
				holeTypeExpr = HoleType.Expr15;
        if(parentNodeClassStr!=null && parentNodeClassStr.equals("AssignExpr")){
          AssignExpr assignExpr = (AssignExpr)parent.getLeft();
          assignExpr.setValue((Expression)node);
          currentHole.set(HoleType.AssignExprValue, false);
          HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(exprWrapperHole);
          exprWrapperHole.addChild(new HoleNode());
        } 
				break;

      case "expr16":
        holeTypeExpr = HoleType.Expr16;
        if(parentNodeClassStr!=null && parentNodeClassStr.equals("AssignExpr")){
          AssignExpr assignExpr = (AssignExpr)parent.getLeft();
          assignExpr.setValue((Expression)node);
          currentHole.set(HoleType.AssignExprValue, false);
          HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(exprWrapperHole);
          exprWrapperHole.addChild(new HoleNode());
        } else if(parentNodeClassStr!=null && parentNodeClassStr.equals("ConditionalExpr")){
          ConditionalExpr conditionalExpr = (ConditionalExpr)parent.getLeft();
          conditionalExpr.setCondition((Expression)node);
          currentHole.set(HoleType.ConditionalExprCondition, false);
          HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(exprWrapperHole);
          exprWrapperHole.addChild(new HoleNode());
        } else if(parentNodeClassStr!=null && parentNodeClassStr.equals("IfStmt")){
          this.generateCondExprInIfForExpr10AndExpr16(parent, holeIndex, node, currentHole, parentHole, holeTypeExpr, parentHoleType);
        } else if(parentNodeClassStr!=null && parentNodeClassStr.equals("ForStmt")){
          if (holeIndex == 1) {
						// !expression in for(; !expression ;){}
						ForStmt forStmt = (ForStmt) parent.getLeft();
						forStmt.setCompare((Expression) node);

						currentHole.set(HoleType.ForCompare, false);
						HoleNode wrapperHoleForExpr = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
						currentHole.addChild(wrapperHoleForExpr);
						wrapperHoleForExpr.addChild(new HoleNode(HoleType.Expression));
					}
        } else if(parentHoleType.equals(HoleType.Arguments)){
          NodeList<Expression> arguments = (NodeList<Expression>) parent.get().get();
          arguments.add((Expression) node);
          currentHole.set(HoleType.Wrapper, false, HoleType.Argument);
          HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(exprWrapperHole);
          exprWrapperHole.addChild(new HoleNode());
        } else if(parentNodeClassStr!=null && parentNodeClassStr.equals("BinaryExpr")){
          BinaryExpr binaryExpr = (BinaryExpr) parent.getLeft();
         if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
              && parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))) {
            if (holeIndex == 0) {
              // left
              binaryExpr.setLeft((Expression) node);
              currentHole.set(HoleType.LeftSubExpr, false);
              HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
              currentHole.addChild(exprWrapper);
              exprWrapper.addChild(new HoleNode());
            } else {
              // right
              binaryExpr.setRight((Expression) node);
              currentHole.set(HoleType.RightSubExpr, false);
              HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
              currentHole.addChild(exprWrapper);
              exprWrapper.addChild(new HoleNode());
            }
          }
        }
        break;
      case "expr17":
        holeTypeExpr = HoleType.Expr17;
        if(parentHoleType.equals(HoleType.Arguments)){
          NodeList<Expression> arguments = (NodeList<Expression>) parent.get().get();
          arguments.add((Expression) node);
          currentHole.set(HoleType.Wrapper, false, HoleType.Argument);
          HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(exprWrapperHole);
          HoleNode parasHole = new HoleNode(HoleType.Parameters, false);
          exprWrapperHole.addChild(parasHole);
          parasHole.addChild(new HoleNode());
        }
      case "expr18":
        holeTypeExpr = HoleType.Expr18;
        if(parentNodeClassStr !=null && parentNodeClassStr.equals("IfStmt")){
          IfStmt ifStmt = (IfStmt)parent.getLeft();
          ifStmt.setCondition((Expression)node);
          currentHole.set(HoleType.IfCondition, false);
          HoleNode expr18Wrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(expr18Wrapper);
          expr18Wrapper.addChild(new HoleNode());
        } else if(parentNodeClassStr !=null && parentNodeClassStr.equals("AssignExpr")){
          AssignExpr assignExpr = (AssignExpr)parent.getLeft();
          assignExpr.setValue((Expression)node);
          currentHole.set(HoleType.AssignExprValue, false);
          HoleNode expr18Wrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(expr18Wrapper);
          expr18Wrapper.addChild(new HoleNode());
        } else if(parentNodeClassStr !=null && parentNodeClassStr.equals("VariableDeclarator")){
          VariableDeclarator variableDeclarator = (VariableDeclarator)parent.getLeft();
          variableDeclarator.setInitializer((Expression)node);
          currentHole.set(HoleType.VariableInitializer, false);
          HoleNode expr18Wrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
          currentHole.addChild(expr18Wrapper);
          expr18Wrapper.addChild(new HoleNode());
        }
        break;
      }

		// this.holeAST.generateDotAndPNGOfHoleAST();
		this.holeAST.cleverMove();

		if (this.isDebug) {
			System.out.println("[log] write to file");
		}
		this.lexicalPreseveToJavaFile();
		return this.compilationUnit;
	}

  private void generateCondExprInIfForExpr10AndExpr16(Either<Node, Either<List<?>, NodeList<?>>> parent, int holeIndex, Node node,
  HoleNode currentHole, HoleNode parentHole, HoleType holeTypeExpr, HoleType parentHoleType){
    IfStmt ifStmt = (IfStmt) parent.getLeft();
    if (parentHoleType.equals(HoleType.Wrapper) && parentHole.getChildList().size() == 1) {
      // parentHole.getChildList().size() == 1 means one child hole, it shall be
      // condition for the if.
      // If condition
      ifStmt.setCondition((Expression) node);
      currentHole.set(HoleType.IfCondition, false);
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      currentHole.addChild(exprWrapperHole);
      exprWrapperHole.addChild(new HoleNode());
    } else if (parentHole.getChildList().size() == 2) {
      // then branch
      this.generateThenStmtInIfStmtForExpr10AndExpr11AndLet6(parent, node, currentHole, holeTypeExpr);
    } else if (parentHole.getChildList().size() > 2) {
      // else branch
      IfStmt elseBranch = new IfStmt();
      elseBranch.setCondition((Expression) node);
      ifStmt.setElseStmt(elseBranch);

      currentHole.set(HoleType.ElseStatement, false);
      HoleNode conditionHole = new HoleNode(HoleType.IfCondition, false);
      currentHole.addChild(conditionHole);
      HoleNode exprWraperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      conditionHole.addChild(exprWraperHole);
      exprWraperHole.addChild(new HoleNode());
    }
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

	/**
	 * 
	 * @param path the compuatation path in AST
	 * @return (parent node in AST, current child node index), parent node can be a
	 *         node, or a List or a NodeList
	 */
	public Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer> getParentOfHole(List<Integer> path) {
		int index;
		Node parent = this.compilationUnit;
		HoleNode parentHole = this.holeAST.getRoot();
		for (index = 0; index < path.size() - 1; index++) {
			HoleNode temp = parentHole.getIthChild(path.get(index));
			parentHole = temp;

			int indexOfHole = path.get(index + 1);
			HoleType holeType = parentHole.getHoleType();
			if (holeType.equals(HoleType.Wrapper)) {
				continue;
			}

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

	private void generateExpInMethodBody(Either<Node, Either<List<?>, NodeList<?>>> parent, HoleNode currentHole,
			Node node, HoleType exprHoleType) {
		MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
		Optional<BlockStmt> optionalBody = mNode.getBody();
		currentHole.set(HoleType.Body, false);

		HoleNode anotherCurrentHole = new HoleNode();
		currentHole.addChild(anotherCurrentHole);

		BlockStmt blockStmt = optionalBody.get();
		NodeList<Statement> statements = blockStmt.getStatements();
		if (statements.size() == 0) {
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add(expressionStmt);

			anotherCurrentHole.set(HoleType.Statements, false);

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
			parentHole.addChild(new HoleNode());
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
			parentHole.addChild(holeNode);
		} else if (parentHole.getHoleType().equals(HoleType.RightSubExpr)) {
			binaryExpr.setRight((Expression) node);
			currentHole.set(HoleType.Expression, false);
			HoleNode holeNode = new HoleNode();
			parentHole.addChild(holeNode);
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
				HoleNode exprWrapperHole = new HoleNode(exprHoleType, false);
				currentHole.addChild(exprWrapperHole);
				parentHole.addChild(new HoleNode());
			} else {
				// right
				binaryExpr.setRight((Expression) node);

				currentHole.set(HoleType.RightSubExpr, false);
				HoleNode anotherCurrentHole = new HoleNode(exprHoleType, false);
				currentHole.addChild(anotherCurrentHole);
				parentOfParentOfParentHole.addChild(new HoleNode());
			}
		}
	}

	private void generateBinarExprInExprForSubExpression(Either<Node, Either<List<?>, NodeList<?>>> parent, int holeIndex,
			Node node, HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole,
			HoleNode parentOfParentOfParentHole, HoleType exprHoleType) {
		BinaryExpr binaryExpr = (BinaryExpr) parent.getLeft();
		if (parentHole.getHoleType().equals(HoleType.LeftSubExpr)) {
			binaryExpr.setLeft((Expression) node);
			currentHole.set(HoleType.Wrapper, false, exprHoleType);
			currentHole.addChild(new HoleNode());
		} else if (parentHole.getHoleType().equals(HoleType.RightSubExpr)) {
			binaryExpr.setRight((Expression) node);
			currentHole.set(HoleType.Wrapper, false, exprHoleType);
			currentHole.addChild(new HoleNode());
		} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
				&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))
				|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr11))) {
			binaryExpr.setRight((Expression) node);
			currentHole.set(HoleType.RightSubExpr, false);
			HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, exprHoleType);
			currentHole.addChild(exprWrapper);
			exprWrapper.addChild(new HoleNode());
		} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
				&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))
				|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
						&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))) {
			if (holeIndex == 0) {
				// left
				binaryExpr.setLeft((Expression) node);

				currentHole.set(HoleType.LeftSubExpr, false);
				HoleNode anotherCurrentHole = new HoleNode(HoleType.Wrapper, false, exprHoleType);
				currentHole.addChild(anotherCurrentHole);
				anotherCurrentHole.addChild(new HoleNode());
			} else {
				// right
				binaryExpr.setRight((Expression) node);

				currentHole.set(HoleType.RightSubExpr, false);
				HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, exprHoleType);
				currentHole.addChild(exprWrapperHole);
				exprWrapperHole.addChild(new HoleNode());
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

			currentHole.set(HoleType.ForUpdate, false);
			currentHole.addChild(new HoleNode(HoleType.Expression, false));
			parentHole.addChild(new HoleNode());
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

			HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsHole);

			HoleNode stmtWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
			stmtsHole.addChild(stmtWrapperHole);

			HoleNode exprHole = new HoleNode(HoleType.Expression, false);
			stmtWrapperHole.addChild(exprHole);

			HoleNode exprWrapperHole = new HoleNode(exprHoleType, false);
			exprHole.addChild(exprWrapperHole);
			stmtsHole.addChild(new HoleNode());
			return blockStmt;
		} else if (bodyClassStr.equals("BlockStmt")) {

		} else {
			System.out.println("Should not go to this branch");
		}

		return body;
	}

	private Statement generateExprStmtBodyForStmtsForLet1AndLet2(Statement body, HoleNode currentHole, Node node,
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
			HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsHole);
			stmtsHole.addChild(this.constructHoleASTOfAssignStmtForLet1AndLet2(exprHoleType));
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
			currentHole.set(HoleType.ThenStatement, false);

			HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsHole);

			HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
			stmtsHole.addChild(holeNodeChild);

			HoleNode childOfHoleNodeChild = new HoleNode(holeTypeExpr, false);
			holeNodeChild.addChild(childOfHoleNodeChild);
			stmtsHole.addChild(new HoleNode());
		} else if (thenStmtStr.equals("BlockStmt")) {
			// Jump out of current if statement.

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

			// HoleNode holeNode = new HoleNode(HoleType.Statement, true);
			parentHole.addChild(new HoleNode());
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
			currentHole.set(HoleType.ThenStatement, false);

			HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsNode);

			HoleNode stmtNode = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
			stmtsNode.addChild(stmtNode);

			HoleNode exprHole = new HoleNode(HoleType.Expression, false);
			stmtNode.addChild(exprHole);

			HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
			exprHole.addChild(exprWrapperHole);

			exprWrapperHole.addChild(new HoleNode(HoleType.Expression));
		} else if (thenStmtStr.equals("BlockStmt")) {
		}
	}

	private void generateSwitchEntry(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, int holeIndex,
			HoleNode currentHole, HoleType holeTypeExpr) {
		SwitchEntry switchEntry = (SwitchEntry) parent.getLeft();
		NodeList<Statement> statements = switchEntry.getStatements();
		currentHole.set(HoleType.Statements, false);
		if (holeIndex < statements.size()) {
			// TODO
		} else {
			ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
			statements.add((Statement) expressionStmt);

			HoleNode exprStmtHole = new HoleNode(HoleType.Expression, false);
			currentHole.addChild(exprStmtHole);
			exprStmtHole.addChild(new HoleNode(holeTypeExpr, false));
			currentHole.addChild(new HoleNode());
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
			currentHole.set(HoleType.Statements, false);

			HoleNode holeNode = new HoleNode(HoleType.Expression, false);
			currentHole.addChild(holeNode);

			HoleNode childHoleNode = new HoleNode(HoleType.Wrapper, false);
			childHoleNode.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
			holeNode.addChild(childHoleNode);
			childHoleNode.addChild(new HoleNode());
		}
	}

	private void generateSwitchEntryForTotalReturnStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			int holeIndex, HoleNode currentHole, HoleType holeTypeExpr) {
		SwitchEntry switchEntry = (SwitchEntry) parent.getLeft();
		NodeList<Statement> statements = switchEntry.getStatements();
		if (holeIndex < statements.size()) {
			// TODO
		} else {
			statements.add((Statement) node);
			currentHole.set(HoleType.Statements, false);

			HoleNode holeNode = new HoleNode(HoleType.Expression, false);
			currentHole.addChild(holeNode);

			HoleNode childHoleNode = new HoleNode(holeTypeExpr, false);
			holeNode.addChild(childHoleNode);
			currentHole.addChild(new HoleNode());
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

			HoleNode holeNode = new HoleNode();
			holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Statement });
			parentOfParentHole.addChild(holeNode);
		} else if (expressionClassStr.equals("AssignExpr")) {
			AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
			assignExpr.setValue((Expression) node);

			currentHole.set(HoleType.AssignExprValue, false);
			currentHole.addChild(new HoleNode(holeTypeExpr, false));

			parentOfParentHole.addChild(new HoleNode());
		}
	}

	private Statement generateTotalStmt(Statement body, Node node, HoleNode currentHole, HoleNode parentHole,
			HoleType holeType) {
		String bodyClassStr = body.getClass().toString();
		bodyClassStr = StringHelper.getClassName(bodyClassStr);
		if (bodyClassStr.equals("ReturnStmt")) {
			BlockStmt blockStmt = new BlockStmt();
			NodeList<Statement> statements = new NodeList<Statement>();
			statements.add((Statement) node);
			blockStmt.setStatements(statements);

			currentHole.set(HoleType.Body, false);
			HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsHole);
			HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeType);
			stmtsHole.addChild(exprWrapperHole);
			parentHole.addChild(new HoleNode());
			return blockStmt;
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
			statements.add((Statement)node);
			blockStmt.setStatements(statements);
      currentHole.set(HoleType.Statements, false); 
			HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeType);
			currentHole.addChild(exprWrapperHole);
			exprWrapperHole.addChild(new HoleNode());
      return blockStmt;
		} else if (bodyClassStr.equals("BlockStmt")) {

		} else {
			System.out.println("Should not go to this branch");
		}
		return body;

	}

	private void generateTotalStmtInIfThenStmt(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
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

			currentHole.set(HoleType.ThenStatement, false);
			HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
			currentHole.addChild(stmtsNode);
			HoleNode holeNodeChild = new HoleNode(HoleType.Expression, false);
			stmtsNode.addChild(holeNodeChild);
			HoleNode childOfanotherChildNode = new HoleNode(holeType, false);
			holeNodeChild.addChild(childOfanotherChildNode);
			parentHole.addChild(new HoleNode());
		} else if (thenStmtStr.equals("BlockStmt")) {
		}
	}


	private void generateReturn6(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, HoleNode currentHole,
			HoleNode parentHole, HoleNode parentOfParentHole, HoleType holeType) {
		ReturnStmt stmt = (ReturnStmt) parent.getLeft();
		stmt.setExpression((Expression) node);
		currentHole.set(holeType, false);
    // since parentOfParentHole is statements hole, it is not suitable for return stmt, so 
    // use parent of it, it is ThenStatement hole node.
    parentOfParentHole.getParent().addChild(new HoleNode());
	}

	private void generateReturn6ForExpr10AndExpr11(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole, HoleType holeType) {
		ReturnStmt stmt = (ReturnStmt) parent.getLeft();
		stmt.setExpression((Expression) node);
		currentHole.set(HoleType.Expression, false);
		HoleNode anotherHole = new HoleNode(HoleType.Wrapper, false);
		anotherHole.setHoleTypeOptionsOfOnlyOne(holeType);
		currentHole.addChild(anotherHole);
		anotherHole.addChild(new HoleNode());
	}

	private void generateExprInAssignExprForExpr3(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeType) {
		AssignExpr assignExpr = (AssignExpr) parent.getLeft();
		assignExpr.setValue((Expression) node);
		String nodeClassStr = StringHelper.getClassName(node.getClass().toString());
    if(nodeClassStr.equals("FieldAccessExpr")){
      // Name dot Name case
      currentHole.set(HoleType.AssignExprValue, false);
      HoleNode nameDotChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.NameDotChain);
      currentHole.addChild(nameDotChainHole);
      nameDotChainHole.addChild(new HoleNode(holeType, false));
      nameDotChainHole.addChild(new HoleNode());
    } else {
      currentHole.set(HoleType.AssignExprValue, false);
      currentHole.addChild(new HoleNode(holeType, false));
      parentOfParentHole.addChild(new HoleNode()); 
    }
	}

  private void generateExprInAssignExpr(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeType) {
    AssignExpr assignExpr = (AssignExpr) parent.getLeft();
    assignExpr.setValue((Expression) node);
    currentHole.set(HoleType.AssignExprValue, false);
    currentHole.addChild(new HoleNode(holeType, false));
    parentOfParentHole.addChild(new HoleNode());
  }

	private void generateExprInAssignExprForExpr10AndExpr11(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeType) {
		AssignExpr assignExpr = (AssignExpr) parent.getLeft();
		assignExpr.setValue((Expression) node);

		currentHole.set(HoleType.AssignExprValue, false);
		HoleNode wrapperHole = new HoleNode(HoleType.Wrapper, false, holeType);
		currentHole.addChild(wrapperHole);
		// HoleNode rightSubHole = new HoleNode(HoleType.RightSubExpr, false);
		// anotherHole.addChild(rightSubHole);
		wrapperHole.addChild(new HoleNode());
	}

	private void generateExprInArguments(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleType holeTypeExpr) {
		NodeList<Expression> arguments = (NodeList<Expression>) parent.get().get();
		arguments.add((Expression) node);

	  currentHole.set(HoleType.Wrapper, false, HoleType.Argument);
    // function call
    if(holeTypeExpr.equals(HoleType.Expr1) || holeTypeExpr.equals(HoleType.Expr2)){
      HoleNode methodCallChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(methodCallChainHole);
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      methodCallChainHole.addChild(exprWrapperHole);
      HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
      exprWrapperHole.addChild(argsHole);
      argsHole.addChild(new HoleNode());
    } else if (holeTypeExpr.equals(HoleType.Expr10)){
      // expression case
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      currentHole.addChild(exprWrapperHole);
      exprWrapperHole.addChild(new HoleNode());
    } else {
		  currentHole.addChild(new HoleNode(holeTypeExpr, false));
		  parentHole.addChild(new HoleNode(HoleType.Argument));
    }
	}

	private void generateCallFunctionExpr(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole, HoleType parentHoleType, int holeIndex,
			String parentNodeClassStr, HoleType holeTypeExpr) {
		String nodeClassStr = StringHelper.getClassName(node.getClass().toString());
		if(parentHole.getHoleTypeOfOptionsIfOnlyOne()!=null && parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.MethodCallExprChain)){
      MethodCallExpr methodCallExpr = (MethodCallExpr)parent.getLeft();

      // Reconstruct a MethodCallExpr as a new scope: newScope.
      Optional<Expression> scope = methodCallExpr.getScope();
      String name = methodCallExpr.getName().getIdentifier();
      NodeList<Expression> arguments = methodCallExpr.getArguments();
      MethodCallExpr newScope = new MethodCallExpr(name);      
      newScope.setArguments(arguments);
      if(scope.isPresent()){
        newScope.setScope(scope.get());
      } 
      MethodCallExpr eNode = (MethodCallExpr)node;
      methodCallExpr.setScope(newScope);
      methodCallExpr.setName(eNode.getName());
      NodeList<Expression> emptyList = new NodeList<Expression>();
      methodCallExpr.setArguments(emptyList);

      currentHole.set(HoleType.Wrapper, false, holeTypeExpr);
      HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
      currentHole.addChild(argsHole);
			argsHole.addChild(new HoleNode());
    }
    else if (parentHoleType.equals(HoleType.Statements)) {
			NodeList<Statement> statements = (NodeList<Statement>) parent.get().get();
			if (holeIndex < statements.size()) {
				// TODO
			} else {
				statements.add(new ExpressionStmt((Expression) node));

				currentHole.set(HoleType.Wrapper, false, HoleType.Statement);
				HoleNode exprHole = new HoleNode(HoleType.Expression, false);
				currentHole.addChild(exprHole);
        HoleNode methodCallChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        exprHole.addChild(methodCallChainHole);
				HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallChainHole.addChild(exprWrapperHole);
				HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
				exprWrapperHole.addChild(argsHole);
				argsHole.addChild(new HoleNode());
			}

		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("MethodDeclaration")) {
			MethodDeclaration mNode = (MethodDeclaration) parent.getLeft();
			Optional<BlockStmt> optionalBody = mNode.getBody();
			currentHole.set(HoleType.Body, false);

			HoleNode anotherCurrentHole = new HoleNode();
			currentHole.addChild(anotherCurrentHole);

			BlockStmt blockStmt = optionalBody.get();
			NodeList<Statement> statements = blockStmt.getStatements();
			if (statements.size() == 0) {
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);

				anotherCurrentHole.set(HoleType.Statements, false);

				HoleNode expressionHole = new HoleNode(HoleType.Expression, false);
				anotherCurrentHole.addChild(expressionHole);

        HoleNode methodCallChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        expressionHole.addChild(methodCallChainHole);

				HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallChainHole.addChild(exprWrapperHole);

				HoleNode holeNodeChild = new HoleNode(HoleType.Arguments, false);
				exprWrapperHole.addChild(holeNodeChild);
				holeNodeChild.addChild(new HoleNode());
			} else {
				// TODO
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("BinaryExpr")) {
			BinaryExpr binaryExpr = (BinaryExpr) parent.getLeft();
			if (parentHole.getHoleType().equals(HoleType.LeftSubExpr)) {
				System.out.println("Shall not go to this branch");
			} else if (parentHole.getHoleType().equals(HoleType.RightSubExpr)) {
				binaryExpr.setRight((Expression) node);
				currentHole.set(HoleType.Expression, false);
        HoleNode methodCallChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        currentHole.addChild(methodCallChainHole);
				HoleNode holeNode = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallChainHole.addChild(holeNode);
				HoleNode anotherHoleNode = new HoleNode(HoleType.Arguments, false);
				holeNode.addChild(anotherHoleNode);
				anotherHoleNode.addChild(new HoleNode());
			} else if ((parentHole.getHoleTypeOfOptionsIfOnlyOne() != null
					&& parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))
					|| (parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne() != null
							&& parentOfParentHole.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr10))) {
				if (holeIndex == 0) {
					// left
					// right
					binaryExpr.setLeft((Expression) node);
					currentHole.set(HoleType.LeftSubExpr, false);
          HoleNode methodCallChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
          currentHole.addChild(methodCallChainHole);
					HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					methodCallChainHole.addChild(exprWrapper);
					HoleNode anotherHoleNode = new HoleNode(HoleType.Arguments, false);
					exprWrapper.addChild(anotherHoleNode);
					anotherHoleNode.addChild(new HoleNode());
				} else {
					// right
					binaryExpr.setRight((Expression) node);
					currentHole.set(HoleType.RightSubExpr, false);
          HoleNode methodCallChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
          currentHole.addChild(methodCallChainHole);
					HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
					methodCallChainHole.addChild(exprWrapper);
					HoleNode anotherHoleNode = new HoleNode(HoleType.Arguments, false);
					exprWrapper.addChild(anotherHoleNode);
					anotherHoleNode.addChild(new HoleNode());
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
				HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
				currentHole.addChild(stmtsHole);
				HoleNode stmtWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
				stmtsHole.addChild(stmtWrapperHole);
				HoleNode exprHole = new HoleNode(HoleType.Expression, false);
				stmtWrapperHole.addChild(exprHole);
        HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
				exprHole.addChild(methodCallExprChainHole);
				HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallExprChainHole.addChild(exprWrapperHole);
				HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
				exprWrapperHole.addChild(argsHole);
				argsHole.addChild(new HoleNode());
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

				HoleNode stmtsHole = new HoleNode(HoleType.Statements, false);
				currentHole.addChild(stmtsHole);

				HoleNode stmtWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
				stmtsHole.addChild(stmtWrapperHole);

				HoleNode exprHole = new HoleNode(HoleType.Expression, false);
				stmtWrapperHole.addChild(exprHole);

        HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        exprHole.addChild(methodCallExprChainHole);

				HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallExprChainHole.addChild(exprWrapperHole);

				HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
				exprWrapperHole.addChild(argsHole);
				argsHole.addChild(new HoleNode());
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
				currentHole.set(HoleType.ThenStatement, false);

				HoleNode statementsHole = new HoleNode(HoleType.Statements, false);
				currentHole.addChild(statementsHole);

				HoleNode statementHole = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);
				statementsHole.addChild(statementHole);

				HoleNode exprHole = new HoleNode(HoleType.Expression, false);
				statementHole.addChild(exprHole);

        HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        exprHole.addChild(methodCallExprChainHole);

				HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallExprChainHole.addChild(exprWrapperHole);

				HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
				exprWrapperHole.addChild(argsHole);
				argsHole.addChild(new HoleNode());
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

        currentHole.set(HoleType.Expression, false);

        HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        currentHole.addChild(methodCallExprChainHole);

				HoleNode exprHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallExprChainHole.addChild(exprHole);

				HoleNode holeNode = new HoleNode(HoleType.Arguments, false);
				parentHole.addChild(holeNode);
				holeNode.addChild(new HoleNode());
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
			SwitchEntry switchEntry = (SwitchEntry) parent.getLeft();
			NodeList<Statement> statements = switchEntry.getStatements();
			if (holeIndex < statements.size()) {
				// TODO
			} else {
				ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add((Statement) expressionStmt);
				currentHole.set(HoleType.Statements, false);

				HoleNode holeNode = new HoleNode(HoleType.Expression, false);
				currentHole.addChild(holeNode);

        HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        holeNode.addChild(methodCallExprChainHole);

				HoleNode childHoleNode = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallExprChainHole.addChild(childHoleNode);

				HoleNode argsNode = new HoleNode(HoleType.Arguments, false);
				childHoleNode.addChild(argsNode);
				argsNode.addChild(new HoleNode());
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

				HoleNode variableHole = new HoleNode(HoleType.Wrapper, false);
				variableHole.setHoleTypeOptionsOfOnlyOne(HoleType.VariableDeclarator);
				variables.addChild(variableHole);

				HoleNode initializerHole = new HoleNode(HoleType.VariableInitializer, false);
				variableHole.addChild(initializerHole);

        HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        initializerHole.addChild(methodCallExprChainHole);

				HoleNode innerHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallExprChainHole.addChild(innerHole);

				HoleNode args = new HoleNode(HoleType.Arguments, false);
				innerHole.addChild(args);

				args.addChild(new HoleNode());
			} else if (expressionClassStr.equals("AssignExpr")) {
				AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
				assignExpr.setValue((Expression) node);
				currentHole.set(HoleType.Expression, false);

        HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
        currentHole.addChild(methodCallExprChainHole);

				HoleNode exprHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
				methodCallExprChainHole.addChild(exprHole);

				HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
				exprHole.addChild(argsHole);

				argsHole.addChild(new HoleNode());
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ReturnStmt")) {
			ReturnStmt stmt = (ReturnStmt) parent.getLeft();
			stmt.setExpression((Expression) node);
			currentHole.set(HoleType.Expression, false);

      HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(methodCallExprChainHole);

			HoleNode exprHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
			methodCallExprChainHole.addChild(exprHole);

			HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
			exprHole.addChild(argsHole);
			argsHole.addChild(new HoleNode());
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("AssignExpr")) {
			AssignExpr assignExpr = (AssignExpr) parent.getLeft();
			assignExpr.setValue((Expression) node);
			currentHole.set(HoleType.AssignExprValue, false);

      HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(methodCallExprChainHole);

			HoleNode exprHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
			methodCallExprChainHole.addChild(exprHole);

			HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
			exprHole.addChild(argsHole);
			argsHole.addChild(new HoleNode());
		} else if (parentHoleType.equals(HoleType.Arguments)) {
			this.generateExprInArguments(parent, node, currentHole, parentHole, holeTypeExpr);
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
			EnclosedExpr enclosedExpr = (EnclosedExpr) parent.getLeft();
			enclosedExpr.setInner((Expression) node);
			currentHole.set(HoleType.InnerExpr, false);
      HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(methodCallExprChainHole);
			HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
			methodCallExprChainHole.addChild(exprWrapper);
			HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
			exprWrapper.addChild(argsHole);
			argsHole.addChild(new HoleNode());
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
			NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parent.getLeft()).getVariables();
			VariableDeclarator vNode = variableDeclarators.get(0);
			vNode.setInitializer((Expression) node);

			currentHole.set(HoleType.VariableDeclarator, false);

			HoleNode variableHole = new HoleNode(HoleType.Wrapper, false);
			variableHole.setHoleTypeOptionsOfOnlyOne(HoleType.VariableDeclarator);
			currentHole.addChild(variableHole);

			HoleNode initializerHole = new HoleNode(HoleType.VariableInitializer, false);
			variableHole.addChild(initializerHole);

      HoleNode methodCallExprChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      initializerHole.addChild(methodCallExprChainHole);
			HoleNode innerHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
			methodCallExprChainHole.addChild(innerHole);
			innerHole.addChild(new HoleNode());
		}
    else if(parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
      VariableDeclarator variableDeclarator = (VariableDeclarator)parent.getLeft();
      variableDeclarator.setInitializer((Expression)node);
      currentHole.set(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      currentHole.addChild(exprWrapperHole);
      exprWrapperHole.addChild(new HoleNode()); 
    }
    else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")){
      this.generateConditionExprThenElseForCall(parent, node, currentHole, parentHole, parentOfParentHole, holeTypeExpr);
    }
    else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")){
      UnaryExpr unaryExpr = (UnaryExpr)parent.getLeft();
      unaryExpr.setExpression((Expression)node);
      currentHole.set(HoleType.Expression, false);
      HoleNode methodCallChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(methodCallChainHole);
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      methodCallChainHole.addChild(exprWrapperHole);
      HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
			exprWrapperHole.addChild(argsHole);
			argsHole.addChild(new HoleNode());
    } else if (parentNodeClassStr != null && parentNodeClassStr.equals("LambdaExpr")){
      LambdaExpr lambdaExpr = (LambdaExpr)parent.getLeft();
      Statement body = lambdaExpr.getBody();

      String bodyClassStr = body.getClass().toString();
      bodyClassStr = StringHelper.getClassName(bodyClassStr);
      if (bodyClassStr.equals("ReturnStmt")) {
        BlockStmt blockStmt = new BlockStmt();
        NodeList<Statement> statements = new NodeList<Statement>();
        ExpressionStmt expressionStmt = new ExpressionStmt((Expression) node);
				statements.add(expressionStmt);
        blockStmt.setStatements(statements);
        lambdaExpr.setBody(blockStmt);

        currentHole.set(HoleType.Body, false);
        HoleNode stmtsNode = new HoleNode(HoleType.Statements, false);
        currentHole.addChild(stmtsNode);
        HoleNode exprNodeHole = new HoleNode(HoleType.Expression, false);
        stmtsNode.addChild(exprNodeHole);
        HoleNode holeNodeChild = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
        exprNodeHole.addChild(holeNodeChild);
        HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
        holeNodeChild.addChild(argsHole);
        argsHole.addChild(new HoleNode());
      } else if (bodyClassStr.equals("BlockStmt")) {

      } else {
        System.out.println("Should not go to this branch");
      }

    }
  }

  private void generateConditionExprThenElseForCall(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, 	HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole, HoleType holeTypeExpr){

    ConditionalExpr conditionalExpr = (ConditionalExpr)parent.getLeft();
    int childListSize = parentHole.getChildListSize();
    if(childListSize==1){
      conditionalExpr.setCondition((Expression)node);
      currentHole.set(HoleType.ConditionalExprCondition, false);
      HoleNode callChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(callChainHole);
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      callChainHole.addChild(exprWrapperHole);
      exprWrapperHole.addChild(new HoleNode());  
    } else if (childListSize==2){
      conditionalExpr.setThenExpr((Expression)node);
      currentHole.set(HoleType.ConditionalExprThen, false);
      HoleNode callChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(callChainHole);
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      callChainHole.addChild(exprWrapperHole);
      exprWrapperHole.addChild(new HoleNode());  
    } else {
      conditionalExpr.setElseExpr((Expression)node);
      currentHole.set(HoleType.ConditionalExprElse, false);
      HoleNode callChainHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExprChain);
      currentHole.addChild(callChainHole);
      HoleNode exprWrapperHole = new HoleNode(HoleType.Wrapper, false, holeTypeExpr);
      callChainHole.addChild(exprWrapperHole);
      exprWrapperHole.addChild(new HoleNode());  
   }
  }
  private void generateExpressionForVariableDecalator(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node, 	HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeTypeExpr){
    VariableDeclarator variableDeclarator = (VariableDeclarator)parent.getLeft();
    variableDeclarator.setInitializer((Expression)node);
    currentHole.set(holeTypeExpr, false);
    parentOfParentHole.addChild(new HoleNode()); 
  }

	private void generateExprForEnclosedExpr(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeTypeExpr) {
		EnclosedExpr enclosedExpr = (EnclosedExpr) parent.getLeft();
		enclosedExpr.setInner((Expression) node);
		currentHole.set(HoleType.InnerExpr, false);

		HoleNode exprHole = new HoleNode(holeTypeExpr, false);
		currentHole.addChild(exprHole);
		parentOfParentHole.addChild(new HoleNode());
	}

	private void generateExprForFieldDeclaration(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			HoleNode currentHole, HoleNode parentOfParentHole, HoleType holeTypeExpr) {
		NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parent.getLeft()).getVariables();
		VariableDeclarator vNode = variableDeclarators.get(0);
		vNode.setInitializer((Expression) node);

		currentHole.set(HoleType.VariableDeclarator, false);
		HoleNode holeNode = new HoleNode();
		holeNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
		parentOfParentHole.addChild(holeNode);
	}

	private void generateExprForExpr5AndExpr14AndExpr15(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			String parentNodeClassStr, int holeIndex, HoleNode currentHole, HoleNode parentHole, HoleNode parentOfParentHole,
			HoleNode parentOfParentOfParentHole, HoleType parentHoleType, HoleType holeTypeExpr) {
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
			if (holeIndex == 0) {
				// i < 10 in while(i < 10){}
				WhileStmt whileStmt = (WhileStmt) parent.getLeft();
				whileStmt.setCondition((Expression) node);
				currentHole.set(HoleType.Expression, false);
				parentHole.addChild(new HoleNode());
			} else {
				this.generateExprStmtInWhileStmt(parent, currentHole, node, holeTypeExpr);
			}
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

				currentHole.set(HoleType.Wrapper, false, HoleType.SwitchEntry);
				HoleNode labelsHole = new HoleNode(HoleType.SwitchEntryLabels, false);
				currentHole.addChild(labelsHole);
				labelsHole.addChild(new HoleNode(HoleType.Expression, false, holeTypeExpr));
				currentHole.addChild(new HoleNode());
			}
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchEntry")) {
			this.generateSwitchEntry(parent, node, holeIndex, currentHole, holeTypeExpr);
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("FieldDeclaration")) {
			NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parent.getLeft()).getVariables();
			VariableDeclarator vNode = variableDeclarators.get(0);
			vNode.setInitializer((Expression) node);

			currentHole.set(HoleType.VariableDeclarator, false);
			HoleNode holeNode = new HoleNode();
			holeNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
			parentOfParentHole.addChild(holeNode);
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("ExpressionStmt")) {
			this.generateExpForExpressionStmt(parent, node, holeIndex, currentHole, parentOfParentHole, holeTypeExpr);
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarationExpr")) {
			VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) parent.getLeft();
			NodeList<VariableDeclarator> variableDeclarators = variableDeclarationExpr.getVariables();
			// only consider one variable case
			variableDeclarators.get(0).setInitializer((Expression) node);
			currentHole.set(HoleType.Expression, false);
			parentOfParentHole.addChild(new HoleNode(HoleType.Expression));
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("SwitchStmt")) {
			SwitchStmt switchStmt = (SwitchStmt) parent.getLeft();
			NodeList<SwitchEntry> switchEntries = switchStmt.getEntries();
			currentHole.set(HoleType.SwitchEntries, false);
			if (switchEntries.size() == 0) {
				SwitchEntry switchEntry = new SwitchEntry();
				NodeList<Expression> labels = new NodeList<Expression>();
				labels.add((Expression) node);
				switchEntry.setLabels(labels);
				switchEntries.add(switchEntry);

				HoleNode switchEntryWrapperHole = new HoleNode(HoleType.Wrapper, false, HoleType.SwitchEntry);
				currentHole.addChild(switchEntryWrapperHole);
				HoleNode labelsHole = new HoleNode(HoleType.SwitchEntryLabels, false);
				switchEntryWrapperHole.addChild(labelsHole);
				labelsHole.addChild(new HoleNode(HoleType.Expression, false, holeTypeExpr));
				switchEntryWrapperHole.addChild(new HoleNode());
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
			HoleNode holeNode = new HoleNode(HoleType.Wrapper, false);
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
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("EnclosedExpr")) {
			this.generateExprForEnclosedExpr(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
		} else if (parentNodeClassStr != null && parentNodeClassStr.equals("VariableDeclarator")){
      this.generateExpressionForVariableDecalator(parent, node, currentHole, parentOfParentHole, holeTypeExpr);
    } else if (parentNodeClassStr != null && parentNodeClassStr.equals("ConditionalExpr")) {
      this.generateConditionExprThenElseSimple(parent, node, holeIndex, currentHole, parentHole, holeTypeExpr);
    } else if (parentNodeClassStr != null && parentNodeClassStr.equals("UnaryExpr")) {
      UnaryExpr unaryExpr = (UnaryExpr)parent.getLeft();
      unaryExpr.setExpression((Expression)node);
      currentHole.set(HoleType.Expression, false);
      currentHole.addChild(new HoleNode(holeTypeExpr, false));
      parentHole.addChild(new HoleNode());
    }
	}

  private void generateExprInUnaryExpr(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
  int holeIndex, HoleNode currentHole, HoleNode parentHole, HoleType holeTypeExpr){
    UnaryExpr unaryExpr = (UnaryExpr)parent.getLeft();
    unaryExpr.setExpression((Expression)node);
    currentHole.set(HoleType.Expression, false); 
    currentHole.addChild(new HoleNode(holeTypeExpr, false));
    parentHole.addChild(new HoleNode());
  }

  private void generateConditionExprThenElseSimple(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
  int holeIndex, HoleNode currentHole, HoleNode parentHole, HoleType holeTypeExpr){
    ConditionalExpr conditionalExpr = (ConditionalExpr)parent.getLeft();
    int childListSize = parentHole.getChildListSize();
    if(childListSize==1){
     conditionalExpr.setCondition((Expression)node);
     currentHole.set(HoleType.ConditionalExprCondition, false);
     currentHole.addChild(new HoleNode(holeTypeExpr, false));
     parentHole.addChild(new HoleNode());
    }
    else if(childListSize==2){
      // then
      conditionalExpr.setThenExpr((Expression)node);
      currentHole.set(HoleType.ConditionalExprThen, false);
      currentHole.addChild(new HoleNode(holeTypeExpr, false));
      parentHole.addChild(new HoleNode());
    } else if (childListSize==3){
      // else
      conditionalExpr.setElseExpr((Expression)node);
      currentHole.set(HoleType.ConditionalExprElse, false);
      currentHole.addChild(new HoleNode(holeTypeExpr, false));
      parentHole.addChild(new HoleNode());
 
   } else {
      System.out.println("ConditionalExpr's child list size shall not be " + childListSize);
    }

  }

	private HoleNode constructHoleASTOfAssignStmtForLet1AndLet2(HoleType holeTypeExpr) {
		HoleNode stmtNode = new HoleNode(HoleType.Wrapper, false, HoleType.Statement);

		HoleNode exprNode = new HoleNode(HoleType.Expression, false);
		stmtNode.addChild(exprNode);

		HoleNode exprWrapper = new HoleNode(HoleType.Wrapper, false);
		exprWrapper.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
		exprNode.addChild(exprWrapper);

		HoleNode targetNode = new HoleNode(HoleType.AssignExprTarget, false);
		exprWrapper.addChild(targetNode);

		HoleNode assignValueHole = new HoleNode(HoleType.AssignExprValue, false);
		exprWrapper.addChild(assignValueHole);

		HoleNode methodCallHole = new HoleNode(HoleType.Wrapper, false, HoleType.MethodCallExpr);
		assignValueHole.addChild(methodCallHole);

		HoleNode argsHole = new HoleNode(HoleType.Arguments, false);
		methodCallHole.addChild(argsHole);
		argsHole.addChild(new HoleNode());

		return stmtNode;
	}

	private void generateExpreStmtForExpr10AndExpr11(Either<Node, Either<List<?>, NodeList<?>>> parent, Node node,
			int holeIndex, HoleNode currentHole, HoleNode parentHole, HoleType holeTypeExpr) {
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
			exprHole.addChild(new HoleNode());
		} else if (expressionClassStr.equals("AssignExpr")) {
			AssignExpr assignExpr = (AssignExpr) expressionStmt.getExpression();
			assignExpr.setValue((Expression) node);

			currentHole.set(HoleType.Expression, false);
			HoleNode valueHole = new HoleNode(HoleType.AssignExprValue, false);
			currentHole.addChild(valueHole);

			HoleNode exprHole = new HoleNode(HoleType.Wrapper, false);
			exprHole.setHoleTypeOptionsOfOnlyOne(holeTypeExpr);
			valueHole.addChild(exprHole);
			exprHole.addChild(new HoleNode());
		}
	}


  private void inefficientMove(String text){
    this.generate("move next");
    this.generate(text);
  }
}
