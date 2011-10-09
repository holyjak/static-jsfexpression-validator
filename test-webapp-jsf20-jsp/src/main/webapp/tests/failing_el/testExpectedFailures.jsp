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

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<html>
    <head>
        <title>testExpectedFailures</title>
    </head>
    <body>
        <h1>testExpectedFailures</h1>
        <f:view>

            <h2>Check all expression branches evaluated</h2>
            <p>
                All parts of an expression should be evaluated even if not needed - contrary to the
                UEL specification that requires the evaluation to stop as soon as possible.
            </p>
            <p>
                <h:outputText value="Invalid EL: #{false and invalidAndBean1}" />
                <h:outputText value="Invalid EL: #{invalidAndBean2 and false}" />

                <h:outputText value="Invalid EL: #{true or invalidOrBean1}" />
                <h:outputText value="Invalid EL: #{invalidOrBean2 or true}" />

                <h:outputText value="Invalid EL: #{false? invalidChoiceBeanA1 : invalidChoiceBeanA2}" />
                <h:outputText value="Invalid EL: #{false? invalidChoiceBeanB1 : invalidChoiceBeanB2}" />
            </p>

        </f:view>
    </body>
</html>
