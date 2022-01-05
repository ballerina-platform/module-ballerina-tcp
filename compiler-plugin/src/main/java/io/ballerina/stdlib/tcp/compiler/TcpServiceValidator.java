/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.tcp.compiler;

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.tcp.Constants;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import static io.ballerina.stdlib.tcp.compiler.TcpConnectionServiceValidator.TCP_106;
import static io.ballerina.stdlib.tcp.compiler.TcpConnectionServiceValidator.TEMPLATE_CODE_GENERATION_HINT;

/**
 * Class to Validate TCP services.
 */
public class TcpServiceValidator {
    private SyntaxNodeAnalysisContext ctx;

    public static final String TCP_103 = "TCP_103";
    public static final String FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE = "Function `{0}` not accepted by the service";

    public TcpServiceValidator(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        ctx = syntaxNodeAnalysisContext;
    }

    public void validate() {
        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) ctx.node();
        if (serviceDeclarationNode.members().isEmpty()) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(TCP_106, TEMPLATE_CODE_GENERATION_HINT,
                    DiagnosticSeverity.INTERNAL);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, serviceDeclarationNode.location()));
        }
        serviceDeclarationNode.members().stream()
                .filter(child -> child.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION
                        || child.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION).forEach(node -> {
            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
            String functionName = functionDefinitionNode.functionName().toString();
            if (Utils.hasRemoteKeyword(functionDefinitionNode) && !Utils.equals(functionName, Constants.ON_CONNECT)) {
                reportInvalidFunction(functionDefinitionNode);
            } else if (Utils.equals(functionName, Constants.ON_CONNECT)) {

                ReturnStatementNodeVisitor returnStatementNodeVisitor = new ReturnStatementNodeVisitor();
                functionDefinitionNode.accept(returnStatementNodeVisitor);

                for (ReturnStatementNode returnStatementNode : returnStatementNodeVisitor.getReturnStatementNodes()) {
                    ExpressionNode expressionNode = returnStatementNode.expression().get();

                    if (expressionNode instanceof ExplicitNewExpressionNode) { // handle return new HelloService();
                        TypeSymbol symbol = ctx.semanticModel().type(expressionNode).get();
                        if (symbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) symbol;
                            ClassSymbol classSymbol = (ClassSymbol) typeReferenceTypeSymbol.typeDescriptor();
                            TcpConnectionServiceValidator tcpConnectionServiceValidator =
                                    new TcpConnectionServiceValidator(ctx, classSymbol);
                            tcpConnectionServiceValidator.validate();
                        }
                    }
                }
            }
        });
    }

    private void reportInvalidFunction(FunctionDefinitionNode functionDefinitionNode) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(TCP_103, FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE,
                DiagnosticSeverity.ERROR);
        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                functionDefinitionNode.location(), functionDefinitionNode.functionName().toString()));
    }
}
