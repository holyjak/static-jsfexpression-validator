package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer;

import org.junit.Test;

public class JsfElExpressionValidityTest {

    @Test
    public void verify_all_el_expressions_valid() throws Exception {

        Map<String, Class<?>> extraVariables = Collections.emptyMap();
        Map<String, Class<?>> localVariableTypes = Collections.emptyMap();
        Map<String, Class<?>> propertyTypeOverrides = Collections.emptyMap();

        JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();

        CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions(
                "src/main/webapp"
                , localVariableTypes
                , extraVariables
                , propertyTypeOverrides);

        assertEquals("There shall be no invalid JSF EL expressions; check System.err/.out for details. FAILURE "
                + results.failures()
                , 0, results.failures().size());

    }

    private JsfStaticAnalyzer createConfiguredAnalyzer() {
        JsfStaticAnalyzer jsfStaticAnalyzer = new JsfStaticAnalyzer();
        jsfStaticAnalyzer.setPrintCorrectExpressions(false);
        jsfStaticAnalyzer.setFacesConfigFiles(Collections.singleton(new File(
                "src/main/webapp/WEB-INF/faces-config.xml")));
        return jsfStaticAnalyzer;
    }

}
