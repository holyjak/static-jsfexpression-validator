package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer;

import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.testbean.MyCollectionBean;
import org.junit.Test;

public class JsfElExpressionValidityTest {

    @Test
    public void verify_all_el_expressions_valid() throws Exception {

        Map<String, Class<?>> extraVariables = Collections.emptyMap();
        Map<String, Class<?>> localVariableTypes = new HashMap<String, Class<?>>();
        Map<String, Class<?>> propertyTypeOverrides = Collections.emptyMap();

        JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();

        // Local variables continued
        localVariableTypes.put("shop.books", Book.class);
        localVariableTypes.put("myCollectionBean.list", MyCollectionBean.ValueHolder.class);
        jsfStaticAnalyzer.registerDataTableTag("t:dataTable");

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
