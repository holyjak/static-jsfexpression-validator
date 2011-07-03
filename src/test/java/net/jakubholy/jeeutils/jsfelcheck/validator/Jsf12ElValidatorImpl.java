package net.jakubholy.jeeutils.jsfelcheck.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import net.jakubholy.jeeutils.jsfelcheck.validator.MockingPropertyResolver.PropertyTypeResolver;

import org.apache.myfaces.el.unified.FacesELContext;
import org.apache.myfaces.el.unified.resolver.ScopedAttributeResolver;
import org.apache.myfaces.el.unified.resolver.implicitobject.ImplicitObjectResolver;
import org.junit.Test;

public class Jsf12ElValidatorImpl {

    private MyResolver myResolver;

    public class MyResolver extends ELResolver implements PropertyTypeResolver {

        private final CompositeELResolver allResolver;
        private ELContext currentContext;
        private MockingPropertyResolver propertyResolver;
        private PredefinedVariableResolver variableResolver;

        public MyResolver(CompositeELResolver allResolver) {
            this.allResolver = allResolver;

            this.propertyResolver = new MockingPropertyResolver();
            propertyResolver.setTypeResolver(this);

            this.variableResolver = new PredefinedVariableResolver(propertyResolver);
        }

        public void setValue(final ELContext context, final Object base, final Object property, final Object value)
            throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        }

        public boolean isReadOnly(final ELContext context, final Object base, final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException {
            return false;
        }

        public Object getValue(final ELContext context, final Object base, final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException {

            currentContext = context;

            if (!(property instanceof String)) {
                return null;
            }

            final String propertyString = (String) property;

            Object result;
            if (base == null) {
                result = resolveVariable(propertyString);
            } else {
                result = resolveProperty(base, propertyString);
            }

            if (result != null) {
                // may be this isn't necessary as likely already was set during the resolution
                context.setPropertyResolved(true);
            }

            return result;

        }

        private Object resolveProperty(Object base, String property) {
            return propertyResolver.getValue(base, property);
        }

        private Object resolveVariable(String variable) {
            return variableResolver.resolveVariable(variable);
        }

        public Class<?> getType(final ELContext context, final Object base, final Object property)
            throws NullPointerException, PropertyNotFoundException, ELException {
            return null;
        }

        public Iterator getFeatureDescriptors(final ELContext context, final Object base) {

            if (base != null) return null;

            final ArrayList<FeatureDescriptor> descriptors = new ArrayList<FeatureDescriptor>();

            Map<String, Object> declaredVariables = variableResolver.getDeclaredVariables();
            for (Entry<String, Object> variable : declaredVariables.entrySet()) {
                descriptors.add(makeDescriptor(variable.getKey(), variable.getValue().getClass()));
            }

            return descriptors.iterator();
        }

        private FeatureDescriptor makeDescriptor(final String beanName, final Class<?> managedBeanType) {
            final FeatureDescriptor fd = new FeatureDescriptor();
            fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, Boolean.TRUE);
            fd.setValue(ELResolver.TYPE, managedBeanType);
            fd.setName(beanName);
            fd.setDisplayName(beanName);
            //fd.setShortDescription();
            fd.setExpert(false);
            fd.setHidden(false);
            fd.setPreferred(true);
            return fd;
        }

        public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
            if (base != null) return null;
            return Object.class;
        }

        @Override
        public Class<?> getType(Object target, Object property) {
            return allResolver.getType(currentContext, target, property);
        }

    }

    private static interface MyVariable {
        public int getIntProperty();
    }

    /**
     * Taken from ResolverBuilderForFaces
     */
    private ELResolver buildElResolver() {
        CompositeELResolver elResolver = new CompositeELResolver();

        elResolver.add(ImplicitObjectResolver.makeResolverForFaces());

        myResolver = new MyResolver(elResolver);
        elResolver.add(myResolver);          // facesContxt.getExternalContext => .getRequest/Session/ApplicationMap()
        elResolver.add(new ResourceBundleELResolver());
        //elResolver.add(new ResourceBundleResolver()); depends on facesContext.getApplication().getResourceBundle(facesContext, property)
        elResolver.add(new MapELResolver());
        elResolver.add(new ListELResolver());
        elResolver.add(new ArrayELResolver());
        elResolver.add(new BeanELResolver());
        elResolver.add(new ScopedAttributeResolver());     // facesContxt.getExternalContext => .getRequest/Session/ApplicationMap()

        return elResolver;
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

        ExpressionFactory expressionFactory = new org.apache.el.ExpressionFactoryImpl();

        FacesContext context = mock(FacesContext.class);
        ELContext elContext = new FacesELContext(buildElResolver() , context);

        myResolver.variableResolver.declareVariable("bean", Collections.EMPTY_MAP);
        myResolver.propertyResolver.definePropertyTypeOverride("bean.*", MyVariable.class);


        ValueExpression valueExpression = expressionFactory.createValueExpression(
                elContext
                , "#{bean['key'].intProperty != 0}"
                , Object.class);

        Object result = valueExpression.getValue(elContext);

        //verify(context, times(2)).getExternalContext();
        assertEquals(true, result);
    }



}
