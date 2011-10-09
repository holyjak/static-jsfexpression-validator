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

package net.jakubholy.jeeutils.jsfelcheck.jasperelcustomizer

import com.sun.faces.el.impl.ConditionalExpression
import org.junit.Test

class GetValueFixTest {

    @Test
    public void choice_should_evaluate_all_children_when_condition_true() throws Exception {
        def condition = new RecordingNodeStub(true);
        def nodeOne = new RecordingNodeStub(1);
        def nodeTwo = new RecordingNodeStub(2);

        assert 1 == GetValueFix.choice(null, new ConditionalExpression(condition, nodeOne, nodeTwo))
        assert condition.called && nodeOne.called && nodeTwo.called
    }

    @Test
    public void choice_should_evaluate_all_children_when_condition_false() throws Exception {
        def condition = new RecordingNodeStub(false);
        def nodeOne = new RecordingNodeStub(1);
        def nodeTwo = new RecordingNodeStub(2);

        assert 2 == GetValueFix.choice(null, new ConditionalExpression(condition, nodeOne, nodeTwo))
        assert condition.called && nodeOne.called && nodeTwo.called
    }

}
