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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class includes tests for Ballerina TCP compiler plugin.
 */
public class CompilerPluginTest {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources", "ballerina_sources")
            .toAbsolutePath();
    private static final Path DISTRIBUTION_PATH = Paths.get("build", "target", "ballerina-distribution")
            .toAbsolutePath();

    @Test
    public void testServiceWithUnacceptedFunction() {
        Package currentPackage = loadPackage("sample_package_1");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 2);
        for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
            Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                    TcpServiceValidator.FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE);
            Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpServiceValidator.TCP_103);
        }
    }

    @Test
    public void testServiceWithoutOnBytes() {
        Package currentPackage = loadPackage("sample_package_2");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 1);
        Diagnostic diagnostic = (Diagnostic) diagnosticResult.diagnostics().toArray()[0];
        Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                TcpConnectionServiceValidator.SERVICE_DOES_NOT_CONTAIN_ON_BYTES_FUNCTION);
        Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_102);
    }

    @Test
    public void testRemoteFunctionsWithoutRemoteKeyword() {
        Package currentPackage = loadPackage("sample_package_3");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 3);
        for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
            Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                    TcpConnectionServiceValidator.REMOTE_KEYWORD_EXPECTED_IN_0_FUNCTION_SIGNATURE);
            Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_101);
        }
    }

    @Test
    public void testOnBytesOnErrorFunctionsWithoutParameters() {
        Package currentPackage = loadPackage("sample_package_4");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 2);
        for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
            Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                    TcpConnectionServiceValidator.NO_PARAMETER_PROVIDED_FOR_0_FUNCTION_EXPECTS_1_AS_A_PARAMETER);
            Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_104);
        }
    }

    @Test
    public void testOnBytesFunctionWithInvalidParameter() {
        Package currentPackage = loadPackage("sample_package_5");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 1);
        Diagnostic diagnostic = (Diagnostic) diagnosticResult.diagnostics().toArray()[0];
        Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                TcpConnectionServiceValidator.INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION);
        Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_104);
    }

    @Test
    public void testOnErrorFunctionWithInvalidParameter() {
        Package currentPackage = loadPackage("sample_package_6");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 1);
        Diagnostic diagnostic = (Diagnostic) diagnosticResult.diagnostics().toArray()[0];
        Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                TcpConnectionServiceValidator.INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2);
    }

    @Test(description = "test onBytes function with byte[] parameter, without readonly intersection")
    public void testOnBytesWithoutReadonlyParameters() {
        Package currentPackage = loadPackage("sample_package_7");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 1);
        Diagnostic diagnostic = (Diagnostic) diagnosticResult.diagnostics().toArray()[0];
        Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                TcpConnectionServiceValidator.INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2);
        Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_104);
    }

    @Test
    public void testValidService() {
        Package currentPackage = loadPackage("sample_package_8");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 0);

        currentPackage = loadPackage("sample_package_15");
        compilation = currentPackage.getCompilation();
        diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 0);
    }

    @Test
    public void testWithAliasModuleNamePrefix() {
        Package currentPackage = loadPackage("sample_package_9");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 1);
        Diagnostic diagnostic = (Diagnostic) diagnosticResult.diagnostics().toArray()[0];
        Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                TcpConnectionServiceValidator.INVALID_PARAMETER_0_PROVIDED_FOR_1_FUNCTION_EXPECTS_2);
        Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_104);
    }

   @Test
   public void testWithOtherModuleServices() {
        Package currentPackage = loadPackage("sample_package_10");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 0);
   }

    @Test
    public void testConnectionServiceWithInvalidReturnType() {
        Package currentPackage = loadPackage("sample_package_11");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 3);
        for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
            Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                    TcpConnectionServiceValidator
                            .INVALID_RETURN_TYPE_0_FUNCTION_1_RETURN_TYPE_SHOULD_BE_A_SUBTYPE_OF_2);
            Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_105);
        }
    }

    @Test
    public void testWithUnSupportedFunctionNames() {
        Package currentPackage = loadPackage("sample_package_12");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 2);
        for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
            Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                    TcpConnectionServiceValidator.FUNCTION_0_NOT_ACCEPTED_BY_THE_SERVICE);
            Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_103);
        }
    }

    @Test
    public void testConnectionServiceWithMoreThanExpectedParameterCount() {
        Package currentPackage = loadPackage("sample_package_13");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 2);
        for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
            Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                    TcpConnectionServiceValidator.PROVIDED_0_PARAMETERS_1_CAN_HAVE_ONLY_2_PARAMETERS);
            Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_104);
        }

        currentPackage = loadPackage("sample_package_14");
        compilation = currentPackage.getCompilation();
        diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 1);
        Diagnostic diagnostic = (Diagnostic) diagnosticResult.diagnostics().toArray()[0];
        Assert.assertEquals(diagnostic.diagnosticInfo().messageFormat(),
                TcpConnectionServiceValidator.PROVIDED_0_PARAMETERS_ON_CLOSE_FUNCTION_CANNOT_HAVE_ANY_PARAMETERS);
        Assert.assertEquals(diagnostic.diagnosticInfo().code(), TcpConnectionServiceValidator.TCP_104);
    }

    @Test
    public void testWithoutServiceClass() {
        Package currentPackage = loadPackage("sample_package_16");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnostics().size(), 1);
    }

    private Package loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage();
    }

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(DISTRIBUTION_PATH).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }
}
