<%--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
--%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<html>
    <head>
        <title>e-BookER</title>

        <link rel="stylesheet" media="screen,projection" type="text/css" href="${facesContext.externalContext.requestContextPath}/static/css/main.css" />
	    <link rel="stylesheet" media="screen,projection" type="text/css" href="${facesContext.externalContext.requestContextPath}/static/css/scheme.css" />
	    <link rel="stylesheet" media="print" type="text/css" href="${facesContext.externalContext.requestContextPath}/static/css/print.css" />

    </head>
    <body>

    <div id="main">

        <f:view>

        <div id="cols" class="box">
        <div id="content">
            <h2 id="content-title">Book Shop</h2>
        <div id="content-in">

        <h:form id="bookListForm" styleClass="uniForm">

            <p>
            <h:commandLink value="All" action="#{shop.findAll}" id="l1"  />
            | <h:commandLink value="Top" action="#{shop.findTopFive}" id="l2"  />
            </p>

            <h:dataTable value="#{shop.books}" var="book" border="1" styleClass="nomb table-style01" rowClasses=",bg">

                <h:column>
                    <f:facet name="header">
                     <h:commandLink value="Name" action="#{shop.sortByName}"  />
            </f:facet>
                    <h:outputText value="#{book.name}"/>
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <h:panelGroup>
                        <h:commandLink value="Author" action="#{shop.sortByAuthor}" />
                        <br />
                        <h:selectOneMenu value="#{shop.authorFilter}" onchange="submit()"
                            valueChangeListener="#{shop.authorFilterChanged}">
                            <f:selectItem itemLabel="ALL" itemValue="*" />
                            <f:selectItems value="#{shop.authors}" />
                        </h:selectOneMenu>
                        </h:panelGroup>
                    </f:facet>
                    <h:outputText value="#{book.author}"/>
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <h:commandLink value="Ranking" action="#{shop.sortByNameAndRanking}" />
                        </f:facet>
                    <h:outputText value="#{book.ranking}"/>
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <h:commandLink value="Available" action="#{shop.sortByNameAndAvailability}" /></f:facet>
                    <h:outputText value="#{book.available? 'X': ''}"/>
                </h:column>
            </h:dataTable>

        </h:form>

        </div>
        </div>
        </div>

        </f:view>

    </div>
    <p style="font-size:-1,color:light-grey">CSS from http://www.nuviotemplates.com/</p>
    </body>
</html>
