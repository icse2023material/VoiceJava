package cn.edu.lyun.kexin.text2code.astskeleton;

import java.util.*;

import java.io.FileWriter;
import java.io.IOException;

import cn.edu.lyun.util.Pair;

public class HoleAST {
	private HoleNode root;

	public HoleAST() {
		HoleNode cu = new HoleNode(HoleType.CompilationUnit, false);
		HoleNode child = new HoleNode(HoleType.Undefined, true);
		child.setHoleTypeOptions(
				new HoleType[] { HoleType.PackageDeclaration, HoleType.ImportDeclaration, HoleType.TypeDeclaration });
		cu.addChild(child);
		this.root = cu;
	}

	/**
	 * 
	 * @return ((parentNode, currentNode), pathToCurrentNode)
	 */
	public Pair<Pair<HoleNode, HoleNode>, List<Integer>> getCurrentHole() {
		List<Integer> path = new ArrayList<Integer>();
		return getCurrentHoleRecursive(this.root, this.root, path);
	}

	public Pair<Pair<HoleNode, HoleNode>, List<Integer>> getCurrentHoleRecursive(HoleNode parent, HoleNode node,
			List<Integer> path) {
		if (node.getIsHole()) {
			Pair<HoleNode, HoleNode> nodePair = new Pair<HoleNode, HoleNode>(parent, node);
			return new Pair<Pair<HoleNode, HoleNode>, List<Integer>>(nodePair, path);
		} else {
			List<HoleNode> childList = node.getChildList();
			for (int index = 0; index < childList.size(); index++) {
				List<Integer> clonedPath = new ArrayList<Integer>(path);
				clonedPath.add(index);
				Pair<Pair<HoleNode, HoleNode>, List<Integer>> result = getCurrentHoleRecursive(node, childList.get(index),
						clonedPath);
				if (result != null) {
					return result;
				}
			}
			return null;
		}
	}

	public HoleNode getRoot() {
		return this.root;
	}

	public HoleNode getParentOfNode(List<Integer> path) {
		HoleNode parent = this.root;
		if (path.size() < 2) {
			return this.root;
		}

		for (int i = 0; i < path.size() - 2; i++) {
			parent = parent.getIthChild(path.get(i));
		}

		return parent;
	}

	public HoleNode getParentOfParentOfParentNode(List<Integer> path) {
		HoleNode parent = this.root;
		if (path.size() < 3) {
			return this.root;
		}

		for (int i = 0; i < path.size() - 3; i++) {
			parent = parent.getIthChild(path.get(i));
		}

		return parent;
	}

	private HoleNode getParentNodeOfIndex(List<Integer> path, int backStep) {
		HoleNode parent = this.root;
		if (path.size() < 3) {
			return this.root;
		}

		for (int i = 0; i < path.size() - backStep; i++) {
			parent = parent.getIthChild(path.get(i));
		}

		return parent;
	}

	public HoleNode checkExpr10NodeAndGoToCorrectParent(HoleNode holeNode, List<Integer> path) {

		HoleType holeType = holeNode.getHoleTypeOfOptionsIfOnlyOne();
		if (holeType != null && holeType.equals(HoleType.Expr10) && holeNode.getChildListSize() == 2) {
			HoleNode pHoleNode = getParentNodeOfIndex(path, 4);
			if (pHoleNode.getHoleType().equals(HoleType.Statements)) {
				return pHoleNode;
			} else {
				return getParentNodeOfIndex(path, 5);
			}
		}
		return holeNode;
	}

