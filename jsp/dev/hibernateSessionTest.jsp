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

<f:subview id="hibernateSessionTest">

  <h:form id="form">

		<h:panelGrid columns="2">
			<h:outputLabel for="id" value="User ID: " />
			<h:inputText id="id" value="#{hibernateSessionTest.userId}" />

			<h:outputLabel for="name" value="User name: " />
			<h:outputText id="name" value="#{hibernateSessionTest.user.lastName}" />

			<h:outputLabel for="labMembers" value="Lab Members: " />
			<h:outputText id="labMembers" value="#{hibernateSessionTest.user.labMembers}" />

			<h:outputLabel for="sessionId" value="Session ID: " />
			<h:outputText id="sesssionId" value="#{hibernateSessionTest.sessionId}" />

			<h:outputLabel for="isUserInSession" value="Is user in Hibernate session: " />
			<h:outputText id="isUserInSesssion" value="#{hibernateSessionTest.userInSession}" />

			<h:outputLabel for="isSessionOpen" value="Is session open: " />
			<h:outputText id="isSesssionOpen" value="#{hibernateSessionTest.sessionOpen}" />
		</h:panelGrid>

		<h:commandButton id="loadCmd" action="#{hibernateSessionTest.load}" value="Load" styleClass="command" />
		<h:commandButton id="refreshCmd" action="" value="Refresh" styleClass="command" />

	</h:form>

</f:subview>
