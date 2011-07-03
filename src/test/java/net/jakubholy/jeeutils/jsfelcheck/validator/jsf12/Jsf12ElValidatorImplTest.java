package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.el.ValueExpression;

import org.junit.Before;
import org.junit.Test;


public class Jsf12ElValidatorImplTest {

    private static interface MyVariable {
        public int getIntProperty();
    }

    private Jsf12ElValidatorImpl validator;

    @Before
    public void setUp() throws Exception {
        validator = new Jsf12ElValidatorImpl();
    }

    @Test
    public void should_evaluate_value_expression() throws Exception {

        /*
        getExpressionFactory().createValueExpression(elContext, expression, expectedType).getValue(elContext);
        • Expr.Factory - use org.apache.el.ExpressionFactoryImpl
        • ELContext - see how JsfElContext is initialized
        ‣ VariableMapper- either custom or unset & use custom ELResolver
        • Mockito with default Answer = deep mock
        ‣ FunctionMapper - either unset or find out how Japser sets & initializes it
        • ELResolver - use composed & the default ones, see ResolverBuilderForFaces
        ‣ opt. modify BeanResolver not to invoke method but mock its return type (x deep Mocks)

        + impl's faces-config parsing; annotated managed bean discovery???
         */

        validator.declareVariable("bean", Collections.EMPTY_MAP);
        validator.definePropertyTypeOverride("bean.*", MyVariable.class);

        ValueExpression valueExpression = validator.expressionFactory.createValueExpression(
                validator.elContext
                , "#{bean['key'].intProperty != 0}"
                , Object.class);

        Object result = valueExpression.getValue(validator.elContext);

        //verify(context, times(2)).getExternalContext();
        assertEquals(true, result);
    }

}
