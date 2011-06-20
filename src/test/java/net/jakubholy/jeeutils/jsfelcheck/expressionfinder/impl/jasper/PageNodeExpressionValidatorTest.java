package net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.ContextVariableRegistry;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PageNodeExpressionValidatorTest {

    private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

    @SuppressWarnings("serial")
    private static class Attributes extends Hashtable<String, String> {

        public static Attributes with(String attributeName, String value) {
            Attributes attributes = new Attributes();
            attributes.put(attributeName, value);
            return attributes;
        }

        public Attributes and(String attributeName, String value) {
            put(attributeName, value);
            return this;
        }

    }

    @Mock private JsfElValidator expressionValidator;
    @Mock private ContextVariableRegistry contextVarRegistry;
    private PageNodeExpressionValidator nodeValidator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        nodeValidator = new PageNodeExpressionValidator(expressionValidator);
    }

    @Test
    public void should_return_empty_map_if_no_jsf_expression_attributes() throws Exception {

        AttributesValidationResult resolvedExpressions = nodeValidator.validateJsfExpressions(EMPTY_ATTRIBUTES);
        assertNotNull(resolvedExpressions);
    }

    @Test
    public void should_report_all_invalid_expressions() throws Exception {
        Attributes attributes = Attributes
            .with("el1", "#{valid1}")
            .and("el2", "#{2nd is a bad one}");

        ValidationResult r1 = new SuccessfulValidationResult(1);
        ValidationResult r2 = new FailedValidationResult(null);

        when(expressionValidator.validateValueElExpression(eq("#{valid1}")))
            .thenReturn(r1);
        when(expressionValidator.validateValueElExpression(eq("#{2nd is a bad one}")))
            .thenReturn(r2);

        AttributesValidationResult results = nodeValidator.validateJsfExpressions(attributes);

        assertTrue(results.jsfExpressionsFound());
        assertEquals(r1, results.get("el1"));
        assertEquals(r2, results.get("el2"));
    }

    @Test
    public void should_ignore_attributes_without_jsfexpressions() throws Exception {
        AttributesValidationResult results = nodeValidator.validateJsfExpressions(
                Attributes.with("id", "justATextValue"));
        assertFalse(results.jsfExpressionsFound());
    }

    @Test
    public void should_recognize_action_and_actionListener_as_method_bindings() throws Exception {

        Attributes attributes = Attributes
            .with("action", "#{bean.method}")
            .and("actionListener", "#{obj2.listen}");

        when(expressionValidator.validateMethodElExpression(anyString()))
            .thenReturn(new AttributesValidationResult());

        nodeValidator.validateJsfExpressions(attributes);

        verify(expressionValidator, never()).validateValueElExpression(anyString());
        verify(expressionValidator, times(2)).validateMethodElExpression(anyString());
    }

    @Test
    public void should_default_to_value_binding() throws Exception {

        when(expressionValidator.validateValueElExpression(anyString()))
            .thenReturn(new AttributesValidationResult());

        nodeValidator.validateJsfExpressions(
                Attributes.with("myValueAttribute", "#{valid1}"));

        verify(expressionValidator).validateValueElExpression(anyString());
        verify(expressionValidator, never()).validateMethodElExpression(anyString());
    }


}
