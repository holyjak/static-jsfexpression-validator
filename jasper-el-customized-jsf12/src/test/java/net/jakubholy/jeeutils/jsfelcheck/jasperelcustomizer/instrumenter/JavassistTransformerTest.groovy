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


import net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer.RecordingNodeStub
import org.apache.el.parser.AstAnd
import org.apache.el.parser.AstChoice
import org.apache.el.parser.AstOr
import org.apache.el.parser.Node
import org.junit.Before
import org.junit.Test

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
        Node or = new AstOr(0);
        or.jjtAddChild(boolNodeOne.withValue(true), 0);
        or.jjtAddChild(boolNodeTwo.withValue(true), 1);

        assert true == or.getValue(null);
        assert boolNodeOne.called && boolNodeTwo.called
    }

    @Test
    public void should_eval_all_and_children() throws Exception {
        Node and = new AstAnd(0);
        and.jjtAddChild(boolNodeOne.withValue(false), 0);
        and.jjtAddChild(boolNodeTwo.withValue(false), 1);

        assert false == and.getValue(null);
        assert boolNodeOne.called && boolNodeTwo.called
    }

    @Test
    public void should_eval_all_choice_children() throws Exception {
        def condition = new RecordingNodeStub(true)
        Node choice = new AstChoice(0);
        choice.jjtAddChild(condition, 0);
        choice.jjtAddChild(boolNodeOne.withValue(22), 1);
        choice.jjtAddChild(boolNodeTwo.withValue(33), 2);

        assert 22 == choice.getValue(null);
        assert condition.called && boolNodeOne.called && boolNodeTwo.called
    }

    @Test
    public void should_return_modified_to_string() {
        assert (new AstChoice(0)).toString().startsWith("HACKED BY JSFELCHECK ")
    }


}
