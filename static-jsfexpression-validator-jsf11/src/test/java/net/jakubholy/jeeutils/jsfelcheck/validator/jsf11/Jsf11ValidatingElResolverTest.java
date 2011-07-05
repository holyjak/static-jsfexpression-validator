package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11;

import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolverAbstractTest;


public class Jsf11ValidatingElResolverTest extends ValidatingJsfElResolverAbstractTest {

    @Override
    protected ValidatingElResolver setUpResolver() {
        return new Jsf11ValidatingElResolver();
    }

}
