<%@ include file="headers.inc" %>

<f:subview id="menu">
  <t:panelGrid columns="1">
    
    <t:outputText id="menuTitle" styleClass="menuItem title" value="#{main.applicationTitle}"/>
  
    <t:htmlTag id="menuSectionSeparator1" value="hr" />

    <h:form id="navForm">
      <t:panelNavigation2 id="navMenu" layout="table" itemClass="menuItem"
        openItemClass="menuItem" activeItemClass="menuItemActive"
        separatorClass="navSeparator">
        <t:commandNavigation2 action="goMain" value="#{\"Main\"}" accesskey="" />
        <t:commandNavigation2 action="goSearch" value="#{\"Search\"}" accesskey="S" />
        <t:commandNavigation2 action="goMyScreens" value="#{\"My Screens\"}" accesskey="" />
        <t:commandNavigation2 action="goMyAccount" value="#{\"My Account\"}" accesskey="" />
        <t:commandNavigation2 action="goHelp" value="#{\"Help\"}" accesskey="H" />
        <t:commandNavigation2 action="goLogout" value="#{\"Logout\"}" accesskey="" />
        <t:commandNavigation2 id="navPanelAdminNode" value="#{\"Admin >>\"}" open="true" accesskey="">
          <t:commandNavigation2 action="goImportScreenResult" value="#{\"Import Screen Result\"}" accesskey="I" />
          <t:commandNavigation2 action="goEditUser" value="#{\"Edit Users\"}" accesskey="" />
          <t:commandNavigation2 action="goEditLibraries" value="#{\"Edit Libraries\"}" accesskey="" />
        </t:commandNavigation2>
      </t:panelNavigation2>
    </h:form>
    
    <t:htmlTag id="menuSectionSeparator2" value="hr" />

    <h:form id="quickFindWellForm">
      <t:panelGrid columns="2">
        <t:outputLabel id="stockPlateNumberLabel" for="stockPlateNumber" value="Plate"
          styleClass="menuItem inputLabel" />
        <t:outputLabel id="wellNameLabel" for="wellName" value="Well"
          styleClass="menuItem inputLabel" />
        <t:inputText id="stockPlateNumber" value="#{query.quickFindStockPlateNumber}" size="5"
          styleClass="input" />
        <t:inputText id="wellName" value="#{query.quickFindWellName}" size="3" styleClass="input" />
      </t:panelGrid>
      <t:commandButton id="quickFindSubmit" value="Go" styleClass="command"/>
    </h:form>
  </t:panelGrid>
</f:subview>
