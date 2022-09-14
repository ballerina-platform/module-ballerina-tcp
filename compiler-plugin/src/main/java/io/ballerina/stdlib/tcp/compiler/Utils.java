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

import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

/**
 * Compiler-plugin utility class.
 */
public class Utils {

    public static boolean equals(String actual, String expected) {
        return actual.compareTo(expected) == 0;
    }

    public static boolean hasRemoteKeyword(FunctionDefinitionNode functionDefinitionNode) {
        return functionDefinitionNode.qualifierList().stream()
                .filter(q -> q.kind() == SyntaxKind.REMOTE_KEYWORD).toArray().length == 1;
    }

    public static boolean hasRemoteKeyword(MethodSymbol methodSymbol) {
        return methodSymbol.qualifiers().stream()
                .filter(qualifier -> qualifier == Qualifier.REMOTE).count() == 1;
    }

    public static void reportDiagnostics(SyntaxNodeAnalysisContext ctx, PluginConstants.CompilationErrors error,
                                         Location location, Object... args) {
        String errorMessage = error.getError();
        String diagnosticCode = error.getErrorCode();
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(diagnosticCode, errorMessage, DiagnosticSeverity.ERROR);
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location, args);
        ctx.reportDiagnostic(diagnostic);
    }
}
