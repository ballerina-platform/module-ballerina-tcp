package io.ballerina.stdlib.tcp.compiler;

import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to collect return statements.
 */
public class ReturnStatementNodeVisitor extends NodeVisitor {

    private List<ReturnStatementNode> returnStatementNodes = new ArrayList<>();

    @Override
    public void visit(ReturnStatementNode returnStatementNode) {
        returnStatementNodes.add(returnStatementNode);
    }

    public List<ReturnStatementNode> getReturnStatementNodes() {
        return returnStatementNodes;
    }
}
