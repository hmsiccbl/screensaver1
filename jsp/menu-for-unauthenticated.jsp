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

<%--
	A special-case "menu" subview (tile) for the login page, which is not able 
	to access Spring session-scoped beans (since Spring's RequestContextListener 
	will not have been called by Tomcat when it redirects unauthenticated users 
	to the special login.jsp form; access to this page does not invoke our 
	web.xml-registered servlet Listeners)
 --%>

<f:subview id="menu">
  <t:panelGrid columns="1">

		<h:form id="titleForm">
			<t:outputText id="menuTitle" value="#{appInfo.applicationName}"
				styleClass="menuItem title" />
			<t:htmlTag value="br"/>
			<t:outputText id="version" value="#{appInfo.applicationVersion}"
				styleClass="menuItem label"
				title="The current version of Screensaver" />
		</h:form>

  </t:panelGrid>
</f:subview>

