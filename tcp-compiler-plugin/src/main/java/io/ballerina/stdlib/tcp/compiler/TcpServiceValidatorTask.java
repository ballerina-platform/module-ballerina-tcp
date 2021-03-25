package io.ballerina.stdlib.tcp.compiler;

import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import org.ballerinalang.stdlib.tcp.Constants;

/**
 * Class to Validate TCP services.
 */
public class TcpServiceValidatorTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {
        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) ctx.node();
        SeparatedNodeList<ExpressionNode> expressions = serviceDeclarationNode.expressions();

        String modulePrefix = Constants.TCP;
        ModulePartNode modulePartNode = ctx.syntaxTree().rootNode();
        for (ImportDeclarationNode importDeclaration : modulePartNode.imports()) {
            if (importDeclaration.moduleName().get(0).toString().split(" ")[0].compareTo(Constants.TCP) == 0) {
                if (importDeclaration.prefix().isPresent()) {
                    modulePrefix = importDeclaration.prefix().get().children().get(1).toString();
                }
                break;
            }
        }

        TcpServiceValidator tcpServiceValidator = null;
        for (ExpressionNode expressionNode : expressions) {
            if (expressionNode.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION) {

                TypeDescriptorNode typeDescriptorNode = ((ExplicitNewExpressionNode) expressionNode).typeDescriptor();
                Node moduleIdentifierTokenOfListener = typeDescriptorNode.children().get(0);
                if (moduleIdentifierTokenOfListener.toString().compareTo(modulePrefix) == 0) {
                    tcpServiceValidator = new TcpServiceValidator(ctx);
                }
            }
        }

        if (tcpServiceValidator != null) {
            tcpServiceValidator.validate();
        }
    }
}
