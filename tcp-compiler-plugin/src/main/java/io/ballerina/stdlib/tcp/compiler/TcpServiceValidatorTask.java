package io.ballerina.stdlib.tcp.compiler;

import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.io.PrintStream;

/**
 * Class to Validate TCP services.
 */
public class TcpServiceValidatorTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        // Test implementation
        PrintStream console = System.out;
        console.println("Engage");
    }
}
