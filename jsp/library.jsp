<%@include file="header.jspf" %>

    <h1>Edit Library</h1>
  
    <f:view>
      <h:form id="libraryForm">
      
        <h:message for="libraryForm"/>

        <h:panelGrid columns="3">
          <h:outputLabel value="Library name" for="libraryName"/>
          <h:inputText id="libraryName" value="#{Library.library.libraryName}" required="true"/>
          <h:message for="libraryName"/>

          <h:outputLabel value="Library short name" for="shortName"/>
          <h:inputText id="shortName" value="#{Library.library.shortName}" required="true"/>
          <h:message for="shortName"/>

          <h:outputLabel value="Library type" for="libraryType"/>
          <h:inputText id="libraryType" value="#{Library.library.libraryType}" required="true" converter="LibraryTypeConverter"/>
          <h:message for="libraryType"/>

          <h:outputLabel value="Start plate" for="startPlate"/>
          <h:inputText id="startPlate" value="#{Library.library.startPlate}" required="true"/>
          <h:message for="startPlate"/>

          <h:outputLabel value="End plate" for="endPlate"/>
          <h:inputText id="endPlate" value="#{Library.library.endPlate}" required="true"/>
          <h:message for="endPlate"/>
        </h:panelGrid>

        <h:panelGrid columns="3" rendered="#{Library.advancedMode}">
          <h:outputLabel value="Vendor" for="vendor"/>
          <h:inputText id="vendor" value="#{Library.library.vendor}"/>
          <h:message for="vendor"/>
        </h:panelGrid>

        <h:panelGroup>
          <h:commandButton id="submitSave" action="#{Library.save}" value="Save" rendered="#{Library.usageMode == 'edit'}"/>
          <h:commandButton id="submitCreate" action="#{Library.create}" value="Create" rendered="#{Library.usageMode == 'create'}"/>
          <%-- Setting immediate="true" allows us to handle this action before bound properties are updated (as long as they have immediate="false", the default) --%>
          <%--h:commandButton id="cancelAction" immediate="true" actionListener="#{Library.cancelEventHandler}" value="Cancel"/--%>
          <h:commandButton id="cancelAction" rendered="false" onclick="javascript:document.cancelForm.cancelButton.click()" value="Cancel"/>
          <h:commandButton id="revertAction" immediate="true" actionListener="#{Library.revertEventHandler}" value="Revert"/>
          <h:commandButton id="showAdvancedAction" immediate="true" actionListener="#{Library.showAdvancedEventListener}" value="Advanced" rendered="#{not Library.advancedMode}"/>
          <h:commandButton id="showBasic" immediate="true" actionListener="#{Library.showAdvancedEventListener}" value="Basic" rendered="#{Library.advancedMode}"/>
        </h:panelGroup>

      </h:form>
      
      <h:form id="cancelForm">
      <h:commandButton id="cancelButton" value="Cancel" action="#{Library.cancel}"/>
      </h:form>
    </f:view>    
    
<%@include file="footer.jspf" %>