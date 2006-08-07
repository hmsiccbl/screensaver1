<%-- %@ include file="headers.inc" --%>

<%@ page 
  contentType="text/html; charset=ISO-8859-1"
  isErrorPage="true"
  import="java.io.*"
  language="java"
%>

<%--f:view--%>
<%--f:subview id="error"--%>
  <html>
  <head>
    <title>Screensaver Server Error</title>
    <link rel="stylesheet" type="text/css" href="css/screensaver.css" />
  </head>

  <body>
    <%--h:outputText value="--%>
    <div class="errorMessage">
      Something downright awful has occurred! Rest assured, the appropriate developer will receive
      his lashings!
    </div>
    <form>
      <textarea rows="20" cols="128" readonly="readonly">
<%
StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      out.print(sw);
      sw.close();
      pw.close();


    %>
</textarea>
    </form>
    <%-- h:form>
        <h:outputText styleClass="errorMessage" escape="false"
          value="#{exceptionReporter.infoMessage}" />
        <t:htmlTag value="br" />
        <h:inputTextarea style="width: 99%;" rows="10" readonly="true"
          value="#{exceptionReporter.stackTrace}" />
      </h:form--%>
  </body>
</html>
<%--/f:subview--%>
<%--/f:view--%>

<%--@include file="footer.jspf"--%>
