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
			<t:commandLink id="menuTitle" action="#{menu.viewMain}"
				value="#{menu.applicationName}" styleClass="menuItem title"
				title="Go to the Screensaver main page" />
			<t:htmlTag value="br" />
			<t:outputText id="version" value="#{menu.applicationVersion}"
				styleClass="menuItem label"
				title="The current version of Screensaver" />
			<t:outputText id="buildNumber"
				value="(#{menu.applicationBuildNumber})" styleClass="menuItem label"
				visibleOnUserRole="developer" title="The build number" />
		</h:form>

		<t:htmlTag id="menuSectionSeparator0" value="hr"
			rendered="#{menu.authenticatedUser}" />

		<t:panelGroup rendered="#{menu.authenticatedUser}">
			<h:form id="userForm">
				<%--t:outputText value="User " styleClass="label"/--%>
				<t:outputText id="userName"
					value="#{menu.screensaverUser.fullNameFirstLast}"
					styleClass="menuItem userName"
					title="The name of the logged in user" />
				<t:div />
				<%-- t:commandLink id="account" action="goMyAccount" value="#{\"Edit\"}" styleClass="menuItem" />
				<t:outputText value="|" styleClass="spacer" /--%>
				<t:commandLink id="logout" action="#{menu.logout}"
					value="#{\"Logout\"}" styleClass="menuItem"
					title="Click here to log out" />
			</h:form>
		</t:panelGroup>

		<t:htmlTag id="menuSectionSeparator1" value="hr" />

		<h:form id="navForm">
			<t:panelNavigation2 id="navMenu" layout="table" itemClass="menuItem"
				openItemClass="menuItem" activeItemClass="menuItemActive"
				rendered="#{menu.authenticatedUser}">
				<t:commandNavigation2 action="#{menu.findWells}" value="Find Wells"
					accesskey="W"
					title="Look up one or more library wells by plate number and well name"
					rendered="#{menu.screener || menu.readAdmin}" />
				<t:commandNavigation2 action="#{menu.findReagents}" value="Find Reagents"
					accesskey="R"
					title="Look up one or more reagents by vendor reagent ID" />
				<t:commandNavigation2 action="#{menu.browseLibraries}"
					value="Browse Libraries" accesskey="L"
					title="Browse the currently available libraries" />
				<t:panelNavigation2 id="libraryNavMenu" layout="table"
					itemClass="submenuItem" openItemClass="submenuItem"
					activeItemClass="menuItemActive">
					<t:commandNavigation2 action="#{menu.browseRnaiLibraries}" value="RNAi"
						title="Browse the currently available RNAi libraries" />
					<t:commandNavigation2 action="#{menu.browseSmallMoleculeLibraries}"
						value="Small Molecule"
						title="Browse the currently available small molecule libraries"/>
				</t:panelNavigation2>
				<t:commandNavigation2 action="#{menu.browseScreens}"
					value="Browse Screens" accesskey="S"
					title="Browse the screens currently available and accessible to you"
					rendered="#{menu.screener || menu.readAdmin}" />
				<t:commandNavigation2 action="#{menu.browseMyScreens}"
					value="My Screens" accesskey="M"
					title="Browse the screens that you headed, led or collaborated on"
					rendered="#{menu.screener}" />
				<t:commandNavigation2 action="#{menu.browseStudies}"
					value="Browse Studies" accesskey="T"
					title="Browse the studies currently available and accessible to you" />
				<t:commandNavigation2 action="#{menu.browseScreeningRoomActivities}"
					value="Browse Activities" accesskey="A"
					title="Browse the screening room activities currently available and accessible to you"/>
				<t:commandNavigation2 action="#{menu.browseScreeners}"
					rendered="#{menu.readAdmin}" value="Browse Screeners" accesskey="U"
					title="Browse the screeners" />
				<t:commandNavigation2 action="#{menu.browseStaff}"
					rendered="#{menu.readAdmin}" value="Browse Staff"
					title="Browse the staff members of the lab" />
				<t:commandNavigation2 />
				<t:commandNavigation2 action="#{menu.viewNews}" value="Latest News"
					accesskey="N" title="The latest Screensaver news" />
				<t:commandNavigation2 action="#{menu.viewDownloads}"
					value="Data Downloads" accesskey="D"
					title="Download SD Files for small molecule libraries" />
				<t:commandNavigation2 action="#{menu.viewHelp}" value="View Help"
					accesskey="H" title="View the Screensaver help page" />
				<t:commandNavigation2 />
				<t:commandNavigation2 action="goEnvironmentInfo" value="Environment"
					visibleOnUserRole="developer"
					title="Access information about the environment that Screensaver is running in" />
				<t:commandNavigation2 action="goSchemaManager"
					value="Schema Manager" visibleOnUserRole="developer"
					title="Various utilities for managing the database" />
			</t:panelNavigation2>
		</h:form>

		<t:htmlTag id="menuSectionSeparator2" value="hr"
			rendered="#{menu.authenticatedUser}" />

		<h:form id="quickFindWellForm">
			<t:panelGrid columns="2"
				rendered="#{menu.authenticatedUser && (menu.screener || menu.readAdmin)}"
				title="Look up a library well by plate number and well name">
				<t:outputLabel id="plateNumberLabel" for="plateNumber" value="Plate"
					styleClass="menuItem label" />
				<t:outputLabel id="wellNameLabel" for="wellName" value="Well"
					styleClass="menuItem label" />
				<t:inputText id="plateNumber" value="#{wellFinder.plateNumber}"
					size="5" styleClass="inputText" />
				<t:inputText id="wellName" value="#{wellFinder.wellName}" size="3"
					styleClass="inputText" />
				<t:commandButton action="#{wellFinder.findWell}"
					id="quickFindWellSubmit" value="Go" styleClass="command"
					rendered="#{menu.authenticatedUser}" />
				<t:outputText id="nowPanelGridHasEvenChildCount" value="" />
			</t:panelGrid>
		</h:form>

		<h:form id="quickFindScreenForm"
			title="Look up a screen by screen number">
			<t:panelGrid columns="1"
				rendered="#{menu.authenticatedUser && (menu.screener || menu.readAdmin)}">
				<t:outputLabel id="screenNumberLabel" for="screenNumber"
					value="Screen #" styleClass="menuItem label" />
				<t:inputText id="screenNumber" value="#{screenFinder.screenNumber}"
					size="5" styleClass="inputText" />
				<t:commandButton action="#{screenFinder.findScreen}"
					id="quickFindScreenSubmit" value="Go"
					rendered="#{menu.authenticatedUser}" styleClass="command" />
			</t:panelGrid>
		</h:form>

		<h:form id="quickFindCherryPickRequest"
			title="Look up a cherry pick request by cherry pick request number">
			<t:panelGrid columns="1"
				rendered="#{menu.authenticatedUser && (menu.screener || menu.readAdmin)}">
				<t:outputLabel id="cherryPickRequestNumberLabel"
					for="cherryPickRequestNumber" value="CPR #"
					styleClass="menuItem label" />
				<t:inputText id="cherryPickRequestNumber"
					value="#{cherryPickRequestFinder.cherryPickRequestNumber}" size="5"
					styleClass="inputText" />
				<t:commandButton
					action="#{cherryPickRequestFinder.findCherryPickRequest}"
					id="quickFindCherryPickRequestSubmit" value="Go"
					rendered="#{menu.authenticatedUser}" styleClass="command" />
			</t:panelGrid>
		</h:form>

	</t:panelGrid>
</f:subview>

