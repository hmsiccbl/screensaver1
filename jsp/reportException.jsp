<%-- TODO: make this work as a JSF MyFaces page.  See http://wiki.apache.org/myfaces/Handling_Server_Errors.  Right now it's just a plain 'ol JSP page. --%>

<%@ page
  pageEncoding="UTF-8"
  import="java.util.List,java.io.PrintWriter,org.apache.myfaces.shared_tomahawk.util.ExceptionUtils"
  isErrorPage="true"%>
<!--
/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//-->
<html>
<head>
  <title>Screensaver Server Error</title>
</head>
<body>

<style>
body {
  font-family : arial, verdana, Geneva, Arial, Helvetica, sans-serif;
    font-size : 1.1em;
}
.errorHeader {
  font-size: 1.6em;
  background-color: #6392C6;
  color: white;
  font-weight: bold;
  padding: 3px;
  margin-bottom: 10px;
}

.errorFooter {
  font-size: 0.8em;
  background-color: #6392C6;
  color: white;
  font-style: italic;
  padding: 3px;
  margin-top: 5px;
}

.errorMessage {
  color: red;
  font-weight: bold;
}
.errorExceptions {
}
.errorExceptionStack {
  margin-top: 5px;
  padding: 3px;
  border-style: solid;
  border-width: 1px;
  border-color: #9F9F9F;
  background-color: #E0E0E0;
}
.errorExceptionCause {
  font-size: 1.1em;
  padding: 3px;
  border-style: solid;
  border-width: 1px;
  border-color: #9F9F9F;
  background-color: #E0E0E0;
}
.errorException {
  font-size: 1.0em;
}
</style>

    <div class="errorHeader">
      Screensaver encountered an error! (the developers will receive their lashings)
    </div>
    <%
      if (exception != null) {
        List exceptions = ExceptionUtils.getExceptions(exception);
        Throwable throwable = (Throwable) exceptions.get(exceptions.size() - 1);
        String exceptionMessage = ExceptionUtils.getExceptionMessage(exceptions);

        %>
    Message: <span class="errorMessage"><%=exceptionMessage%></span>
    <%PrintWriter pw = new PrintWriter(out);%><br/>
    <pre class="errorException">
    <%throwable.printStackTrace(pw);%>
    </pre>
    <%throwable = (Throwable) exceptions.get(0);%>
    <pre class="errorException">
      <%throwable.printStackTrace(pw);%>
    </pre>
    <%
      } else {
    %>
    Unknown error
    <%
    }
    %>
    <div class="errorFooter">
      Screensaver Exception Report
    </div>
  </body>
</html>



