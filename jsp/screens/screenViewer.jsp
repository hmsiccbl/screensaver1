<%@include file="/headers.inc"%>

<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="screenViewer">

	<h:form id="screenForm">

		<t:panelGrid columns="2">

			<t:outputLabel for="screenId" value="Screen ID" />
			<t:inputText id="screenId" value="#{screenViewer.screen.screenId}"
				displayValueOnly="true" displayValueOnlyStyleClass="restrictedInput" />

			<t:outputLabel for="screenNumber" value="Screen Number"
				styleClass="inputLabel" />
			<t:inputText id="screenNumber"
				value="#{screenViewer.screen.screenNumber}" styleClass="input" />

			<t:outputLabel for="title" value="Title" styleClass="inputLabel" />
			<t:inputText id="title" value="#{screenViewer.screen.title}"
				styleClass="input" />

			<t:outputLabel for="dateCreated" value="Date Created"
				styleClass="inputLabel" />
			<t:inputDate id="dateCreated"
				value="#{screenViewer.screen.dateCreated}" popupCalendar="true"
				styleClass="input" />

			<t:outputLabel for="screenType" value="Screen Type"
				styleClass="inputLabel" />
			<t:selectOneMenu id="screenType"
				value="#{screenViewer.screen.screenType}"
				converter="ScreenTypeConverter" styleClass="input">
				<f:selectItems value="#{screenViewer.screenTypeSelectItems}" />
			</t:selectOneMenu>

			<%-- "lab name" == last name of "lead head", but former is required for UI, latter is for internal design --%>
			<t:outputLabel for="labName" value="Lab Name" />
			<t:selectOneMenu id="labName" value="#{screenViewer.screen.labHead}"
				converter="ScreeningRoomUserConverter" styleClass="input">
				<f:selectItems value="#{screenViewer.labNameSelectItems}" />
			</t:selectOneMenu>


			<t:outputLabel for="publishableProtocol" value="Publishable Protocol"
				styleClass="inputLabel" />
			<t:inputTextarea id="publishableProtocol" rows="3"
				value="#{screenViewer.screen.publishableProtocol}"
				styleClass="input" />

		</t:panelGrid>

		<t:panelGroup id="commandPanel">
			<t:commandButton id="save" value="Save" action="#{screenViewer.save}"
				styleClass="command" />
		</t:panelGroup>

	</h:form>

</f:subview>


