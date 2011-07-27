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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf12;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.el.FunctionMapper;

import org.apache.el.parser.AstFunction;
import org.apache.el.parser.ELParser;
import org.apache.el.parser.Node;
import org.apache.el.parser.NodeVisitor;

/**
 * Instead of resolving to the correct Method based on prefix, name, taglib URI and
 * information in a taglib info file, this mapper just returns a suitable
 * generic method taking the expected number of parameters.
 * <p>
 * In the future I may implement/reuse a true function mapper.
 */
public class MethodFakingFunctionMapper extends FunctionMapper {

    private final class FunctionArityExtractingVisitor implements NodeVisitor {

        private final Map<String, Integer> functionArity = new HashMap<String, Integer>();

        @Override
        public void visit(Node n) throws Exception {
            if (n instanceof AstFunction) {
                visitFunction((AstFunction) n);
            }
        }

        private void visitFunction(AstFunction function) throws Exception {
            String nodeFunctionQName = function.getPrefix() + ":" + function.getLocalName();
            functionArity.put(nodeFunctionQName, function.jjtGetNumChildren());
        }

        public Map<String, Integer> getFunctionArities() {
            return functionArity;
        }

    }

    private static final String DEFAULT_RESULT = "";

    // CHECKSTYLE:OFF
    static final Method[] FAKE_METHODS = new Method[] {
        createFakeMethod(0)
        , createFakeMethod(1)
        , createFakeMethod(2)
        , createFakeMethod(3)
        , createFakeMethod(4)
        , createFakeMethod(5)
    };
    // CHECKSTYLE:ON

    private final Map<String, Integer> functionArities = new HashMap<String, Integer>();
    private String currentExpression;

    private static Method createFakeMethod(int arity) {

        Class<?>[] argumentTypes = new Class[arity];
        Arrays.fill(argumentTypes, Object.class);

        try {
            return MethodFakingFunctionMapper.class.getDeclaredMethod(
                    "fakeMethod" + arity
                    , argumentTypes);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    // CHECKSTYLE:OFF
    public static String fakeMethod0() { return DEFAULT_RESULT; }
    public static String fakeMethod1(Object p1) { return DEFAULT_RESULT; }
    public static String fakeMethod2(Object p1, Object p2) { return DEFAULT_RESULT; }
    public static String fakeMethod3(Object p1, Object p2, Object p3) { return DEFAULT_RESULT; }
    public static String fakeMethod4(Object p1, Object p2, Object p3, Object p4) { return DEFAULT_RESULT; }
    public static String fakeMethod5(Object p1, Object p2, Object p3, Object p4, Object p5) { return DEFAULT_RESULT; }
    // hopefully nobody uses method of more than 5 parameters
    // CHECKSTYLE:ON

    @Override
    public Method resolveFunction(String prefix, String name) {

        final String resolvedFunctionQName = prefix + ":" + name;

        Integer arity = functionArities.get(resolvedFunctionQName);
        if (arity == null) {
            Map<String, Integer> localFunctionArities = extractFunctionArities();
            arity = localFunctionArities.get(resolvedFunctionQName);
            functionArities.putAll(localFunctionArities);
        }

        if (arity == null) {
            throw new IllegalStateException("Couldn't determine the arity of the function " + resolvedFunctionQName
                    + " from the EL '" + getCurrentExpressionOrFail() + "' - not found in it.");
        } else if (arity >= FAKE_METHODS.length) {
            throw new IllegalArgumentException("Currently we only can fake methods with up to 5 parameters but "
                    + resolvedFunctionQName + " has " + arity + ". This is really a bad practice anyway.");
        } else {
            return FAKE_METHODS[arity];
        }
    }

    private Map<String, Integer> extractFunctionArities() {
        Node parsedEl = ELParser.parse(getCurrentExpressionOrFail());
        try {
            FunctionArityExtractingVisitor arityExtractingVisitor = new FunctionArityExtractingVisitor();
            parsedEl.accept(arityExtractingVisitor);
            return arityExtractingVisitor.getFunctionArities();
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected failure parsing the EL " + getCurrentExpressionOrFail(), e);
        }
    }

    public void setCurrentExpression(String currentExpression) {
        this.currentExpression = currentExpression;
    }

    private String getCurrentExpressionOrFail() {
        if (currentExpression == null) {
            throw new IllegalStateException("Current EL expression not set!");
        }
        return currentExpression;
    }

}