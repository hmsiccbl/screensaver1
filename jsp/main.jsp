<%@include file="header.jspf"%>

<h1>
  Screensaver
</h1>

<f:view>

  <h:form id="createLibraryForm">
    <h:commandButton value="Create library" action="#{Main.createLibrary}" styleClass="command"/>
  </h:form>

  <h:form id="queryForm">
    <h:message for="queryForm" />

    <h:panelGroup>
      <h:outputLabel value="Find Library by name: " for="libraryName" />
      <h:inputText id="libraryName" value="#{Main.libraryName}" required="true" />
      <h:message for="libraryName" />
      <h:commandButton id="submitFind" action="#{Main.findLibrary}" value="Find" styleClass="command"/>
      <h:commandButton id="submitReset" action="#{Main.reset}" value="Reset" styleClass="command"/>
    </h:panelGroup>
  </h:form>

  <%@include file="screenResultImport.jspf"%>

</f:view>

<%@include file="footer.jspf"%>
