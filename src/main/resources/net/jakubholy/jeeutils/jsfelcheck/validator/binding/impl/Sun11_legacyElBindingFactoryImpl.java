package net.jakubholy.jeeutils.jsfelcheck.validator.binding.impl;

import javax.faces.el.MethodBinding;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;

import net.jakubholy.jeeutils.jsfelcheck.validator.binding.ElBindingFactory;

import com.sun.faces.el.MethodBindingFactory;
import com.sun.faces.el.ValueBindingFactory;

/**
 * Implementation using a legacy Sun-based jsf-impl 1.1.
 */
public class Sun11_legacyElBindingFactoryImpl implements ElBindingFactory {

    @SuppressWarnings("rawtypes")
    private static Class[] NO_PARAMS = new Class[0];

    private final ValueBindingFactory valueBindingFactory = new ValueBindingFactory();
    private final MethodBindingFactory methodBindingFactory = new MethodBindingFactory();

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.ElBindingFactory#createValueBinding(java.lang.String)
     */
    @Override
    public ValueBinding createValueBinding(String ref)
            throws ReferenceSyntaxException {
        return valueBindingFactory.createValueBinding(ref);
    }

    /* (non-Javadoc)
     * @see net.jakubholy.jeeutils.jsfelcheck.validator.ElBindingFactory#createMethodBinding(java.lang.String)
     */
    @Override
    public MethodBinding createMethodBinding(String ref) {
        if (ref == null) {
            throw new NullPointerException("The argument ref: String may not be null");
        }

        return methodBindingFactory.createMethodBinding(ref, NO_PARAMS);
    }

}
