/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jakubholy.jeeutils.jsfelcheck;

import net.jakubholy.jeeutils.jsfelcheck.beanfinder.InputResource;
import net.jakubholy.jeeutils.jsfelcheck.beanfinder.ManagedBeanFinder;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.facelets.JsfElValidatingFaceletsParser;
import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.pagenodes.PageNodeListener;
import net.jakubholy.jeeutils.jsfelcheck.validator.AttributeInfo;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElExpressionFilter;
import net.jakubholy.jeeutils.jsfelcheck.validator.ElVariableResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.JsfElValidator;
import net.jakubholy.jeeutils.jsfelcheck.validator.ValidatingElResolver;
import net.jakubholy.jeeutils.jsfelcheck.validator.exception.InvalidExpressionException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ValidationResult;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Stupid non-abstract subclass so that we can test the class' code.
 */
public class TestJsfStaticAnalyzerImpl extends AbstractJsfStaticAnalyzer<TestJsfStaticAnalyzerImpl> {

	public TestJsfStaticAnalyzerImpl() {
		super(ViewType.JSP);
	}

	/** BEWARE: Only partly implemented, i.e. doesn't record all. */
    public static class RecordingDummyElResolver implements ValidatingElResolver {

        private Map<String, Object> extraVariables = new Hashtable<String, Object>();
        private Map<String, Class<?>> propertyTypeOverrides = new Hashtable<String, Class<?>>();

        @Override
        public void setUnknownVariableResolver(ElVariableResolver unknownVariableResolver) {
        }

        @Override
        public void setIncludeKnownVariablesInException(boolean includeKnownVariablesInException) {
        }

        @Override
        public void addElExpressionFilter(ElExpressionFilter elExpressionFilter) {
        }

        @Override
        public JsfElValidator declareVariable(String name, Object value) {
            extraVariables.put(name, value);
            return this;
        }

        @Override
        public JsfElValidator definePropertyTypeOverride(String mapJsfExpression, Class<?> newType) {
            propertyTypeOverrides.put(mapJsfExpression, newType);
            return this;
        }

        @Override
        public ValidationResult validateElExpression(String expression, AttributeInfo attributeInfo) {
            return new FailedValidationResult(new InvalidExpressionException("N/A", "Validation not implemented"));
        }

        public Map<String, Object> getExtraVariables() {
            return extraVariables;
        }

        public Map<String, Class<?>> getPropertyTypeOverrides() {
            return propertyTypeOverrides;
        }
    }

    private RecordingDummyElResolver recordingDummyElResolver;

    @Override
    protected ValidatingElResolver createValidatingElResolver() {
        recordingDummyElResolver = new RecordingDummyElResolver();
        return recordingDummyElResolver;
    }

	@Override
	protected JsfElValidatingFaceletsParser createValidatingFaceletsParser(File webappRoot, PageNodeListener pageNodeValidator) {
		return null;
	}

	@Override
    protected ManagedBeanFinder createManagedBeanFinder(Collection<InputResource> facesConfigFilesToRead) {
        return null;
    }

    public RecordingDummyElResolver getResolver() {
        return recordingDummyElResolver;
    }
}
