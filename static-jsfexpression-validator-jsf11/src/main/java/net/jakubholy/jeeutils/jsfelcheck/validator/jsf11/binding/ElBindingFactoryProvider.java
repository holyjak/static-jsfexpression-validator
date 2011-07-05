package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding;

import java.util.logging.Logger;

import javax.faces.application.Application;

import net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding.impl.Sun11_02ElBindingFactoryImpl;


public class ElBindingFactoryProvider {

    private static final Logger LOG = Logger.getLogger(ElBindingFactoryProvider.class.getName());

    public static ElBindingFactory getFactory(Application application) {

        try {
            Class.forName("com.sun.faces.el.ValueBindingFactory");
            LOG.info("Instantiating JSF EL Binding factory for the legacy jsf-impl 1.1 ...");
            return instantiate(
                    "net.jakubholy.jeeutils.jsfelcheck.validator.binding.impl.Sun11_legacyElBindingFactoryImpl"
                    , "legacy Sun-based v1.1");
        } catch (ClassNotFoundException e) {}

        try {
            Class.forName("com.sun.faces.el.MixedELValueBinding");
            LOG.info("Instantiating JSF EL Binding factory for the published jsf-impl 1.1_02 ...");
            return new Sun11_02ElBindingFactoryImpl(application);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No supported implementation of JSF found (jsf-impl 1.1 (legacy) and 1.1_02");
        }
    }

    private static ElBindingFactory instantiate(String implType, String jsfImlpVersion)  {
        Exception failure = null;
        try {
            @SuppressWarnings("unchecked")
            Class<ElBindingFactory> impl = (Class<ElBindingFactory>) Class.forName(implType);
            return impl.newInstance();
        } catch (ClassNotFoundException e) {
            failure = e;
        } catch (InstantiationException e) {
            failure = e;
        } catch (IllegalAccessException e) {
            failure = e;
        }

        throw new IllegalStateException("Failed to load adapter class '"
                + implType + "' for the detected jsf-impl version " + jsfImlpVersion
                , failure);
    }

}
