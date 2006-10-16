<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<%-- 
TODO:

- hyperlink lab head and lead screener (in read-only mode)
- consider forcing a resort after a sort column value is edited
- fix/test import errors cause screenResultImporter to open, showing errors table
--%>

<f:subview id="screenViewer">

	<t:aliasBean alias="#{navigator}"
		value="#{searchResultsRegistry.searchResults}">
		<%@ include file="../searchResultsNavPanel.jspf"%>
	</t:aliasBean>

	<h:form id="screenForm">

		<t:panelGrid columns="2">

			<t:outputLabel for="screenId" value="Screen ID"
				visibleOnUserRole="developer" styleClass="inputLabel" />
			<t:outputText id="screenId" value="#{screenViewer.screen.screenId}"
				styleClass="dataText" visibleOnUserRole="developer" />

			<t:outputLabel for="screenNumber" value="Screen Number"
				styleClass="inputLabel" />
			<t:inputText id="screenNumber"
				value="#{screenViewer.screen.screenNumber}" styleClass="input"
				displayValueOnly="#{screenViewer.readOnly}" />

			<t:outputLabel for="title" value="Title" styleClass="inputLabel" />
			<t:inputText id="title" value="#{screenViewer.screen.title}"
				displayValueOnly="#{screenViewer.readOnly}" size="80"
				styleClass="input" />

			<t:outputLabel for="dateCreatedEditable" value="Created"
				styleClass="inputLabel" />
			<t:inputDate id="dateCreatedEditable"
				value="#{screenViewer.screen.dateCreated}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateCreated"
				value="#{screenViewer.screen.dateCreated}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="dateOfApplicationEditable" value="Application Date"
				styleClass="inputLabel" />
			<t:inputDate id="dateOfApplicationEditable"
				value="#{screenViewer.screen.dateOfApplication}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateOfApplication"
				value="#{screenViewer.screen.dateOfApplication}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="dateDataMeetingScheduledEditable" value="Data Meeting Scheduled"
				styleClass="inputLabel" />
			<t:inputDate id="dateDataMeetingScheduledEditable"
				value="#{screenViewer.screen.dataMeetingScheduled}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateDataMeetingScheduled"
				value="#{screenViewer.screen.dataMeetingScheduled}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="dateDataMeetingCompletedEditable" value="Data Meeting Completed"
				styleClass="inputLabel" />
			<t:inputDate id="dateDataMeetingCompletedEditable"
				value="#{screenViewer.screen.dataMeetingComplete}" popupCalendar="true"
				rendered="#{!screenViewer.readOnly}" styleClass="input" />
			<t:outputText id="dateDataMeetingCompleted"
				value="#{screenViewer.screen.dataMeetingComplete}"
				rendered="#{screenViewer.readOnly}" styleClass="dataText" />

			<t:outputLabel for="screenType" value="Screen Type"
				styleClass="inputLabel" />
			<t:selectOneMenu id="screenType"
				value="#{screenViewer.screen.screenType}"
				converter="ScreenTypeConverter" styleClass="input"
				displayValueOnly="#{screenViewer.readOnly}">
				<f:selectItems value="#{screenViewer.screenTypeSelectItems}" />
			</t:selectOneMenu>

			<%-- "lab name" == last name of "lead head", but former is required for UI, latter is for internal design --%>
			<t:outputLabel for="labName" value="Lab Name" styleClass="inputLabel" />
			<t:selectOneMenu id="labName" value="#{screenViewer.screen.labHead}"
				converter="ScreeningRoomUserConverter"
				onchange="javascript:submit()" immediate="true" valueChangeListener="#{screenViewer.update}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input">
				<f:selectItems value="#{screenViewer.labNameSelectItems}" />
			</t:selectOneMenu>

			<t:outputLabel for="leadScreener" value="Lead Screener"
				styleClass="inputLabel" />
			<t:selectOneMenu id="leadScreener"
				value="#{screenViewer.screen.leadScreener}"
				converter="ScreeningRoomUserConverter"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input">
				<f:selectItems value="#{screenViewer.leadScreenerSelectItems}" />
			</t:selectOneMenu>

			<t:outputLabel for="collaborators"
				value="Collaborators" styleClass="inputLabel" />
			<t:panelGrid columns="2" styleClass="nonSpacingPanel">
				<t:selectManyListbox id="collaboratorsEditable"
					value="#{screenViewer.screen.collaboratorsList}"
					converter="ScreeningRoomUserConverter"
					rendered="#{!screenViewer.readOnly}" size="8" styleClass="input">
					<f:selectItems value="#{screenViewer.collaboratorSelectItems}" />
				</t:selectManyListbox>

				<t:dataTable var="collaborator"
					value="#{screenViewer.screen.collaborators}">
					<h:column>
						<t:commandLink action="#{screenViewer.viewCollaborator}"
							value="#{collaborator.lastName}, #{collaborator.firstName}"
							styleClass="command">
							<f:param name="collaboratorIdToView" value="#{collaborator}" />
						</t:commandLink>
					</h:column>
				</t:dataTable>
			</t:panelGrid>

			<t:outputLabel for="publishableProtocol" value="Publishable Protocol"
				styleClass="inputLabel" />
			<t:inputTextarea id="publishableProtocol" rows="3" cols="80"
				value="#{screenViewer.screen.publishableProtocol}"
				readonly="#{screenViewer.readOnly}" styleClass="input" />

			<t:outputLabel for="summary" value="Summary"
				styleClass="inputLabel" />
			<t:inputTextarea id="summary" rows="3" cols="80"
				value="#{screenViewer.screen.summary}"
				readonly="#{screenViewer.readOnly}" styleClass="input" />

			<t:outputLabel for="comments" value="Comments"
				styleClass="inputLabel" />
			<t:inputTextarea id="comments" rows="3" cols="80"
				value="#{screenViewer.screen.comments}"
				readonly="#{screenViewer.readOnly}" styleClass="input" />

			<t:outputLabel for="statusItems" value="Status Items"
				styleClass="inputLabel" />
			<h:panelGrid columns="1">
				<t:dataTable id="statusItems" styleClass="standardTable"
					rowClasses="row1,row2" columnClasses="column" var="statusItem"
					value="#{screenViewer.statusItemsDataModel}"
					preserveDataModel="false">
					<h:column>
						<f:facet name="header">
							<h:outputText value="Date" />
						</f:facet>
						<t:inputText value="#{statusItem.statusDate}"
							rendered="#{screenViewer.readOnly}" />
						<t:inputDate id="statusDateEditable"
							value="#{statusItem.statusDate}" popupCalendar="true"
							rendered="#{!screenViewer.readOnly}" styleClass="input" />
						<t:outputText id="statusDate" value="#{statusItem.statusDate}"
							rendered="#{screenViewer.readOnly}" styleClass="dataText" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText value="Value" />
						</f:facet>
						<t:selectOneMenu value="#{statusItem.statusValue}"
							readonly="#{screenViewer.readOnly}"
							converter="StatusValueConverter">
							<f:selectItems value="#{screenViewer.statusValueSelectItems}" />
						</t:selectOneMenu>
					</h:column>
				</t:dataTable>
				<h:panelGroup>
					<t:commandButton value="Add" action="#{screenViewer.addStatusItem}"
						styleClass="command" rendered="#{!screenViewer.readOnly}" />
				</h:panelGroup>
			</h:panelGrid>

			<t:outputLabel value="Screen Results"
				styleClass="inputLabel" />
			<t:panelGroup>
				<t:outputLabel value="<none>" styleClass="inputLabel"
					rendered="#{empty screenViewer.screen.screenResult}" />
				<t:outputLabel value="Date created: " styleClass="inputLabel"
					rendered="#{!empty screenViewer.screen.screenResult}" />
				<t:outputText
					value="#{screenViewer.screen.screenResult.dateCreated}"
					styleClass="data"
					rendered="#{!empty screenViewer.screen.screenResult}" />
				<t:commandButton
					value="#{screenViewer.readOnly ? \"View...\" : \"View/Edit/Load...\"}"
					action="#{screenViewer.viewScreenResult}" styleClass="command"
					rendered="#{!empty screenViewer.screen.screenResult || !screenViewer.readOnly}" />

			</t:panelGroup>

			<t:panelGroup id="commandPanel">
				<t:commandButton id="save" value="Save"
					action="#{screenViewer.save}" styleClass="command"
					rendered="#{!screenViewer.readOnly}" />
			</t:panelGroup>
		</t:panelGrid>
	</h:form>
</f:subview>


