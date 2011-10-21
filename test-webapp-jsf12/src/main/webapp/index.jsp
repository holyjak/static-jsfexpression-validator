<%@ page import="java.io.File" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.TreeSet" %>
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
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<head>
    <title>Index</title>
</head>
<body>
    <h1>Available pages</h1>

    Application:
    <ul>
        <li><a href="helloWorld.jsf">helloWorld</a></li>
    </ul>

    <%!
        // UGLY HACKED CODE :-(
        
        public Collection<String> listFiles(File file) {
            Collection<String> collected = new TreeSet<String>();
            if (!file.canRead()) {
                throw new IllegalStateException("Cannot read file " + file.getAbsolutePath());
            }
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                for (File subFile : subFiles) {
                    collected.addAll(
                        listFiles(subFile));
                }
            } else {
                collected.add(file.getPath());
            }
            return collected;
        }
    %>

    <%
        Collection<String> files = listFiles(new File("src/main/webapp/tests"));
    %>
    
    Tests:
    <ul>
        <%
            for (String file : files) {
                out.print("<li><a href='");
                out.print(file.replaceFirst("src/main/webapp/", "").replaceFirst(".jsp", ".jsf"));
                out.print("'>");
                out.print(file.replaceFirst(".*/", "").replaceFirst(".jsp$", ""));
                out.print("</a></li>");
            }
        %>
    </ul>

</body>