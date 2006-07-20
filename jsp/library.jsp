<%@include file="header.jspf" %>

    <h1>Edit Library</h1>
  
    <f:view>
      <h:form id="libraryForm">
      
        <h:message for="libraryForm"/>

        <h:panelGrid columns="3">
          <h:outputLabel value="Library name" for="libraryName"/>
          <h:inputText id="libraryName" value="#{library.library.libraryName}" required="true"/>
          <h:message for="libraryName"/>

          <h:outputLabel value="Library short name" for="shortName"/>
          <h:inputText id="shortName" value="#{library.library.shortName}" required="true"/>
          <h:message for="shortName"/>

          <h:outputLabel value="Library type" for="libraryType"/>
          <h:inputText id="libraryType" value="#{library.library.libraryType}" required="true" converter="LibraryTypeConverter"/>
          <h:message for="libraryType"/>

          <h:outputLabel value="Start plate" for="startPlate"/>
          <h:inputText id="startPlate" value="#{library.library.startPlate}" required="true"/>
          <h:message for="startPlate"/>

          <h:outputLabel value="End plate" for="endPlate"/>
          <h:inputText id="endPlate" value="#{library.library.endPlate}" required="true"/>
          <h:message for="endPlate"/>
        </h:panelGrid>

        <h:panelGrid columns="3" rendered="#{library.advancedMode}">
          <h:outputLabel value="Vendor" for="vendor"/>
          <h:inputText id="vendor" value="#{library.library.vendor}"/>
          <h:message for="vendor"/>
        </h:panelGrid>

        <h:panelGroup>
          <h:commandButton id="submitSave" action="#{library.save}" value="Save" rendered="#{library.usageMode == 'edit'}"/>
          <h:commandButton id="submitCreate" action="#{library.create}" value="Create" rendered="#{library.usageMode == 'create'}"/>
          <%-- Setting immediate="true" allows us to handle this action before bound properties are updated (as long as they have immediate="false", the default) --%>
          <%--h:commandButton id="cancelAction" immediate="true" actionListener="#{library.cancelEventHandler}" value="Cancel"/--%>
          <h:commandButton id="cancelAction" rendered="false" onclick="javascript:document.cancelForm.cancelButton.click()" value="Cancel"/>
          <h:commandButton id="revertAction" immediate="true" actionListener="#{library.revertEventHandler}" value="Revert"/>
          <h:commandButton id="showAdvancedAction" immediate="true" actionListener="#{library.showAdvancedEventListener}" value="Advanced" rendered="#{not Library.advancedMode}"/>
          <h:commandButton id="showBasic" immediate="true" actionListener="#{library.showAdvancedEventListener}" value="Basic" rendered="#{library.advancedMode}"/>
        </h:panelGroup>

      </h:form>
      
      <h:form id="cancelForm">
      <h:commandButton id="cancelButton" value="Cancel" action="#{library.cancel}"/>
      </h:form>
    </f:view>    
    
<%@include file="footer.jspf" %>