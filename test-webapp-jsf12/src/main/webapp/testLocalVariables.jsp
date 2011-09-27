<%-- Access this page as testLocalVariables.jsf --%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
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

            <h2>Custom h:dataTable derivation</h2>
            <t:dataTable value="#{myCollectionBean.list}" var="myCollItem">
                <t:column>
                    <t:outputText value="Item value: #{myCollItem.value}" />
                </t:column>
            </t:dataTable>

            <h2>Completely custom local variable producing tag</h2>
            TBD

        </f:view>
    </body>
</html>
