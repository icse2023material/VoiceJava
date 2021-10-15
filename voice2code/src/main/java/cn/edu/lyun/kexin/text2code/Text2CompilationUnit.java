package cn.edu.lyun.kexin.text2code;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import cn.edu.lyun.util.Pair;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import cn.edu.lyun.kexin.text2ast.ASTManager;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleAST;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleNode;
import cn.edu.lyun.kexin.text2code.astskeleton.HoleType;
import cn.edu.lyun.kexin.text2code.astskeleton.TypeNameMap;
import cn.edu.lyun.kexin.text2pattern.nfa.RegexSet;
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

		switch (pattern.getName()) {
			case "moveNext":
				// delete current hole, move to next one
				parentHole.deleteHole(holeIndex);
				// TODO: small step move. Not syntax-directed.
				HoleNode parentOfParentHole = this.holeAST.getParentOfNode(path);
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
					holeNode = new HoleNode(HoleType.Undefined, true);
					holeNode.setHoleTypeOptions(new HoleType[] { HoleType.ImportDeclaration });
					parentHole.addChild(holeNode);
				}
				break;
			case "interface":
				parentNode = (CompilationUnit) parentNodeAndIndex.getFirst();
				break;
			case "class":
				parentNode = (CompilationUnit) parentNodeAndIndex.getFirst();
				if (parentHole.getHoleType().equals(HoleType.Wrapper)) {
					parentNode.addType((ClassOrInterfaceDeclaration) node);

					currentHole.setIsHole(false);
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
				pNode = (ClassOrInterfaceDeclaration) parentNodeAndIndex.getFirst();
				pNode.addMember((BodyDeclaration<?>) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.FieldDeclaration);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.Expression });
				currentHole.addChild(holeNode);
				break;
			case "typeExtends":
				MethodDeclaration mNode = (MethodDeclaration) parentNodeAndIndex.getFirst();
				mNode.setType((Type) node);
				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.TypeExtends);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] {});
				parentHole.addChild(holeNode);
				break;
			case "typeDefine":
				break;
			case "for":
				break;
			case "while":
				break;
			case "if":
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
				break;
			case "let2":
				break;
			case "let3":
				break;
			case "let4":
				break;
			case "let5":
				break;
			case "let6":
				break;
			case "return1":
				break;
			case "return2":
				break;
			case "return3":
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
				NodeList<VariableDeclarator> variableDeclarators = ((FieldDeclaration) parentNodeAndIndex.getFirst())
						.getVariables();
				VariableDeclarator vNode = variableDeclarators.get(0);
				vNode.setInitializer((Expression) node);

				currentHole.setIsHole(false);
				currentHole.setHoleType(HoleType.VariableDeclarator);
				holeNode = new HoleNode(HoleType.Undefined, true);
				holeNode.setHoleTypeOptions(new HoleType[] { HoleType.BodyDeclaration });
				parentOfParentHole = this.holeAST.getParentOfNode(path);
				parentOfParentHole.addChild(holeNode);
				break;
			case "expr6":
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
					List<?> nodeList = (List<?>) method.invoke(parent);
					parent = (Node) nodeList.get(indexWithSameType);
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
