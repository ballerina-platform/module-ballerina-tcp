/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.plugins.codeaction.CodeAction;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.stdlib.tcp.compiler.TcpConnectionServiceValidator.TCP_106;

/**
 * Code action to add resource code snippet.
 */
public class AddTcpCodeTemplate implements CodeAction {
    public static final String NODE_LOCATION = "node.location";
    public static final String LS = System.lineSeparator();
    public static final String RESOURCE_TEXT = LS + "\tremote function onConnect(tcp:Caller caller) returns " +
            "tcp:ConnectionService " +
            "{" + LS + "\t\treturn new TcpService();" + LS + "\t}";
    public static final String SERVICE_TEXT = LS + LS + "service class TcpService {" + LS +
            "\t*tcp:ConnectionService;" + LS + LS +
            "\tremote function onBytes(tcp:Caller caller, readonly & byte[] data) " +
            "returns tcp:Error? {"  + LS +
            "\t}" + LS +
            "}";

    @Override
    public List<String> supportedDiagnosticCodes() {
        return List.of(TCP_106);
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext codeActionContext) {
        Diagnostic diagnostic = codeActionContext.diagnostic();
        if (diagnostic.location() == null) {
            return Optional.empty();
        }
        CodeActionArgument locationArg = CodeActionArgument.from(NODE_LOCATION,
                diagnostic.location().lineRange());
        return Optional.of(CodeActionInfo.from("Insert service template", List.of(locationArg)));
    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext codeActionExecutionContext) {
        LineRange lineRange = null;
        for (CodeActionArgument argument : codeActionExecutionContext.arguments()) {
            if (NODE_LOCATION.equals(argument.key())) {
                lineRange = argument.valueAs(LineRange.class);
            }
        }

        if (lineRange == null) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = codeActionExecutionContext.currentDocument().syntaxTree();
        NonTerminalNode node = findNode(syntaxTree, lineRange);
        if (!(node instanceof ServiceDeclarationNode)) {
            return Collections.emptyList();
        }

        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;

        List<TextEdit> textEdits = new ArrayList<>();

        TextRange resourceTextRange = TextRange.from(serviceDeclarationNode.openBraceToken().textRange().endOffset(),
                0);
        TextRange insertWsServiceTextRange = TextRange.from(serviceDeclarationNode.closeBraceToken().textRange()
                .endOffset(), 0);
        textEdits.add(TextEdit.from(resourceTextRange, RESOURCE_TEXT));
        textEdits.add(TextEdit.from(insertWsServiceTextRange, SERVICE_TEXT));
        TextDocumentChange change = TextDocumentChange.from(textEdits.toArray(new TextEdit[0]));
        return Collections.singletonList(new DocumentEdit(codeActionExecutionContext.fileUri(),
                SyntaxTree.from(syntaxTree, change)));
    }

    @Override
    public String name() {
        return "ADD_SERVICE_CODE_SNIPPET";
    }

    public static NonTerminalNode findNode(SyntaxTree syntaxTree, LineRange lineRange) {
        if (lineRange == null) {
            return null;
        }

        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int end = textDocument.textPositionFrom(lineRange.endLine());
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, end - start), true);
    }
}
