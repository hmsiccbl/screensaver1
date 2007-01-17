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

    <%--
      HACK: there remain some difficulties in determining / predicting the order of initialization
      of the JSF managed-beads as enumerated in the faces-config.xml file. a current workaround is
      to reference the managed-beads that need to get initialized first in a null context here.
	--%>
	<h:outputText value="#{librariesController}" rendered="#{empty librariesController && false}" />
	<h:outputText value="#{screensController}" rendered="#{empty screensController && false}" />
	
    <t:commandLink id="menuTitle" action="goMain" value="#{menu.applicationTitle}" styleClass="menuItem title"/>

    <t:htmlTag id="menuSectionSeparator0" value="hr" rendered="#{menu.authenticatedUser}"/>

		<t:panelGroup rendered="#{menu.authenticatedUser}">
			<h:form id="userForm">
				<%--t:outputText value="User " styleClass="label"/--%>
				<t:outputText id="userName" 
					value="#{login.screensaverUser.fullNameFirstLast}" styleClass="menuItem userName" />
					<t:div/>
				<%-- t:commandLink id="account" action="goMyAccount" value="#{\"Edit\"}" styleClass="menuItem" />
				<t:outputText value="|" styleClass="spacer" /--%>
				<t:commandLink id="logout" action="#{login.logout}" value="#{\"Logout\"}" styleClass="menuItem"/>
			</h:form>
		</t:panelGroup>
  
    <t:htmlTag id="menuSectionSeparator1" value="hr" />

    <h:form id="navForm">
			<t:panelNavigation2 id="navMenu" layout="table" itemClass="menuItem"
				openItemClass="menuItem" activeItemClass="menuItemActive"
				separatorClass="navSeparator" rendered="#{menu.authenticatedUser}">
				<t:commandNavigation2 action="#{librariesController.findWells}"
					value="#{\"Find Wells\"}"
					accesskey="W" />
				<t:commandNavigation2
					action="#{librariesController.browseLibraries}"
					value="#{\"Browse Libraries\"}"
					rendered="#{menu.authenticatedUser}" accesskey="L" />
				<t:commandNavigation2 action="#{screensController.browseScreens}"
					value="#{\"Browse Screens\"}"
					rendered="#{menu.authenticatedUser && login.userAllowedAccessToScreens}"
					accesskey="S" />
				<t:commandNavigation2 accesskey="" />
				<t:commandNavigation2 action="#{mainController.viewDownloads}"
					value="#{\"Data Downloads\"}"
					accesskey="D" />
				<t:commandNavigation2 action="#{mainController.viewHelp}"
					value="Instructions"
					accesskey="H" />
				<t:commandNavigation2 accesskey="" />
				<t:commandNavigation2 id="navPanelDeveloperNode"
					value="Developer >>" accesskey="" visibleOnUserRole="developer"
					open="true">
					<t:commandNavigation2 action="goEnvironmentInfo" value="Env Info" />
					<t:commandNavigation2 action="goSchemaManager"
						value="#{\"Schema Manager\"}" />
				</t:commandNavigation2>
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
          styleClass="menuItem inputLabel"
        />
        <t:outputLabel
          id="wellNameLabel"
          for="wellName"
          value="Well"
          styleClass="menuItem inputLabel"
        />
        <t:inputText
          id="plateNumber"
          value="#{wellFinder.plateNumber}"
          size="5"
          styleClass="input"
        />
        <t:inputText
          id="wellName"
          value="#{wellFinder.wellName}"
          size="3"
          styleClass="input"
        />
        <t:commandButton
	      action="#{wellFinder.findWell}"
          id="quickFindWellSubmit"
          value="Go"
          styleClass="command"
        />
      </t:panelGrid>
    </h:form>
    
    <h:form id="quickFindScreenForm">
			<t:panelGrid columns="1"
				rendered="#{menu.authenticatedUser && login.userAllowedAccessToScreens}">
				<t:outputLabel id="screenNumberLabel" for="screenNumber"
					value="Screen #" styleClass="menuItem inputLabel" />
				<t:inputText id="screenNumber" value="#{screenFinder.screenNumber}"
					size="5" styleClass="input" validator="IntegerConverter" />
				<t:commandButton action="#{screenFinder.findScreen}"
					id="quickFindScreenSubmit" value="Go"
					rendered="#{menu.authenticatedUser}" styleClass="command" />
			</t:panelGrid>
		</h:form>
    
  </t:panelGrid>
</f:subview>

