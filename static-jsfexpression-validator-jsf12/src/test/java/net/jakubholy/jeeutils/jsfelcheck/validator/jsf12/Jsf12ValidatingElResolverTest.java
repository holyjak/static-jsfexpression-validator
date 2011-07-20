package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import org.junit.Test;

import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolverAbstractTest;


public class Jsf12ValidatingElResolverTest extends ValidatingJsfElResolverAbstractTest {

    @Override
    protected ValidatingElResolver setUpResolver() {
        return new Jsf12ValidatingElResolver();
    }

    /**
     * JSF EL up to 1.1 were marked with #{..}, since the introduction of UEL in 1.2 they use
     * ${..} (used for JSP EL only before that).
     */
    @Test
    public void should_recognize_both_jsf_and_uel_expression_markers() throws Exception {
        elResolver.declareVariable("myStringBean", "Hello!");

        assertResultValue("#{myStringBean}", "Hello!");
        assertResultValue("${myStringBean}", "Hello!");

        // This particular implementation doesn't fail when we pass in a literal string, which is little strange
        assertResultValue("this is a literal string, not an EL expression"
                , "this is a literal string, not an EL expression");
    }

}
