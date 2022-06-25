public class Pair {

    public void generatePngOfHoleAst() {
        HoleNode exprHole = new HoleNode(holeType.undefined, true);
        if (holeType.equals(holeType.parameters)) {
            currentHole.set(holeType.parameters, false);
            parentHole.addChild(exprHole);
        } else if (holeType.equals(holeType.forInitialization)) {
            currentHole.set(holeType.forInitialization, false);
            exprHole.setHoleTypeOptionsOfOnlyOne(holeType.forCompare);
            parentHole.addChild(exprHole);
        } else {
            parentHole.deleteHole(holeIndex);
            parentOfParentHole.addChild(exprHole);
        }
    }
}
