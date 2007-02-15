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

		<h:form id="titleForm">
			<t:commandLink id="menuTitle" action="#{mainController.viewMain}"
				value="#{menu.applicationName}" styleClass="menuItem title" />
			<t:htmlTag value="br"/>
			<t:outputText id="version" value="#{menu.applicationVersion}"
				styleClass="menuItem label" />
			<t:outputText id="buildNumber" value="(#{menu.applicationBuildNumber})"
				styleClass="menuItem label" visibleOnUserRole="developer" />
		</h:form>

		<t:htmlTag id="menuSectionSeparator0" value="hr" 
			rendered="#{menu.authenticatedUser}" />

		<t:panelGroup rendered="#{menu.authenticatedUser}">
			<h:form id="userForm">
				<%--t:outputText value="User " styleClass="label"/--%>
				<t:outputText id="userName" 
					value="#{menu.screensaverUser.fullNameFirstLast}" styleClass="menuItem userName" />
					<t:div/>
				<%-- t:commandLink id="account" action="goMyAccount" value="#{\"Edit\"}" styleClass="menuItem" />
				<t:outputText value="|" styleClass="spacer" /--%>
				<t:commandLink id="logout" action="#{mainController.logout}"
					value="#{\"Logout\"}" styleClass="menuItem" />
			</h:form>
		</t:panelGroup>
  
    <t:htmlTag id="menuSectionSeparator1" value="hr"/>

    <h:form id="navForm">
			<t:panelNavigation2 id="navMenu" layout="table" itemClass="menuItem"
				openItemClass="menuItem" activeItemClass="menuItemActive"
				separatorClass="navSeparator" rendered="#{menu.authenticatedUser}">
				<t:commandNavigation2 action="#{librariesController.findWells}"
					value="Find Wells"
					accesskey="W" />
				<t:commandNavigation2
					action="#{librariesController.browseLibraries}"
					value="Browse Libraries"
					accesskey="L" />
				<t:commandNavigation2 action="#{screensController.browseScreens}"
					value="Browse Screens"
					rendered="#{menu.readOnlyAdmin}"
					accesskey="S" />
				<t:commandNavigation2 />
				<t:commandNavigation2 action="#{screensController.browseMyScreens}"
					value="Browse My Screens"
					rendered="#{!menu.readOnlyAdmin}"
					accesskey="M" />
				<t:commandNavigation2 />
				<t:commandNavigation2 action="#{mainController.viewDownloads}"
					value="Data Downloads"
					accesskey="D" />
				<t:commandNavigation2 action="#{mainController.viewInstructions}"
					value="Instructions"
					accesskey="H" />
				<t:commandNavigation2 />
				<t:commandNavigation2 action="goEnvironmentInfo" value="Environment"
					visibleOnUserRole="developer" />
				<t:commandNavigation2 action="goSchemaManager"
					value="Schema Manager" visibleOnUserRole="developer" />
			</t:panelNavigation2>
		</h:form>

		<t:htmlTag id="menuSectionSeparator2" value="hr"
			rendered="#{menu.authenticatedUser}" />

		<h:form id="quickFindWellForm">
      <t:panelGrid columns="2" rendered="#{menu.authenticatedUser}">
        <t:outputLabel
          id="plateNumberLabel"
          for="plateNumber"
          value="Plate"
          styleClass="menuItem label"
        />
        <t:outputLabel
          id="wellNameLabel"
          for="wellName"
          value="Well"
          styleClass="menuItem label"
        />
        <t:inputText
          id="plateNumber"
          value="#{wellFinder.plateNumber}"
          size="5"
          styleClass="inputText"
        />
        <t:inputText
          id="wellName"
          value="#{wellFinder.wellName}"
          size="3"
          styleClass="inputText"
        />
      </t:panelGrid>
			<t:commandButton action="#{wellFinder.findWell}"
				id="quickFindWellSubmit" value="Go" styleClass="command"
				rendered="#{menu.authenticatedUser}" />
		</h:form>
    
    <h:form id="quickFindScreenForm">
			<t:panelGrid columns="1"
				rendered="#{menu.authenticatedUser}">
				<t:outputLabel id="screenNumberLabel" for="screenNumber"
					value="Screen #" styleClass="menuItem label" />
				<t:inputText id="screenNumber" value="#{screenFinder.screenNumber}"
					size="5" styleClass="inputText" />
				<t:commandButton action="#{screenFinder.findScreen}"
					id="quickFindScreenSubmit" value="Go"
					rendered="#{menu.authenticatedUser}" styleClass="command" />
			</t:panelGrid>
		</h:form>
    
  </t:panelGrid>
</f:subview>

