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



package net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer.instrumenter;


import com.sun.faces.el.impl.AndOperator
import com.sun.faces.el.impl.BinaryOperatorExpression
import com.sun.faces.el.impl.ConditionalExpression
import com.sun.faces.el.impl.OrOperator
import net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer.RecordingNodeStub
import org.junit.Before
import org.junit.Test

import static java.lang.Boolean.TRUE
import static java.lang.Boolean.FALSE

public class JavassistTransformerTest {

    private RecordingNodeStub boolNodeOne;
    private RecordingNodeStub boolNodeTwo;

    @Before
    public void setUp() {
        boolNodeOne = new RecordingNodeStub()
        boolNodeTwo = new RecordingNodeStub()
    }

    @Test
    public void should_eval_all_or_children() throws Exception {
        /*def parser = new ELParserImpl();
        parser.initParser(new StringReader("#{true or noSuchBeam}"))
        assert "" == parser.ExpressionString()*/

        def branchOne = new RecordingNodeStub(TRUE)
        def branchTwo = new RecordingNodeStub(FALSE)
        def expression = new BinaryOperatorExpression(branchOne, [new OrOperator()], [branchTwo])
        assert true == expression.evaluate(null)
        assert branchOne.called && branchTwo.called
    }

    @Test
    public void should_eval_all_and_children() throws Exception {
        def branchOne = new RecordingNodeStub(FALSE)
        def branchTwo = new RecordingNodeStub(TRUE)
        def expression = new BinaryOperatorExpression(branchOne, [new AndOperator()], [branchTwo])
        assert false == expression.evaluate(null)
        assert branchOne.called && branchTwo.called
    }

    @Test
    public void should_eval_all_choice_children() throws Exception {
        def condition = new RecordingNodeStub(TRUE)
        def branchOne = new RecordingNodeStub(22)
        def branchTwo = new RecordingNodeStub(33)
        def choice = new ConditionalExpression(condition, branchOne, branchTwo)

        assert 22 == choice.evaluate(null)
        assert condition.called && branchOne.called && branchTwo.called
    }

    @Test
    public void should_return_modified_to_string() {
        assert new AndOperator().toString().startsWith("HACKED BY JSFELCHECK ")
        assert new OrOperator().toString().startsWith("HACKED BY JSFELCHECK ")
        assert new ConditionalExpression(null, null, null).toString().startsWith("HACKED BY JSFELCHECK ")
    }

}