	public void writeDotFile() {
		FileWriter filewriter;
		try {
			filewriter = new FileWriter("holeAST.dot");
			filewriter.write("digraph {");
			filewriter.write("\n");
			Queue<HoleNode> queue = new ArrayDeque<>();
			queue.add(this.root);
			String colorRed = " [color = red] ";
			while (!queue.isEmpty()) {
				HoleNode holeNode = queue.poll();
				String nodeStr = holeNode.getHoleType().equals(HoleType.Wrapper)
						? holeNode.getHoleType() + "_" + holeNode.getHoleTypeOfOptionsIfOnlyOne() + "_"
								+ String.valueOf(holeNode.hashCode())
						: holeNode.getHoleType() + "_" + String.valueOf(holeNode.hashCode());
				if (holeNode.getIsHole()) {
					nodeStr += colorRed;
				}

				for (HoleNode holeNodeChild : holeNode.getChildList()) {
					queue.add(holeNodeChild);
					HoleType childHoleType = holeNodeChild.getHoleType();
					String nodeChildStr = childHoleType.equals(HoleType.Wrapper)
							? childHoleType + "_" + holeNodeChild.getHoleTypeOfOptionsIfOnlyOne() + "_"
									+ String.valueOf(holeNodeChild.hashCode())
							: childHoleType + "_" + String.valueOf(holeNodeChild.hashCode());
					if (holeNodeChild.getIsHole()) {
						nodeChildStr += colorRed;
					}
					String line = nodeStr + " -> " + nodeChildStr;
					filewriter.write(line);
					filewriter.write("\n");
				}
			}
			filewriter.write("}");
			filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generateDotAndPNGOfHoleAST() {
		this.writeDotFile();
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("dot -Tpng holeAST.dot -o holeAST.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cleverMove() {
		Pair<Pair<HoleNode, HoleNode>, List<Integer>> pair = getCurrentHole();
		HoleNode parentHole = pair.getFirst().getFirst();
		List<Integer> path = pair.getSecond();

		boolean deleted = false;
		while (hashOnlyOneChild(parentHole) || isNodeChildrenFull(parentHole) || isChildIsReturnHole(parentHole)) {
			int index = path.remove(path.size() - 1);
			if (!deleted) {
				deleted = true;
				parentHole.deleteHole(index);
			}
			parentHole = parentHole.getParent();
      // Patch: ForCompare -> Expr16 Case
      if(parentHole.getHoleType().equals(HoleType.ForCompare) && parentHole.getIthChild(0).getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.Expr16)){
        parentHole = parentHole.getParent();
      }
		}
		if (deleted) {
			HoleNode newHole = new HoleNode();
			parentHole.addChild(newHole);
		}
	}

  public boolean isChildIsReturnHole(HoleNode holeNode){
    HoleNode lastChildNode = holeNode.getIthChild(holeNode.getChildListSize()-1);
    HoleType holeType = lastChildNode.getHoleTypeOfOptionsIfOnlyOne();
    if(holeType != null && holeType.equals(HoleType.Return)){
      return true;
    } else {
      return false;
    }
  }

	public boolean hashOnlyOneChild(HoleNode holeNode) {
		Set<HoleType> set = new HashSet<HoleType>(Arrays.asList(HoleType.ImportDeclaration, HoleType.TypeDeclaration, HoleType.VariableDeclarators, HoleType.VariableDeclarationExpr,
				HoleType.Expression, HoleType.Body, HoleType.Statement, HoleType.VariableInitializer, HoleType.ForInitialization, HoleType.IfCondition,
				HoleType.Expr1, HoleType.Expr2, HoleType.ThenStatement, HoleType.AssignExprValue, HoleType.Expr16, HoleType.Return, HoleType.ForCompare,
        HoleType.Argument, HoleType.ConditionalExprThen, HoleType.ConditionalExprElse, HoleType.ConditionalExprCondition, HoleType.NewInstance,
				HoleType.RightSubExpr, HoleType.LeftSubExpr, HoleType.InnerExpr, HoleType.TryBlock));
		return holeNode.getNonUndefinedChildListSize() == 1 && set.contains(holeNode.getHoleType());
	}

	public boolean isNodeChildrenFull(HoleNode holeNode) {
		Set<HoleType> oneChildSet = new HashSet<HoleType>(Arrays.asList(HoleType.Statement, HoleType.VariableDeclarators, HoleType.VariableDeclarationExpr,
				HoleType.Expr1, HoleType.Expr2, HoleType.MethodCallExpr, HoleType.LetExpr, HoleType.ForInitialization,HoleType.ConditionalExprThen, HoleType.ConditionalExprElse, HoleType.ConditionalExprCondition,
        HoleType.Argument, HoleType.LeftSubExpr, HoleType.RightSubExpr, HoleType.Expr16, HoleType.Return, HoleType.IfCondition, HoleType.NewInstance, HoleType.ForCompare,
				HoleType.Expr11, HoleType.InnerExpr, HoleType.FieldDeclaration, HoleType.TryBlock));
		Set<HoleType> twoChildrenSet = new HashSet<HoleType>(Arrays.asList(HoleType.LetExpr, HoleType.CatchClause, HoleType.Expr19Cast,
				HoleType.SwitchEntry, HoleType.SwitchStmt, HoleType.Expr10, HoleType.WhileStmt, HoleType.VariableDeclarator, HoleType.Expr17));
		Set<HoleType> threeChildrenSet = new HashSet<HoleType>(Arrays.asList(HoleType.BodyDeclaration, HoleType.Expr15));
		if (holeNode.getHoleType().equals(HoleType.Wrapper)) {
			HoleType holeType = holeNode.getHoleTypeOfOptionsIfOnlyOne();
			if (holeType != null) {
				// children: [returnType, arguments. body]
				if (holeType.equals(HoleType.MethodDeclaration)) {
					HoleNode parentOfParent = holeNode.getParent().getParent();
					if (parentOfParent.getHoleTypeOfOptionsIfOnlyOne().equals(HoleType.InterfaceDeclaration)) {
						return holeNode.getNonUndefinedChildListSize() == 2;
					} else {
						return holeNode.getChildList().get(holeNode.getChildListSize() - 1).getHoleType().equals(HoleType.Body);
					}
				}
				if (holeType.equals(HoleType.ForStmt)) {
					return holeNode.getChildList().get(holeNode.getChildListSize() - 1).getHoleType().equals(HoleType.Body);
				}

				if (holeType.equals(HoleType.IfStmt)) {
					if (holeNode.getNonUndefinedChildListSize() == 3) {
						return holeNode.getChildList().get(holeNode.getChildListSize() - 1).getHoleType()
								.equals(HoleType.ElseStatement);
					}
				} else if (oneChildSet.contains(holeType)) {
					return holeNode.getNonUndefinedChildListSize() == 1;
				} else if (twoChildrenSet.contains(holeType)) {
					return holeNode.getNonUndefinedChildListSize() == 2;
				} else if (threeChildrenSet.contains(holeType)) {
					return holeNode.getNonUndefinedChildListSize() == 3;
				}

				return false;
			}
			return false;
		}
		if (holeNode.getHoleType().equals(HoleType.ElseStatement))

		{
			// default else branch
			if (holeNode.getNonUndefinedChildListSize() == 1
					&& holeNode.getIthChild(0).getHoleType().equals(HoleType.Statements)) {
				return true;
			} else if (holeNode.getNonUndefinedChildListSize() == 3) {
				return holeNode.getChildList().get(holeNode.getChildListSize() - 1).getHoleType()
						.equals(HoleType.ElseStatement);
			}
		}

		return false;
	}

}
