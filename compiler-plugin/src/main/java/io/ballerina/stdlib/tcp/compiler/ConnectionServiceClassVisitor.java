package io.ballerina.stdlib.tcp.compiler;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

/**
 * Class to filter a specific ConnectionService.
 */
public class ConnectionServiceClassVisitor extends NodeVisitor {

    private String serviceClassName;
    private ClassDefinitionNode classDefinitionNode;

    public ConnectionServiceClassVisitor(String serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {
        boolean isServiceClass = classDefinitionNode.classTypeQualifiers().stream()
                .anyMatch(token -> token.kind() == SyntaxKind.SERVICE_KEYWORD);
        if (isServiceClass && Utils.equals(classDefinitionNode.className().text(), serviceClassName)) {
            this.classDefinitionNode = classDefinitionNode;
        }
    }

    public ClassDefinitionNode getClassDefinitionNode() {
        return classDefinitionNode;
    }
}
