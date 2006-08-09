<%@include file="headers.inc"%>

<f:subview id="libraryViewer">
  <h:form id="libraryForm">

    <h:message for="libraryForm" />

    <h:panelGrid columns="3">
      <h:outputLabel value="Library name" for="libraryName" />
      <h:inputText id="libraryName" value="#{libraryViewer.library.libraryName}" required="true" />
      <h:message for="libraryName" />

      <h:outputLabel value="Library short name" for="shortName" />
      <h:inputText id="shortName" value="#{libraryViewer.library.shortName}" required="true" />
      <h:message for="shortName" />

      <h:outputLabel value="Library type" for="libraryType" />
      <h:inputText id="libraryType" value="#{libraryViewer.library.libraryType}" required="true"
        converter="LibraryTypeConverter" />
      <h:message for="libraryType" />

      <h:outputLabel value="Start plate" for="startPlate" />
      <h:inputText id="startPlate" value="#{libraryViewer.library.startPlate}" required="true" />
      <h:message for="startPlate" />

      <h:outputLabel value="End plate" for="endPlate" />
      <h:inputText id="endPlate" value="#{libraryViewer.library.endPlate}" required="true" />
      <h:message for="endPlate" />
    </h:panelGrid>

    <h:panelGrid columns="3" rendered="#{libraryViewer.advancedMode}">
      <h:outputLabel value="Vendor" for="vendor" />
      <h:inputText id="vendor" value="#{libraryViewer.library.vendor}" />
      <h:message for="vendor" />
    </h:panelGrid>

    <h:panelGroup>
      <h:commandButton id="submitSave" action="#{libraryViewer.save}" value="Save"
        rendered="#{libraryViewer.usageMode == 'edit'}" />
      <h:commandButton id="submitCreate" action="#{libraryViewer.create}" value="Create"
        rendered="#{libraryViewer.usageMode == 'create'}" />
      <%-- Setting immediate="true" allows us to handle this action before bound properties are updated (as long as they have immediate="false", the default) --%>
      <%--h:commandButton id="cancelAction" immediate="true" actionListener="#{library.cancelEventHandler}" value="Cancel"/--%>
      <h:commandButton id="cancelAction" rendered="false"
        onclick="javascript:document.cancelForm.cancelButton.click()" value="Cancel" />
      <h:commandButton id="revertAction" immediate="true"
        actionListener="#{libraryViewer.revertEventHandler}" value="Revert" />
      <h:commandButton id="showAdvancedAction" immediate="true"
        actionListener="#{libraryViewer.showAdvancedEventListener}" value="Advanced"
        rendered="#{not libraryViewer.advancedMode}" />
      <h:commandButton id="showBasic" immediate="true"
        actionListener="#{libraryViewer.showAdvancedEventListener}" value="Basic"
        rendered="#{libraryViewer.advancedMode}" />
    </h:panelGroup>

  </h:form>

  <h:form id="cancelForm">
    <h:commandButton id="cancelButton" value="Cancel" action="#{libraryViewer.cancel}" />
  </h:form>
</f:subview>


