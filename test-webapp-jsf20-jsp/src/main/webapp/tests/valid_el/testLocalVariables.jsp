<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%-- Access this page as tests/valid_el/testLocalVariables.jsf --%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<html>
    <head>
        <title>testLocalVariables</title>
    </head>
    <body>
        <h1>testLocalVariables</h1>
        <f:view>

            <h2>Normal h:dataTable</h2>
            <h:dataTable value="#{myCollectionBean.list}" var="myCollItem">
                <h:column>
                    <h:outputText value="Item value: #{myCollItem.value}" />
                </h:column>
            </h:dataTable>

            <h2>Completely custom local variable producing tag</h2>
            TBD

            <h1>Property override in a collection</h1>
            <h:outputText value="List item value: #{myCollectionBean.list[0].value}" />

            <h1>Extra variables</h1>
            <h:outputText value="Unknown managed bean - extra variable: #{iAmExtraVariable}" />

        </f:view>
    </body>
</html>
