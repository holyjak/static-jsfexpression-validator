package net.jakubholy.jeeutils.jsfelcheck.validator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import javax.faces.context.FacesContext;

import net.jakubholy.jeeutils.jsfelcheck.validator.PredefinedVariableResolver.NewVariableEncounteredListener;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.VariableNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PredefinedVariableResolverTest {

    private PredefinedVariableResolver resolver;
    @Mock FacesContext fc;
    @Mock NewVariableEncounteredListener listener;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        resolver = new PredefinedVariableResolver(listener);
    }

    @Test(expected=VariableNotFoundException.class)
    public void should_throw_exception_for_unknown_variable() throws Exception {
        resolver.resolveVariable(fc, "unknown");
    }

    @Test
    public void should_return_predefined_variable() throws Exception {
        resolver.declareVariable("var1", "Var1_Value");
        resolver.declareVariable("bean2", 222);

        assertEquals("Var1_Value", resolver.resolveVariable(fc, "var1"));
        assertEquals(222, resolver.resolveVariable(fc, "bean2"));
    }

    @Test
    public void should_fall_back_to_UnknownVariableResolver_if_variable_unknown() throws Exception {

        resolver.setUnknownVariableResolver(new ElVariableResolver() {
            @Override public Class<?> resolveVariable(String name) {
                return List.class;
            }
        });

        Object result = resolver.resolveVariable(fc, "unknown");

        assertNotNull("Should be resolved by the UnknownVariableResolver", result);
        assertThat(result, is(instanceOf(List.class)));
    }

    @Test
    public void should_invoke_its_listener_when_variable_known() throws Exception {
        resolver.declareVariable("newVariableName", "value");
        resolver.resolveVariable(fc, "newVariableName");
        verify(listener).handleNewVariableEncountered("newVariableName");
    }

    @Test
    public void should_invoke_its_listener_when_variable_resolved_by_its_unknownVariableResolver() throws Exception {
        resolver.setUnknownVariableResolver(new ElVariableResolver() {
            @Override public Class<?> resolveVariable(String name) {
                return List.class;
            }
        });
        resolver.resolveVariable(fc, "variableDelegatedToUVR");
        verify(listener).handleNewVariableEncountered("variableDelegatedToUVR");
    }

    @Test
    public void should_not_invoke_its_listener_when_unknown_variable_encountered() throws Exception {
        try {
            resolver.resolveVariable(fc, "unknownVariable");
        } catch (VariableNotFoundException e) {}
        verifyZeroInteractions(listener);
    }

}
