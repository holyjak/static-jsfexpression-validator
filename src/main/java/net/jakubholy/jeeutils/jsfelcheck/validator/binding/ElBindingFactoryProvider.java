package net.jakubholy.jeeutils.jsfelcheck.validator.binding;

import java.util.logging.Logger;

import javax.faces.application.Application;

import net.jakubholy.jeeutils.jsfelcheck.validator.binding.impl.Sun11_02ElBindingFactoryImpl;
import net.jakubholy.jeeutils.jsfelcheck.validator.binding.impl.Sun11_legacyElBindingFactoryImpl;

public class ElBindingFactoryProvider {

    private static final Logger LOG = Logger.getLogger(ElBindingFactoryProvider.class.getName());

    public static ElBindingFactory getFactory(Application application) {

        try {
            Class.forName("com.sun.faces.el.ValueBindingFactory");
            LOG.info("Instantiating JSF EL Binding factory for the legacy jsf-impl 1.1 ...");
            return new Sun11_legacyElBindingFactoryImpl();
        } catch (ClassNotFoundException e) {}

        try {
            Class.forName("com.sun.faces.el.MixedELValueBinding");
            LOG.info("Instantiating JSF EL Binding factory for the published jsf-impl 1.1_02 ...");
            return new Sun11_02ElBindingFactoryImpl(application);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No supported implementation of JSF found (jsf-impl 1.1 (legacy) and 1.1_02");
        }
    }

}
