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
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.tcp.Constants;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.List;
import java.util.Optional;

/**
 * Class to Validate TCP ConnectionServices.
 */
public class TcpConnectionServiceValidator {

    private MethodSymbol onCloseFunctionSymbol;
    private MethodSymbol onBytesFunctionSymbol;
    private MethodSymbol onErrorFunctionSymbol;
    private final ClassSymbol classSymbol;
    private static final String modulePrefix = "ballerina/tcp" + SyntaxKind.COLON_TOKEN.stringValue();
    private final SyntaxNodeAnalysisContext ctx;

    // Error codes for reporting error diagnostics
    public static final String TCP_101 = "TCP_101";
    public static final String TCP_102 = "TCP_102";
    public static final String TCP_103 = "TCP_103";
    public static final String TCP_104 = "TCP_104";
    public static final String TCP_105 = "TCP_105";
    public static final String TCP_106 = "TCP_106";

    // Message formats for reporting error diagnostics
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
    public static final String TEMPLATE_CODE_GENERATION_HINT
            = "Template generation for empty service";

    // expected parameters and return types
    public static final String READONLY_INTERSECTION = "readonly & ";
    public static final String CALLER = "Caller";
    public static final String BYTE_ARRAY = "byte[]";
    public static final String ERROR = "Error";
    public static final String OPTIONAL = "?";
    public static final String NIL = "()";

    public TcpConnectionServiceValidator(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext, ClassSymbol classSymbol) {
        ctx = syntaxNodeAnalysisContext;
        this.classSymbol = classSymbol;
    }

    public void validate() {
        classSymbol.methods().values().stream()
                .forEach(methodSymbol -> filterRemoteMethods(methodSymbol));
        checkOnBytesFunctionExistence();
        validateFunctionSignature(onBytesFunctionSymbol, Constants.ON_BYTES);
        validateFunctionSignature(onErrorFunctionSymbol, Constants.ON_ERROR);
        validateFunctionSignature(onCloseFunctionSymbol, Constants.ON_CLOSE);
    }

    private void filterRemoteMethods(MethodSymbol methodSymbol) {
        String functionName = methodSymbol.getName().get();
        if (Utils.hasRemoteKeyword(methodSymbol)
                && !Utils.equals(functionName, Constants.ON_BYTES)
                && !Utils.equals(functionName, Constants.ON_ERROR)
                && !Utils.equals(functionName, Constants.ON_CLOSE)) {
            reportInvalidFunction(methodSymbol);
        } else {
            onBytesFunctionSymbol = Utils.equals(functionName, Constants.ON_BYTES) ? methodSymbol
                    : onBytesFunctionSymbol;
            onErrorFunctionSymbol = Utils.equals(functionName, Constants.ON_ERROR) ? methodSymbol
                    : onErrorFunctionSymbol;
            onCloseFunctionSymbol = Utils.equals(functionName, Constants.ON_CLOSE) ? methodSymbol
                    : onCloseFunctionSymbol;
        }
    }

