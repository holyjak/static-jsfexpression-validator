<%-- Access this page as tests/valid_el/testLocalVariables.jsf --%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<html>
    <head>
        <title>testVaroius</title>
    </head>
    <body>
        <h1>testVaroius</h1>
        <f:view>

            <div>
                <h2>Simple method binding test</h2>
                <h:commandButton action="#{myCollectionBean.actionMethod}" />
            </div>

        </f:view>
    </body>
</html>
