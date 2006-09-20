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
				displayValueOnly="true" styleClass="restrictedInput"
				visibleOnUserRole="developer" />

			<t:outputLabel for="screenNumber" value="Screen Number"
				styleClass="inputLabel" />
			<t:inputText id="screenNumber"
				value="#{screenViewer.screen.screenNumber}" styleClass="input"
				displayValueOnly="true" />

			<t:outputLabel for="title" value="Title" styleClass="inputLabel" />
			<t:inputText id="title" value="#{screenViewer.screen.title}"
				enabledOnUserRole="screensAdmin" styleClass="input" />

			<t:outputLabel for="dateCreated" value="Date Created"
				styleClass="inputLabel" />
			<t:inputDate id="dateCreated"
				value="#{screenViewer.screen.dateCreated}" popupCalendar="true"
				enabledOnUserRole="screensAdmin" styleClass="input" />

			<t:outputLabel for="screenType" value="Screen Type"
				styleClass="inputLabel" />
			<t:selectOneMenu id="screenType"
				value="#{screenViewer.screen.screenType}"
				converter="ScreenTypeConverter" styleClass="input"
				enabledOnUserRole="screensAdmin">
				<f:selectItems value="#{screenViewer.screenTypeSelectItems}" />
			</t:selectOneMenu>

			<%-- "lab name" == last name of "lead head", but former is required for UI, latter is for internal design --%>
			<t:outputLabel for="labName" value="Lab Name" />
			<t:selectOneMenu id="labName" value="#{screenViewer.screen.labHead}"
				converter="ScreeningRoomUserConverter"
				enabledOnUserRole="screensAdmin" styleClass="input">
				<f:selectItems value="#{screenViewer.labNameSelectItems}" />
			</t:selectOneMenu>

			<t:outputLabel for="leadScreener" value="Lead Screener" />
			<t:selectOneMenu id="leadScreener"
				value="#{screenViewer.screen.leadScreener}"
				converter="ScreeningRoomUserConverter"
				enabledOnUserRole="screensAdmin" styleClass="input">
				<f:selectItems value="#{screenViewer.leadScreenerSelectItems}" />
			</t:selectOneMenu>

			<t:outputLabel for="collaboratorsForm:collaborators"
				value="Collaborators" />

			<%--h:form id="collaboratorsForm"--%>

			<t:panelGrid columns="2" visibleOnUserRole="screensAdmin">

				<t:outputLabel for="collaborators" value="Existing Collaborators" />

				<t:outputLabel for="nonCollaborators" value="Non-Collaborators" />

				<t:selectManyListbox id="collaborators"
					value="#{screenViewer.selectedCollaborators}"
					converter="ScreeningRoomUserConverter" size="5" styleClass="input">
					<f:selectItems value="#{screenViewer.collaboratorSelectItems}" />
				</t:selectManyListbox>

				<t:selectManyListbox id="nonCollaborators"
					value="#{screenViewer.selectedNonCollaborators}"
					converter="ScreeningRoomUserConverter" size="5" styleClass="input">
					<f:selectItems value="#{screenViewer.nonCollaboratorSelectItems}" />
				</t:selectManyListbox>

				<t:commandButton id="removeSelectedCollaborators" value="Remove"
					action="#{screenViewer.removeSelectedCollaborators}"
					styleClass="command" />

				<t:commandButton id="addSelectedNonCollaborators" value="Add"
					action="#{screenViewer.addSelectedNonCollaborators}"
					styleClass="command" />

			</t:panelGrid>

			<%--/h:form--%>

			<t:outputLabel for="publishableProtocol" value="Publishable Protocol"
				enabledOnUserRole="screensAdmin" styleClass="inputLabel" />
			<t:inputTextarea id="publishableProtocol" rows="3"
				value="#{screenViewer.screen.publishableProtocol}"
				styleClass="input" />
		</t:panelGrid>


		<t:panelGroup id="commandPanel">
			<t:commandButton id="save" value="Save" action="#{screenViewer.save}"
				styleClass="command" visibleOnUserRole="screensAdmin" />
		</t:panelGroup>
	</h:form>
</f:subview>


