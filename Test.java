package cn.edu.anonymous.kexin.text2code;

import java.io.filewriter;
import java.io.ioexception;
import java.lang.reflect.invocationtargetexception;
import java.lang.reflect.method;
import java.sql.ref;
import java.util.*;
import cn.edu.anonymous.util.pair;
import cn.edu.anonymous.util.string.helper;
import com.github.javaparser.ast.compilationunit;
import com.github.javaparser.ast.importdeclaration;
import com.github.javaparser.ast.node;
import com.github.javaparser.ast.nodelist;
import com.github.javaparser.ast.packagedeclaration;
import com.github.javaparser.ast.body.bodydeclaration;
import com.github.javaparser.ast.body.classorinterfacedeclaration;
import com.github.javaparser.ast.body.fielddeclaration;
import com.github.javaparser.ast.body.methoddeclaration;
import com.github.javaparser.ast.body.parameter;
import com.github.javaparser.ast.type.classorinterfacetype;
import com.github.javaparser.ast.type.referencetype;
import com.github.javaparser.ast.type.type;
import com.github.javaparser.ast.type.typeparameter;
import com.github.javaparser.ast.body.variabledeclarator;
import com.github.javaparser.ast.expr.assignexpr;
import com.github.javaparser.ast.expr.binaryexpr;
import com.github.javaparser.ast.expr.boolean.literalexpr;
import com.github.javaparser.ast.expr.castexpr;
import com.github.javaparser.ast.expr.conditionalexpr;
import com.github.javaparser.ast.expr.enclosedexpr;
import com.github.javaparser.ast.expr.expression;
import com.github.javaparser.ast.expr.fieldaccessexpr;
import com.github.javaparser.ast.expr.instanceofexpr;
import com.github.javaparser.ast.expr.lambdaexpr;
import com.github.javaparser.ast.expr.methodcallexpr;
import com.github.javaparser.ast.expr.nameexpr;
import com.github.javaparser.ast.expr.nullliteralexpr;
import com.github.javaparser.ast.expr.objectcreationexpr;
import com.github.javaparser.ast.expr.simplename;
import com.github.javaparser.ast.expr.unaryexpr;
import com.github.javaparser.ast.expr.variabledeclarationexpr;
import com.github.javaparser.ast.stmt.blockstmt;
import com.github.javaparser.ast.stmt.catchclause;
import com.github.javaparser.ast.stmt.expressionstmt;
import com.github.javaparser.ast.stmt.forstmt;
import com.github.javaparser.ast.stmt.ifstmt;
import com.github.javaparser.ast.stmt.returnstmt;
import com.github.javaparser.ast.stmt.statement;
import com.github.javaparser.ast.stmt.switchentry;
import com.github.javaparser.ast.stmt.switchstmt;
import com.github.javaparser.ast.stmt.trystmt;
import com.github.javaparser.ast.stmt.whilestmt;
import com.github.javaparser.printer.lexicalpreservation.lexicalpreservingprinter;
import cn.edu.anonymous.kexin.text2ast.astmanager;
import cn.edu.anonymous.kexin.text2ast.fieldast;
import cn.edu.anonymous.kexin.text2code.astskeleton.holeast;
import cn.edu.anonymous.kexin.text2code.astskeleton.holenode;
import cn.edu.anonymous.kexin.text2code.astskeleton.holetype;
import cn.edu.anonymous.kexin.text2code.astskeleton.typenamemap;
import cn.edu.anonymous.kexin.text2pattern.nfa.regexset;
import cn.edu.anonymous.kexin.text2pattern.pattern.pattern;
import cn.edu.anonymous.kexin.text2pattern.pattern.patternset;
import io.vavr.partialfunction;
import io.vavr.control.either;
import javassist.bytecode.stackmap.basicblock.catch;

public class Text2CompilationUnit {

    private CompilationUnit compilationUnit;

    private HoleAst holeAst;

    private boolean isDebug;

    public HoleAst getHoleAst() {
        return this.holeAst;
    }

    public Node getCompilationUnit() {
        return this.compilationUnit;
    }

    public void generatePngofHoleAst() {
        this.holeAst.generateDotAndPngofHoleAst();
    }

