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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.stdlib.tcp.Constants;

import java.util.Optional;

/**
 * Class to Validate TCP ConnectionServices.
 */
public class TcpConnectionServiceValidator {
    private FunctionDefinitionNode onCloseFunctionNode;
    private FunctionDefinitionNode onBytesFunctionNode;
    private FunctionDefinitionNode onErrorFunctionNode;
    private final String modulePrefix;
    private final SyntaxNodeAnalysisContext ctx;

    // Message formats for reporting error diagnostics
    private static final String CODE = "UDP_101";
    public static final String SERVICE_DOES_NOT_CONTAIN_ON_BYTES_FUNCTION
            = "Service does not contain `onBytes` function.";
    public static final String NO_PARAMETER_PROVIDED_FOR_0_FUNCTION_EXPECTS_1_AS_A_PARAMETER
            = "No parameter provided for `{0}`, function expects `{1}` as a parameter.";
    public static final String REMOTE_KEYWORD_EXPECTED_IN_0_FUNCTION_SIGNATURE
            = "`remote` keyword expected in `{0}` function signature.";
    public static final String INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2
            = "Invalid parameter `{0}` provided for `{1}`, function expects `{2}`.";
    public static final String INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION
            = "Invalid parameter `{0}` provided for `{1}` function.";
    public static final String INVALID_RETURN_TYPE_0_FUNCTION_1_RETURN_TYPE_SHOULD_BE_A_SUBTYPE_OF_2
            = "Invalid return type `{0}` provided for function `{1}`, return type should be a subtype of `{2}`";
    public static final String FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE = "Function `{0}` not accepted by the service";
    public static final String PROVIDED_0_PARAMETERS_1_CAN_HAVE_ONLY_2_PARAMETERS
            = "Provided {0} parameters, `{1}` can have only {2} parameters";
    public static final String PROVIDED_0_PARAMETERS_ON_CLOSE_FUNCTION_CANNOT_HAVE_ANY_PARAMETERS
            = "Provided {0} parameters, `onClose` function cannot have any parameters";

    // expected parameters and return types
    public static final String READONLY_INTERSECTION = "readonly & ";
    public static final String CALLER = "Caller";
    public static final String BYTE_ARRAY = "byte[]";
    public static final String ERROR = "Error";
    public static final String OPTIONAL = "?";
    public static final String NILL = "()";

    public TcpConnectionServiceValidator(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext,
                                         String modulePrefixOrModuleName) {
        ctx = syntaxNodeAnalysisContext;
        modulePrefix = modulePrefixOrModuleName;
    }

