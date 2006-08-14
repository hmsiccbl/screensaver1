<%@ include file="headers.inc" %>

<f:subview id="menu">
  <t:panelGrid columns="1">

    <t:commandLink id="menuTitle" action="goMain" value="#{menu.applicationTitle}" styleClass="menuItem title"  />

    <t:commandLink id="userName" action="goMyAccount" value="#{menu.userPrincipalName}" rendered="#{menu.authenticatedUser}" styleClass="menuItem userName"/>
  
    <t:htmlTag id="menuSectionSeparator1" value="hr" />

    <h:form id="navForm">
      <t:panelNavigation2 id="navMenu" layout="table" itemClass="menuItem"
        openItemClass="menuItem" activeItemClass="menuItemActive"
        separatorClass="navSeparator">
        <t:commandNavigation2 action="goQuery" value="#{\"Search\"}" accesskey="S" />
        <t:commandNavigation2 action="#{librariesBrowser.goBrowseLibraries}" value="#{\"Browse Libraries\"}" accesskey="L" />
        <t:commandNavigation2 action="goMyAccount" value="#{\"My Account\"}" accesskey="" />
        <t:commandNavigation2 action="goHelp" value="#{\"Help\"}" accesskey="H" />
        <t:commandNavigation2 action="#{menu.logout}" value="#{\"Logout\"}" accesskey="" />
        <t:commandNavigation2 id="navPanelAdminNode" value="#{\"Admin >>\"}" accesskey="" visibleOnUserRole="admin" >
          <t:commandNavigation2 action="goImportScreenResult" value="#{\"Import Screen Result\"}" accesskey="I" />
          <t:commandNavigation2 action="goImportRNAiLibraryContents" value="#{\"Import RNAi Library Contents\"}" accesskey="I" />
          <t:commandNavigation2 action="goEditUser" value="#{\"Edit Users\"}" accesskey="" />
          <t:commandNavigation2 action="goEditLibraries" value="#{\"Edit Libraries\"}" accesskey="" />
        </t:commandNavigation2>
        <t:commandNavigation2 id="navPanelDeveloperNode" value="#{\"Developer >>\"}" accesskey="" visibleOnUserRole="developer" >
		  <t:commandNavigation2 action="goEnvironmentInfo" value="#{\"Env Info\"}" />
          <t:commandNavigation2 action="goSchemaManager" value="#{\"Schema Manager\"}" />
        </t:commandNavigation2>
      </t:panelNavigation2>
    </h:form>
    
    <t:htmlTag id="menuSectionSeparator2" value="hr" />

    <h:form id="quickFindWellForm">
      <t:panelGrid columns="2">
        <t:outputLabel id="stockPlateNumberLabel" for="stockPlateNumber" value="Plate"
          styleClass="menuItem inputLabel" />
        <t:outputLabel id="wellNameLabel" for="wellName" value="Well"
          styleClass="menuItem inputLabel" />[
        <t:inputText id="stockPlateNumber" value="#{query.stockPlateNumber}" size="5"
          styleClass="input" />
        <t:inputText id="wellName" value="#{query.wellName}" size="3" styleClass="input" />
      </t:panelGrid>
      <t:commandButton id="quickFindSubmit" value="Go" styleClass="command"/>
    </h:form>
  </t:panelGrid>
</f:subview>

