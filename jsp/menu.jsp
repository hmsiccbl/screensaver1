<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="menu">
  <t:panelGrid columns="1">

    <t:commandLink id="menuTitle" action="goMain" value="#{menu.applicationTitle}" styleClass="menuItem title"/>

    <t:htmlTag id="menuSectionSeparator0" value="hr" rendered="#{menu.authenticatedUser}"/>

		<%-- TODO: this layout won't work well for long user names... --%>
		<t:panelGroup rendered="#{menu.authenticatedUser}">
			<h:form id="userForm">
				<t:outputText value="User " styleClass="label"/>
				<t:commandLink id="userName" action="goMyAccount" value="#{menu.userPrincipalName}" styleClass="menuItem userName"/>
				<t:outputText value="|" styleClass="spacer"/>
				<t:commandLink id="account" action="goMyAccount" value="#{\"Edit\"}" styleClass="menuItem"/>
				<t:outputText value="|" styleClass="spacer"/>
				<t:commandLink id="logout" action="#{menu.logout}" value="#{\"Logout\"}" styleClass="menuItem"/>
			</h:form>
		</t:panelGroup>
  
    <t:htmlTag id="menuSectionSeparator1" value="hr" />

    <h:form id="navForm">
      <t:panelNavigation2 id="navMenu" layout="table" itemClass="menuItem"
        openItemClass="menuItem" activeItemClass="menuItemActive"
        separatorClass="navSeparator">
        <t:commandNavigation2 action="goQuery" value="#{\"Search\"}" accesskey="S" />
        <t:commandNavigation2 action="#{librariesBrowser.goBrowseLibraries}" value="#{\"Browse Libraries\"}" accesskey="L" />
        <t:commandNavigation2 action="#{screensBrowser.goBrowseScreens}" value="#{\"Browse Screens\"}" accesskey="S" />
        <t:commandNavigation2 accesskey="" />
        <t:commandNavigation2 action="goHelp" value="#{\"Help\"}" accesskey="H" />
        <t:commandNavigation2 id="navPanelAdminNode" value="#{\"Admin >>\"}" accesskey="" visibleOnUserRole="readEverythingAdmin" >
          <t:commandNavigation2 action="goEditUser" value="#{\"Edit Users\"}" accesskey="usersAdmin" />
          <t:commandNavigation2 action="goEditLibraries" value="#{\"Edit Libraries\"}" accesskey="librariesAdmin" />
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