    public void validate() {
        ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) ctx.node();
        classDefinitionNode.members().stream()
                .filter(child -> child.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION
                        || child.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION).forEach(node -> {
            FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
            String functionName = functionDefinitionNode.functionName().toString();
            if (Utils.hasRemoteKeyword(functionDefinitionNode)
                    && !Utils.equals(functionName, Constants.ON_BYTES)
                    && !Utils.equals(functionName, Constants.ON_ERROR)
                    && !Utils.equals(functionName, Constants.ON_CLOSE)) {
                reportInvalidFunction(functionDefinitionNode);
            } else {
                onBytesFunctionNode = Utils.equals(functionName, Constants.ON_BYTES) ? functionDefinitionNode
                        : onBytesFunctionNode;
                onErrorFunctionNode = Utils.equals(functionName, Constants.ON_ERROR) ? functionDefinitionNode
                        : onErrorFunctionNode;
                onCloseFunctionNode = Utils.equals(functionName, Constants.ON_CLOSE) ? functionDefinitionNode
                        : onCloseFunctionNode;
            }
        });
        checkOnBytesFunctionExistence();
        validateFunctionSignature(onBytesFunctionNode, Constants.ON_BYTES);
        validateFunctionSignature(onErrorFunctionNode, Constants.ON_ERROR);
        validateFunctionSignature(onCloseFunctionNode, Constants.ON_CLOSE);
    }

    private void reportInvalidFunction(FunctionDefinitionNode functionDefinitionNode) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(CODE, FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE,
                DiagnosticSeverity.ERROR);
        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                functionDefinitionNode.location(), functionDefinitionNode.functionName().toString()));
    }

    private void validateFunctionSignature(FunctionDefinitionNode functionDefinitionNode, String functionName) {
        if (functionDefinitionNode != null) {
            hasRemoteKeyword(functionDefinitionNode, functionName);
            SeparatedNodeList<ParameterNode> parameterNodes = functionDefinitionNode.functionSignature().parameters();
            if (!functionName.equals(Constants.ON_CLOSE)
                    && hasNoParameters(parameterNodes, functionDefinitionNode, functionName)) {
                return;
            }
            validateParameter(parameterNodes, functionName);
            validateFunctionReturnTypeDesc(functionDefinitionNode, functionName);
        }
    }

    private void checkOnBytesFunctionExistence() {
        if (onBytesFunctionNode == null) {
            // ConnectionService should contain onBytes method
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(CODE, SERVICE_DOES_NOT_CONTAIN_ON_BYTES_FUNCTION,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    ctx.node().location()));
        }
    }

    private void hasRemoteKeyword(FunctionDefinitionNode functionDefinitionNode, String functionName) {
        boolean hasRemoteKeyword = Utils.hasRemoteKeyword(functionDefinitionNode);
        if (!hasRemoteKeyword) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(CODE,
                    REMOTE_KEYWORD_EXPECTED_IN_0_FUNCTION_SIGNATURE,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    functionDefinitionNode.functionKeyword().location(), functionName));
        }
    }

    private boolean hasNoParameters(SeparatedNodeList<ParameterNode> parameterNodes,
                                    FunctionDefinitionNode functionDefinitionNode, String functionName) {
        if (parameterNodes.isEmpty()) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(CODE,
                    NO_PARAMETER_PROVIDED_FOR_0_FUNCTION_EXPECTS_1_AS_A_PARAMETER, DiagnosticSeverity.ERROR);
            String expectedParameter = functionName.equals(Constants.ON_BYTES) ?
                    READONLY_INTERSECTION + BYTE_ARRAY : modulePrefix + ERROR;
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    functionDefinitionNode.functionSignature().location(), functionName, expectedParameter));
            return true;
        }
        return false;
    }

    private void validateParameter(SeparatedNodeList<ParameterNode> parameterNodes, String functionName) {
        if (hasValidParameterCount(parameterNodes.size(), functionName)) {
            for (ParameterNode parameterNode : parameterNodes) {
                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                Node parameterTypeName = requiredParameterNode.typeName();
                boolean hasCaller = parameterTypeName.toString().contains(modulePrefix + CALLER);
                boolean hasByteArray = parameterTypeName.toString().contains(BYTE_ARRAY);
                DiagnosticInfo diagnosticInfo;

                if (functionName.equals(Constants.ON_BYTES)
                        && ((parameterTypeName.kind() == SyntaxKind.INTERSECTION_TYPE_DESC && !hasByteArray)
                        || (parameterTypeName.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE && !hasCaller))) {
                    diagnosticInfo = new DiagnosticInfo(CODE, INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION,
                            DiagnosticSeverity.ERROR);
                    ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                            requiredParameterNode.location(), requiredParameterNode, functionName));
                } else if (functionName.equals(Constants.ON_ERROR)
                        && !parameterTypeName.toString().contains(modulePrefix + ERROR)) {
                    diagnosticInfo = new DiagnosticInfo(CODE,
                            INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2, DiagnosticSeverity.ERROR);
                    ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                            requiredParameterNode.location(), requiredParameterNode, functionName,
                            modulePrefix + ERROR));
                } else if (parameterTypeName.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE
                        && parameterTypeName.kind() != SyntaxKind.INTERSECTION_TYPE_DESC) {
                    if (functionName.equals(Constants.ON_BYTES) && hasByteArray) {
                        diagnosticInfo = new DiagnosticInfo(CODE,
                                INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2, DiagnosticSeverity.ERROR);
                        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                                requiredParameterNode.location(), requiredParameterNode, functionName,
                                READONLY_INTERSECTION + modulePrefix + BYTE_ARRAY));
                    } else {
                        diagnosticInfo = new DiagnosticInfo(CODE, INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION,
                                DiagnosticSeverity.ERROR);
                        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                                requiredParameterNode.location(), requiredParameterNode, functionName));
                    }
                }
            }
        }
    }

    private boolean hasValidParameterCount(int parameterCount, String functionName) {
        DiagnosticInfo diagnosticInfo;
        if (functionName.equals(Constants.ON_BYTES) && parameterCount > 2) {
            diagnosticInfo = new DiagnosticInfo(CODE, PROVIDED_0_PARAMETERS_1_CAN_HAVE_ONLY_2_PARAMETERS,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    onBytesFunctionNode.location(), parameterCount, functionName, 2));
            return false;
        } else if (functionName.equals(Constants.ON_ERROR) && parameterCount > 1) {
            diagnosticInfo = new DiagnosticInfo(CODE, PROVIDED_0_PARAMETERS_1_CAN_HAVE_ONLY_2_PARAMETERS,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    onErrorFunctionNode.location(), parameterCount, functionName, 1));

            return false;
        } else if (functionName.equals(Constants.ON_CLOSE) && parameterCount > 0) {
            diagnosticInfo = new DiagnosticInfo(CODE,
                    PROVIDED_0_PARAMETERS_ON_CLOSE_FUNCTION_CANNOT_HAVE_ANY_PARAMETERS, DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    onCloseFunctionNode.location(), parameterCount, functionName, 2));
            return false;
        }
        return true;
    }

    private void validateFunctionReturnTypeDesc(FunctionDefinitionNode functionDefinitionNode, String functionName) {
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptorNode = functionDefinitionNode
                .functionSignature().returnTypeDesc();
        if (returnTypeDescriptorNode.isEmpty()) {
            return;
        }

        Node returnTypeDescriptor = returnTypeDescriptorNode.get().type();
        String returnTypeDescriptorType = returnTypeDescriptor.toString().stripTrailing();

        if (functionName.equals(Constants.ON_BYTES) && returnTypeDescriptor.kind() == SyntaxKind.ARRAY_TYPE_DESC
                && Utils.equals(returnTypeDescriptorType, BYTE_ARRAY)) {
            return;
        }

        if (functionName.equals(Constants.ON_BYTES) && returnTypeDescriptor.kind() == SyntaxKind.OPTIONAL_TYPE_DESC
                && (Utils.equals(returnTypeDescriptorType, modulePrefix + ERROR + OPTIONAL)
                || Utils.equals(returnTypeDescriptorType, BYTE_ARRAY + OPTIONAL))) {
            return;
        }


        if ((functionName.equals(Constants.ON_ERROR) || functionName.equals(Constants.ON_CLOSE))
                && returnTypeDescriptor.kind() == SyntaxKind.OPTIONAL_TYPE_DESC
                && Utils.equals(returnTypeDescriptorType, modulePrefix + ERROR + OPTIONAL)) {
            return;
        }

        if (returnTypeDescriptor.kind() == SyntaxKind.NIL_TYPE_DESC) {
            return;
        }

        boolean hasInvalidUnionTypeDesc = false;
        boolean isUnionTypeDesc = false;
        if (functionName.equals(Constants.ON_BYTES) && returnTypeDescriptor.kind() == SyntaxKind.UNION_TYPE_DESC) {
            isUnionTypeDesc = true;
            UnionTypeDescriptorNode unionTypeDescriptorNode = (UnionTypeDescriptorNode) returnTypeDescriptor;
            for (Node descriptor : unionTypeDescriptorNode.children()) {
                String descriptorType = descriptor.toString().stripTrailing();
                if (descriptor.kind() == SyntaxKind.PIPE_TOKEN) {
                    continue;
                } else if (descriptor.kind() == SyntaxKind.ARRAY_TYPE_DESC
                        && Utils.equals(descriptorType, BYTE_ARRAY)) {
                    continue;
                } else if (descriptor.kind() == SyntaxKind.OPTIONAL_TYPE_DESC
                        && (Utils.equals(descriptorType, modulePrefix + ERROR + OPTIONAL)
                        || Utils.equals(descriptorType, BYTE_ARRAY + OPTIONAL))) {
                    continue;
                } else {
                    hasInvalidUnionTypeDesc = true;
                }
            }
        }

        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(CODE,
                INVALID_RETURN_TYPE_0_FUNCTION_1_RETURN_TYPE_SHOULD_BE_A_SUBTYPE_OF_2, DiagnosticSeverity.ERROR);
        if (functionName.equals(Constants.ON_BYTES) && (hasInvalidUnionTypeDesc || !isUnionTypeDesc)) {
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    returnTypeDescriptor.location(), returnTypeDescriptor.toString(), functionName,
                    BYTE_ARRAY + " | " + modulePrefix + ERROR + OPTIONAL));
        } else if (!isUnionTypeDesc) {
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    returnTypeDescriptor.location(), returnTypeDescriptor.toString(), functionName,
                    modulePrefix + ERROR + " | " + NILL));
        }
    }
}