    public CompilationUnit generate(string text) {
        Pattern pattern = RegexSet.compile(new PatternSet()).matchPattern(text.trim()).concatNames();
        if (pattern == null) {
            system.out.println("Match failed");
            return null;
        } else {
        }
        if (this.isDebug) {
            system.out.println("[log] matched pattern name:" + pattern.getName());
        } else {
        }
        Node node = ASTManager.generate(pattern);
        Pair<Pair<HoleNode, HoleNode>, List<Integer>> holePosition = this.holeAst.getCurrentHole();
        List<Integer> path = holePosition.getSecond();
        Pair<HoleNode, HoleNode> parentAndCurrentHole = holePosition.getFirst();
        HoleNode parentHole = parentAndCurrentHole.getFirst();
        HoleNode currentHole = parentAndCurrentHole.getSecond();
        HoleType parentHoleType = parentHole.getHoleType();
        HoleNode parentOfParentHole = parentHole.getParent();
        HoleNode parentOfParentOfParentHole = null;
        if (parentOfParentHole != null) {
            parentOfParentOfParentHole = parentOfParentHole.getParent();
        } else {
        }
        Pair<Either<Node, Either<List<?>, NodeList<?>>>, Integer> parentAndIndex = this.getParentOfHole(path);
        Either<Node, Either<List<?>, NodeList<?>>> parent = parentAndIndex.getFirst();
        int holeIndex = parentAndIndex.getSecond();
        string parentNodeClassStr = null;
        if (parent.isLeft()) {
            parentNodeClassStr = StringHelper.getClassName(parent.getLeft().getClass().toString());
        } else {
        }
        switch(pattern.getName()) {
            case "moveNext":
                HoleNode exprHole = new HoleNode(holeType.undefined, true);
                if (currentHole.getHoleTypeOfOptionsIfOnlyOne() != null) {
                    HoleType holeType = currentHole.getHoleTypeOfOptionsIfOnlyOne();
                    if (holeType.equals(holeType.parameters)) {
                        currentHole.set(holeType.parameters, false);
                        parentHole.addChild(exprHole);
                    } else if (holeType.equals(holeType.forInitialization)) {
                        currentHole.set(holeType.forInitialization, false);
                        exprHole.setHoleTypeOptionsOfOnlyOne(holeType.forCompare);
                        parentHole.addChild(exprHole);
                    } else if (holeType.equals(holeType.forCompare)) {
                        ForStmt forStmt = (ForStmt) parent.getLeft();
                        forStmt.setCompare(new BooleanLiteralExpr(true));
                        currentHole.set(holeType.forCompare, false);
                        exprHole.setHoleTypeOptionsOfOnlyOne(holeType.forExpression);
                        parentHole.addChild(exprHole);
                    } else if (holeType.equals(holeType.forExpression)) {
                        currentHole.set(holeType.forExpression, false);
                        parentHole.addChild(exprHole);
                    } else {
                        parentHole.deleteHole(holeIndex);
                        parentOfParentHole.addChild(exprHole);
                    }
                } else if (parentHole.getHoleTypeOfOptionsIfOnlyOne() != null && parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(holeType.methodDeclaration)) {
                    if (parentHole.getChildListSize() == 1) {
                        currentHole.set(holeType.type, false);
                    } else if (parentHole.getChildListSize() == 2) {
                        currentHole.set(holeType.parameters, false);
                    } else if (parentHole.getChildListSize() == 3) {
                        currentHole.set(holeType.thrownExceptions, false);
                    } else {
                    }
                    parentHole.addChild(new HoleNode());
                } else if (parentHole.getHoleTypeOfOptionsIfOnlyOne() != null && parentHole.getHoleTypeOfOptionsIfOnlyOne().equals(holeType.ifStmt)) {
                    IfStmt ifStmt = (IfStmt) parent.getLeft();
                    if (parentHole.getNonUndefinedChildListSize() == 1) {
                        Statement thenStmt = ifStmt.getThenStmt();
                        string thenStmtStr = StringHelper.getClassName(thenStmt.getClass().toString());
                        if (thenStmtStr.equals("ReturnStmt")) {
                            BlockStmt blockStmt = new BlockStmt();
                            NodeList<Statement> statements = new NodeList<Statement>();
                            blockStmt.setStatements(statements);
                            ifStmt.setThenStmt(blockStmt);
                            currentHole.set(holeType.thenStatement, false);
                            HoleNode stmtsHole = new HoleNode(holeType.statements, false);
                            currentHole.addChild(stmtsHole);
                            parentHole.addChild(new HoleNode());
                        } else if (thenStmtStr.equals("BlockStmt")) {
                            system.out.println("Shall not go this branch");
                        } else {
                        }
                    } else if (!parentHole.getIthChild(parentHole.getChildListSize() - 2).getHoleType().equals(holeType.elseStatement)) {
                        BlockStmt blockStmt = new BlockStmt();
                    }
                }
        }
    }
}
