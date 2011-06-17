package net.jakubholy.jeeutils.jsfelcheck.validator.binding;

import javax.faces.el.MethodBinding;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;

/**
 * Create JSF EL bindings using a suitable underlying implementation.
 */
public interface ElBindingFactory {

    ValueBinding createValueBinding(String ref) throws ReferenceSyntaxException;

    MethodBinding createMethodBinding(String ref);

}