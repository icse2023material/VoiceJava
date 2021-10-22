package cn.edu.lyun.kexin.text2code.astskeleton;

import java.util.*;

public class HoleNode {
	private HoleType[] holeTypeOptions;
	private HoleType holeType;
	private boolean isHole;

	private List<HoleNode> childList;

	public HoleNode() {
		this.isHole = true;
		this.holeType = HoleType.Undefined;
		this.childList = new ArrayList<HoleNode>();
	}

	public HoleNode(HoleType holeType, boolean isHole) {
		this.holeType = holeType;
		this.isHole = isHole;
		this.childList = new ArrayList<HoleNode>();

	}

	public void setHoleTypeOptions(HoleType[] holeTypeOptions) {
		this.holeTypeOptions = holeTypeOptions;
	}

	public HoleType[] getHoleTypeOptions() {
		return this.holeTypeOptions;
	}

	public HoleType getHoleTypeOfOptionsIfOnlyOne() {
		if (this.holeTypeOptions != null && this.holeTypeOptions.length == 1) {
			return this.holeTypeOptions[0];
		} else {
			return null;
		}
	}

	public void setHoleType(HoleType holeType) {
		this.holeType = holeType;
	}

	public HoleType getHoleType() {
		return this.holeType;
	}

	public void setIsHole(boolean isHole) {
		this.isHole = isHole;
	}

	public boolean getIsHole() {
		return this.isHole;
	}

	public void set(HoleType holeType, boolean isHole) {
		this.holeType = holeType;
		this.isHole = isHole;
	}

	public List<HoleNode> getChildList() {
		return this.childList;
	}

	public void addChild(HoleNode holeNode) {
		this.childList.add(holeNode);
	}

	public HoleNode getIthChild(int index) {
		return this.childList.get(index);
	}

	public int getChildListSize() {
		return this.childList.size();
	}

	public void deleteHole(int index) {
		this.childList.remove(index);
	}

}
