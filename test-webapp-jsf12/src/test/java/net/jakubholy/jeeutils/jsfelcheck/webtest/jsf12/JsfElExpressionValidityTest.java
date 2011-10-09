package net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12;

import net.jakubholy.jeeutils.jsfelcheck.CollectedValidationResults;
import net.jakubholy.jeeutils.jsfelcheck.JsfStaticAnalyzer;
import net.jakubholy.jeeutils.jsfelcheck.webtest.jsf12.testbean.MyCollectionBean;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static net.jakubholy.jeeutils.jsfelcheck.config.LocalVariableConfiguration.declareLocalVariable;
import static org.junit.Assert.assertEquals;

/**
 * Check EL expressions that are supposed to be validated successfully.
 */
public class JsfElExpressionValidityTest {

    @Test
    public void verify_all_el_expressions_valid() throws Exception {

        JsfStaticAnalyzer jsfStaticAnalyzer = createConfiguredAnalyzer();

        jsfStaticAnalyzer.withLocalVariablesConfiguration(
                declareLocalVariable("shop.books", Book.class)
                    .and("myCollectionBean.list", MyCollectionBean.ValueHolder.class)
                    .withCustomDataTableTagAlias("t:dataTable"))
                .withPropertyTypeOverride("myCollectionBean.list.*", MyCollectionBean.ValueHolder.class)
                .withExtraVariable("iAmExtraVariable", new Object())
                ;

        CollectedValidationResults results = jsfStaticAnalyzer.validateElExpressions("src/main/webapp//tests/valid_el");

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
