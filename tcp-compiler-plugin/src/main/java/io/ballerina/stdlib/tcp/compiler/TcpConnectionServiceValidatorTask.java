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

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import org.ballerinalang.stdlib.tcp.Constants;

/**
 * Class to validate TCP ConnectionService.
 */
public class TcpConnectionServiceValidatorTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {

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
        // Todo: filter only the tcp:ConnectionService classes
        TcpConnectionServiceValidator serviceValidator = new TcpConnectionServiceValidator(ctx,
                modulePrefix + SyntaxKind.COLON_TOKEN.stringValue());
        serviceValidator.validate();
    }
}
