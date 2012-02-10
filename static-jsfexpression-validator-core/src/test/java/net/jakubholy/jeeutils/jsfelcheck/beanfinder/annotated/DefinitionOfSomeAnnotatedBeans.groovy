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



package net.jakubholy.jeeutils.jsfelcheck.beanfinder.annotated

import javax.faces.bean.ManagedBean

@ValuedBeanAnnotation
private class ValuedWithDefaultName {}

@ValuedBeanAnnotation("custom_ValuedWithExplicitName_name")
private class ValuedWithExplicitName {}

@ManagedBean
private class ManagedBeanWithDefaultName {}

@ManagedBean(name="custom_ManagedBeanWithExplicitName_name")
private class ManagedBeanWithExplicitName {}

/** See the non-public classes above (annotation finder will find them as if in files of their own) */
public abstract class DefinitionOfSomeAnnotatedBeans {}