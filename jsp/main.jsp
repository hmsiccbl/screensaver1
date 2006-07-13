<%@include file="header.jspf"%>

<h1>
  Screensaver
</h1>

<f:view>

  <h:form id="createLibraryForm">
    <h:commandLink action="#{Main.viewSample1ScreenResult}">
      <h:outputText value="View Screen Result (sample 1)" />
    </h:commandLink>
    <br>
    <h:commandLink action="#{Main.viewSample2ScreenResult}">
      <h:outputText value="View Screen Result (sample 2)" />
    </h:commandLink>
    <br>
    <h:commandLink action="#{Main.createLibrary}">
      <h:outputText value="Create library" />
      <%--f:param name="mode" value="create"/--%>
    </h:commandLink>
  </h:form> 

  <h:form id="queryForm">
    <h:message for="queryForm" />

    <h:panelGrid columns="3">
      <h:outputLabel value="Find Library by name: " for="libraryName" />
      <h:inputText id="libraryName" value="#{Main.libraryName}" required="true" />
      <h:message for="libraryName" />
    </h:panelGrid>

    <h:panelGroup>
      <h:commandButton id="submitFind" action="#{Main.findLibrary}" value="Find" />
      <h:commandButton id="submitReset" action="#{Main.reset}" value="Reset" />
    </h:panelGroup>
  </h:form>

  <%@include file="screenResultImport.jspf"%>

  <%-- h:form id="calendarForm">
    <t:inputCalendar />
  </h:form --%>

</f:view>

<%@include file="footer.jspf"%>
