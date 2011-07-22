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

package net.jakubholy.jeeutils.jsfelcheck.validator.jsf11.binding;

import static org.junit.Assert.*;

import org.junit.Test;


public class ElBindingFactoryProviderTest {

    @Test
    public void should_instantiate_defaul_implementation() throws Exception {
        assertNotNull(ElBindingFactoryProvider.getFactory(null));
    }

    @Test
    public void should_be_able_to_get_the_legacy_impl_class() throws Exception {
        assertNotNull(Class.forName(ElBindingFactoryProvider.LEGACY_IMPL_CLASS));
    }

}
