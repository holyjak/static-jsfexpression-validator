package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingJsfElResolverAbstractTest;


public class Jsf12ValidatingElResolverTest extends ValidatingJsfElResolverAbstractTest {

    @Override
    protected ValidatingElResolver setUpResolver() {
        return new Jsf12ValidatingElResolver();
    }

}
