/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.tcp.compiler;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.tcp.Constants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.List;
import java.util.Optional;

/**
 * Class to Validate TCP services.
 */
public class TcpServiceValidatorTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private static final String ORG_NAME = "ballerina";

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {
        List<Diagnostic> diagnostics = ctx.semanticModel().diagnostics();
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                return;
            }
        }
        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) ctx.node();

        Optional<Symbol> serviceDeclarationSymbol = ctx.semanticModel().symbol(serviceDeclarationNode);
        TcpServiceValidator tcpServiceValidator;
        if (serviceDeclarationSymbol.isPresent()) {
            List<TypeSymbol> listenerTypes = ((ServiceDeclarationSymbol) serviceDeclarationSymbol.get())
                    .listenerTypes();
            for (TypeSymbol listenerType : listenerTypes) {
                if (isListenerBelongsToTcpModule(listenerType)) {
                    tcpServiceValidator = new TcpServiceValidator(ctx);
                    tcpServiceValidator.validate();
                    return;
                }
            }
        }
    }

    private boolean isTcpModule(ModuleSymbol moduleSymbol) {
        return Utils.equals(moduleSymbol.getName().get(), Constants.TCP)
                && Utils.equals(moduleSymbol.id().orgName(), ORG_NAME);
    }

    private boolean isListenerBelongsToTcpModule(TypeSymbol listenerType) {
        if (listenerType.typeKind() == TypeDescKind.UNION) {
            return ((UnionTypeSymbol) listenerType).memberTypeDescriptors().stream()
                    .filter(typeDescriptor -> typeDescriptor instanceof TypeReferenceTypeSymbol)
                    .map(typeReferenceTypeSymbol -> (TypeReferenceTypeSymbol) typeReferenceTypeSymbol)
                    .anyMatch(typeReferenceTypeSymbol -> isTcpModule(typeReferenceTypeSymbol.getModule().get()));
        }

        if (listenerType.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            return isTcpModule(((TypeReferenceTypeSymbol) listenerType).typeDescriptor().getModule().get());
        }

        return false;
    }
}
