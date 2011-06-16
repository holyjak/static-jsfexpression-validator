package net.jakubholy.jeeutils.jsfelcheck.validator;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;


public class MockingPropertyResolverTest {

    private MockingPropertyResolver resolver;

    @Before
    public void setUp() {
        resolver = new MockingPropertyResolver();
        resolver.handleNewVariableEncountered("bean");
    }

    @Test
    public void should_keep_current_type_if_no_override() throws Exception {
        assertSame(String.class
                , resolver.determineFinalType("property", String.class));
        assertSame(URL.class
                , resolver.determineFinalType("property", URL.class));
    }

    @Test
    public void should_respect_component_type_override() throws Exception {
        resolver.definePropertyTypeOverride("bean.*", Integer.class);

        assertSame(Integer.class
                , resolver.determineFinalType("mapKey", String.class));
    }

    @Test
    public void should_respect_property_type_override() throws Exception {
        resolver.definePropertyTypeOverride("bean.property", URL.class);

        assertSame(URL.class
                , resolver.determineFinalType("property", Math.class));
    }

    @Test
    public void should_prioritize_property_over_component_type_override() throws Exception {
        resolver.definePropertyTypeOverride("bean.property", URL.class);
        resolver.definePropertyTypeOverride("bean.*", Integer.class);


        assertSame(Integer.class
                , resolver.determineFinalType("nonOverridenProperty", String.class));
        assertSame(URL.class
                , resolver.determineFinalType("property", String.class));
    }

}
