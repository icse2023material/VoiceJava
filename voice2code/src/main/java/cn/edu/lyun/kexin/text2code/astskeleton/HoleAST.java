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

}