    private void reportInvalidFunction(MethodSymbol methodSymbol) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(TCP_103, FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE,
                DiagnosticSeverity.ERROR);
        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                methodSymbol.getLocation().get(), methodSymbol.getName().get()));
    }

    private void validateFunctionSignature(MethodSymbol methodSymbol, String functionName) {
        if (methodSymbol != null) {
            hasRemoteKeyword(methodSymbol, functionName);
            List<ParameterSymbol> parameterSymbols = methodSymbol.typeDescriptor().parameters();
            if (!functionName.equals(Constants.ON_CLOSE)
                    && hasNoParameters(parameterSymbols, methodSymbol, functionName)) {
                return;
            }
            validateParameter(parameterSymbols, functionName);
            validateFunctionReturnTypeDesc(methodSymbol, functionName);
        }
    }

    private void checkOnBytesFunctionExistence() {
        if (onBytesFunctionSymbol == null) {
            // ConnectionService should contain onBytes method
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(TCP_102, SERVICE_DOES_NOT_CONTAIN_ON_BYTES_FUNCTION,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    ctx.node().location()));
        }
    }

    private void hasRemoteKeyword(MethodSymbol methodSymbol, String functionName) {
        boolean hasRemoteKeyword = Utils.hasRemoteKeyword(methodSymbol);
        if (!hasRemoteKeyword) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(TCP_101,
                    REMOTE_KEYWORD_EXPECTED_IN_0_FUNCTION_SIGNATURE,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    methodSymbol.getLocation().get(), functionName));
        }
    }

    private boolean hasNoParameters(List<ParameterSymbol> parameterSymbols,
                                    MethodSymbol methodSymbol, String functionName) {
        if (parameterSymbols.isEmpty()) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(TCP_104,
                    NO_PARAMETER_PROVIDED_FOR_0_FUNCTION_EXPECTS_1_AS_A_PARAMETER, DiagnosticSeverity.ERROR);
            String expectedParameter = functionName.equals(Constants.ON_BYTES) ?
                    READONLY_INTERSECTION + BYTE_ARRAY : modulePrefix + ERROR;
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    methodSymbol.getLocation().get(), functionName, expectedParameter));
            return true;
        }
        return false;
    }

    private void validateParameter(List<ParameterSymbol> parameterSymbols, String functionName) {
        if (hasValidParameterCount(parameterSymbols.size(), functionName)) {
            for (ParameterSymbol parameterSymbol : parameterSymbols) {
                TypeSymbol typeSymbol = parameterSymbol.typeDescriptor();
                String signature = typeSymbol.signature();
                boolean hasCaller = signature.startsWith(modulePrefix)
                        && signature.endsWith(SyntaxKind.COLON_TOKEN.stringValue() + CALLER);
                boolean hasError = signature.startsWith(modulePrefix)
                        && signature.endsWith(SyntaxKind.COLON_TOKEN.stringValue() + ERROR);
                boolean hasByteArray = signature.contains(BYTE_ARRAY);
                DiagnosticInfo diagnosticInfo;

                if (functionName.equals(Constants.ON_BYTES)
                        && ((typeSymbol.typeKind() == TypeDescKind.INTERSECTION && !hasByteArray)
                        || (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE && !hasCaller))) {
                    diagnosticInfo = new DiagnosticInfo(TCP_104, INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION,
                            DiagnosticSeverity.ERROR);
                    ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                            parameterSymbol.getLocation().get(), parameterSymbol.signature(), functionName));
                } else if (functionName.equals(Constants.ON_ERROR)
                        && (typeSymbol.typeKind() != TypeDescKind.TYPE_REFERENCE || !hasError)) {
                    diagnosticInfo = new DiagnosticInfo(TCP_104,
                            INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2, DiagnosticSeverity.ERROR);
                    ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                            parameterSymbol.getLocation().get(), parameterSymbol.signature(), functionName,
                            modulePrefix + ERROR));
                } else if (typeSymbol.typeKind() != TypeDescKind.TYPE_REFERENCE
                        && typeSymbol.typeKind() != TypeDescKind.INTERSECTION
                        && typeSymbol.typeKind() != TypeDescKind.ERROR) {
                    if (functionName.equals(Constants.ON_BYTES) && hasByteArray) {
                        diagnosticInfo = new DiagnosticInfo(TCP_104,
                                INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2, DiagnosticSeverity.ERROR);
                        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                                parameterSymbol.getLocation().get(), parameterSymbol.signature(), functionName,
                                READONLY_INTERSECTION + BYTE_ARRAY));
                    } else {
                        diagnosticInfo = new DiagnosticInfo(TCP_104, INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION,
                                DiagnosticSeverity.ERROR);
                        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                                parameterSymbol.getLocation().get(), parameterSymbol.signature(), functionName));
                    }
                }
            }
        }
    }

    private boolean hasValidParameterCount(int parameterCount, String functionName) {
        DiagnosticInfo diagnosticInfo;
        if (functionName.equals(Constants.ON_BYTES) && parameterCount > 2) {
            diagnosticInfo = new DiagnosticInfo(TCP_104, PROVIDED_0_PARAMETERS_1_CAN_HAVE_ONLY_2_PARAMETERS,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    onBytesFunctionSymbol.getLocation().get(), parameterCount, functionName, 2));
            return false;
        } else if (functionName.equals(Constants.ON_ERROR) && parameterCount > 1) {
            diagnosticInfo = new DiagnosticInfo(TCP_104, PROVIDED_0_PARAMETERS_1_CAN_HAVE_ONLY_2_PARAMETERS,
                    DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    onErrorFunctionSymbol.getLocation().get(), parameterCount, functionName, 1));

            return false;
        } else if (functionName.equals(Constants.ON_CLOSE) && parameterCount > 0) {
            diagnosticInfo = new DiagnosticInfo(TCP_104,
                    PROVIDED_0_PARAMETERS_ON_CLOSE_FUNCTION_CANNOT_HAVE_ANY_PARAMETERS, DiagnosticSeverity.ERROR);
            ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    onCloseFunctionSymbol.getLocation().get(), parameterCount, functionName, 2));
            return false;
        }
        return true;
    }

    private void validateFunctionReturnTypeDesc(MethodSymbol methodSymbol, String functionName) {
        Optional<TypeSymbol> typeSymbol = methodSymbol.typeDescriptor().returnTypeDescriptor();
        if (typeSymbol.isEmpty()) {
            return;
        }

        TypeSymbol returnTypeSymbol = typeSymbol.get();
        if (functionName.equals(Constants.ON_BYTES) && returnTypeSymbol.typeKind() == TypeDescKind.ARRAY
                && Utils.equals(returnTypeSymbol.signature(), BYTE_ARRAY)) {
            return;
        }

        if (returnTypeSymbol.typeKind() == TypeDescKind.NIL) {
            return;
        }

        boolean hasInvalidUnionTypeDesc = false;
        boolean isUnionTypeDesc = false;
        boolean isOptionalError = false;
        if ((functionName.equals(Constants.ON_ERROR) || functionName.equals(Constants.ON_CLOSE))
                && returnTypeSymbol.typeKind() == TypeDescKind.UNION) {
            isUnionTypeDesc = true;
            for (TypeSymbol symbol : ((UnionTypeSymbol) typeSymbol.get()).memberTypeDescriptors()) {
                if (symbol.typeKind() == TypeDescKind.TYPE_REFERENCE
                        && symbol.signature().startsWith(modulePrefix)
                        && symbol.signature().endsWith(SyntaxKind.COLON_TOKEN.stringValue() + ERROR)) {
                    continue;
                } else if (symbol.typeKind() == TypeDescKind.NIL) {
                    isOptionalError = true;
                    continue;
                } else {
                    hasInvalidUnionTypeDesc = true;
                    break;
                }
            }
            hasInvalidUnionTypeDesc = !isOptionalError || hasInvalidUnionTypeDesc;
        } else if (functionName.equals(Constants.ON_BYTES) && returnTypeSymbol.typeKind() == TypeDescKind.UNION) {
            isUnionTypeDesc = true;
            for (TypeSymbol symbol : ((UnionTypeSymbol) typeSymbol.get()).memberTypeDescriptors()) {
                if (symbol.typeKind() == TypeDescKind.ARRAY && Utils.equals(symbol.signature(), BYTE_ARRAY)) {
                    continue;
                } else if (symbol.typeKind() == TypeDescKind.TYPE_REFERENCE
                        && symbol.signature().startsWith(modulePrefix)
                        && symbol.signature().endsWith(SyntaxKind.COLON_TOKEN.stringValue() + ERROR)) {
                    continue;
                } else if (symbol.typeKind() == TypeDescKind.NIL) {
                    continue;
                } else {
                    hasInvalidUnionTypeDesc = true;
                    break;
                }
            }
        }

        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(TCP_105,
                INVALID_RETURN_TYPE_0_FUNCTION_1_RETURN_TYPE_SHOULD_BE_A_SUBTYPE_OF_2, DiagnosticSeverity.ERROR);
        Location returnTypeSymbolLocation = returnTypeSymbol.getLocation().isPresent() ?
                returnTypeSymbol.getLocation().get() : methodSymbol.getLocation().get();
        if ((hasInvalidUnionTypeDesc || !isUnionTypeDesc)) {
            if (functionName.equals(Constants.ON_BYTES)) {
                ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                        returnTypeSymbolLocation, returnTypeSymbol.signature(), functionName,
                        BYTE_ARRAY + "|" + modulePrefix + ERROR + OPTIONAL));
            } else {
                ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                        returnTypeSymbolLocation, returnTypeSymbol.signature(), functionName,
                        modulePrefix + ERROR + "|" + NIL));
            }
        }
    }
}
