<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%-- 
TODO:
- show more summary info fields for extant screen result
- abasetestset name should be prompted for prior to an add
- message user if duplicate keyword is added (other entity types as well?)
- table sorting (fixed, but by business key, or something appropriate)
- validation messages (per component)
- save button needs to be more accessible (multiple locations)
- provide file upload for attached file
- sort subtables
- show isShareable' property, and make editable
--%>

<f:subview id="screenViewer">

	<t:aliasBean alias="#{navigator}" value="#{screenViewer.screenSearchResults}">
		<%@ include file="../searchResultsNavPanel.jspf"%>
	</t:aliasBean>

	<h:form id="screenForm">

		<t:panelGrid id="labelsAndDataColumns" columns="2"
			rowClasses="row1,row2" columnClasses="keyColumn,column">

			<t:outputLabel for="screenId" value="Screen ID"
				visibleOnUserRole="developer" styleClass="inputLabel" />
			<t:outputText id="screenId" value="#{screenViewer.screen.screenId}"
				styleClass="dataText" visibleOnUserRole="developer" />

			<t:outputLabel for="screenNumber" value="Screen Number"
				styleClass="inputLabel" />
			<t:outputText id="screenNumber"
				value="#{screenViewer.screen.screenNumber}" styleClass="dataText" />

			<t:outputLabel for="title" value="Title" styleClass="inputLabel" />
			<t:inputText id="title" value="#{screenViewer.screen.title}"
				displayValueOnly="#{screenViewer.readOnly}" size="80"
				styleClass="input" displayValueOnlyStyleClass="dataText" />

			<t:outputLabel value="Screen Results" styleClass="inputLabel" />
			<t:panelGrid columns="1">
				<t:panelGroup>
					<t:outputLabel value="<none>" styleClass="inputLabel"
						rendered="#{empty screenViewer.screenResult}" />
					<t:outputLabel value="Date created: " styleClass="inputLabel"
						rendered="#{!empty screenViewer.screenResult}" />
					<t:outputText
						value="#{screenViewer.screenResult.dateCreated}"
						styleClass="data"
						rendered="#{!empty screenViewer.screenResult}" />
				</t:panelGroup>
				<t:commandButton
					value="#{screenViewer.editable ? \"View/Edit/Load...\" : \"View...\"}"
					action="#{screenViewer.viewScreenResult}" styleClass="command"
					rendered="#{!empty screenViewer.screenResult || screenViewer.editable}" />
			</t:panelGrid>

			<t:outputLabel for="dateCreatedEditable" value="Created"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:inputDate id="dateCreatedEditable"
				value="#{screenViewer.screen.dateCreated}" popupCalendar="true"
				rendered="#{screenViewer.editable}" styleClass="input" />
			<t:outputText id="dateCreated"
				value="#{screenViewer.screen.dateCreated}"
				rendered="#{screenViewer.readOnlyAdmin}" styleClass="dataText" />

			<t:outputLabel for="dateOfApplicationEditable"
				value="Application Date"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:inputDate id="dateOfApplicationEditable"
				value="#{screenViewer.screen.dateOfApplication}"
				popupCalendar="true" rendered="#{screenViewer.editable}"
				styleClass="input" />
			<t:outputText id="dateOfApplication"
				value="#{screenViewer.screen.dateOfApplication}"
				rendered="#{screenViewer.readOnlyAdmin}" styleClass="dataText" />

			<t:outputLabel for="dateDataMeetingScheduledEditable"
				value="Data Meeting Scheduled"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:inputDate id="dateDataMeetingScheduledEditable"
				value="#{screenViewer.screen.dataMeetingScheduled}"
				popupCalendar="true" rendered="#{screenViewer.editable}"
				styleClass="input" />
			<t:outputText id="dateDataMeetingScheduled"
				value="#{screenViewer.screen.dataMeetingScheduled}"
				rendered="#{screenViewer.readOnlyAdmin}" styleClass="dataText" />

			<t:outputLabel for="dateDataMeetingCompletedEditable"
				value="Data Meeting Completed"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:inputDate id="dateDataMeetingCompletedEditable"
				value="#{screenViewer.screen.dataMeetingComplete}"
				popupCalendar="true" rendered="#{screenViewer.editable}"
				styleClass="input" />
			<t:outputText id="dateDataMeetingCompleted"
				value="#{screenViewer.screen.dataMeetingComplete}"
				rendered="#{screenViewer.readOnlyAdmin}" styleClass="dataText" />

			<t:outputLabel for="screenType" value="Screen Type"
				styleClass="inputLabel" />
			<t:selectOneMenu id="screenType"
				value="#{screenViewer.screen.screenType}"
				converter="ScreenTypeConverter"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="input">
				<f:selectItems value="#{screenViewer.screenTypeSelectItems}" />
			</t:selectOneMenu>

			<%-- "lab name" == last name of "lab head", but former is required for UI, latter is for internal design --%>
			<t:outputLabel for="labNameEditable" value="Lab Name"
				styleClass="inputLabel" />
			<%-- TODO: Would like to use immediate="true", but seems to be causing problems 
			     with JSF components' attempts to save/restore local state. 
			     See https://wiki.med.harvard.edu/ICCBL/JsfImmedateAttributeAndHibernate --%>
			<h:panelGroup>
				<t:commandLink id="labName"
					value="#{screenViewer.screen.labHead.labName}"
					action="#{screenViewer.viewLabHead}" styleClass="dataText entityLink"
					style="margin-right: 4px" />
				<t:outputText value=" (" styleClass="dataText" />
				<h:outputLink value="mailto:#{screenViewer.screen.labHead.email}">
					<t:outputText id="labHeadEmail"
						value="#{screenViewer.screen.labHead.email}" styleClass="dataText " />
				</h:outputLink>
				<t:outputText value=")" styleClass="dataText" />
				<t:selectOneMenu id="labNameEditable"
					value="#{screenViewer.labName.value}"
					onchange="javascript:submit()" immediate="true"
					valueChangeListener="#{screenViewer.update}"
					rendered="#{screenViewer.editable}" styleClass="input">
					<f:selectItems value="#{screenViewer.labName.selectItems}" />
				</t:selectOneMenu>
			</h:panelGroup>

			<t:outputLabel for="leadScreenerEditable" value="Lead Screener"
				styleClass="inputLabel" />
			<h:panelGroup>
				<t:commandLink id="leadScreener"
					value="#{screenViewer.screen.leadScreener.fullNameLastFirst}"
					action="#{screenViewer.viewLeadScreener}" styleClass="dataText entityLink"
					style="margin-right: 4px" />
				<t:outputText value=" (" styleClass="dataText" />
				<h:outputLink
					value="mailto:#{screenViewer.screen.leadScreener.email}">
					<t:outputText id="leadScreenerEmail"
						value="#{screenViewer.screen.leadScreener.email}"
						styleClass="dataText" />
				</h:outputLink>
				<t:outputText value=")" styleClass="dataText" />
				<t:selectOneMenu id="leadScreenerEditable"
					value="#{screenViewer.leadScreener.value}"
					rendered="#{screenViewer.editable}" styleClass="input">
					<f:selectItems value="#{screenViewer.leadScreener.selectItems}" />
				</t:selectOneMenu>
			</h:panelGroup>

			<t:outputLabel for="collaborators" value="Collaborators"
				styleClass="inputLabel" />
			<t:panelGrid columns="2" styleClass="nonSpacingPanel" columnClasses="column">
				<t:outputText value="<none>" rendered="#{empty screenViewer.screen.collaborators}"/>
				<t:dataTable var="collaborator"
					value="#{screenViewer.screen.collaborators}"
					headerClass="tableHeader"
					rendered="#{!empty screenViewer.screen.collaborators}" >
					<t:column>
						<t:commandLink action="#{screenViewer.viewCollaborator}"
							value="#{collaborator.fullNameLastFirst}" styleClass="dataText entityLink" />
						<t:outputText value=" (" styleClass="dataText" />
						<t:commandLink action="#{screenViewer.viewCollaboratorLabHead}"
							value="#{collaborator.labName}" styleClass="dataText labLink" />
						<t:outputText value=")" styleClass="dataText" />
					</t:column>
				</t:dataTable>

				<t:selectManyListbox id="collaboratorsEditable"
					value="#{screenViewer.collaborators.value}"
					rendered="#{screenViewer.editable}" size="5" styleClass="input">
					<f:selectItems value="#{screenViewer.collaborators.selectItems}" />
				</t:selectManyListbox>

			</t:panelGrid>

			<t:outputLabel for="publishableProtocol" value="Publishable Protocol"
				styleClass="inputLabel" />
			<t:inputTextarea id="publishableProtocol" rows="3" cols="80"
				value="#{screenViewer.screen.publishableProtocol}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="summary" value="Summary" styleClass="inputLabel" />
			<t:inputTextarea id="summary" rows="3" cols="80"
				value="#{screenViewer.screen.summary}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="comments" value="Comments"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:inputTextarea id="comments" rows="3" cols="80"
				value="#{screenViewer.screen.comments}"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				displayValueOnly="#{screenViewer.readOnlyAdmin}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="statusItems" value="Status Items"
				styleClass="inputLabel" />
			<t:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.statusItems}" />
				<t:dataTable id="statusItems" var="statusItem"
					value="#{screenViewer.statusItemsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.statusItems}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="Status" />
						</f:facet>
						<t:outputText value="#{statusItem.statusValue}"
							styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Date" />
						</f:facet>
						<t:inputDate id="statusDateEditable"
							value="#{statusItem.statusDate}" popupCalendar="true"
							rendered="#{screenViewer.editable}" styleClass="input" />
						<t:outputText id="statusDate" value="#{statusItem.statusDate}"
							rendered="#{!screenViewer.editable}" styleClass="dataText" />
					</t:column>
					<t:column rendered="#{screenViewer.editable}">
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deleteStatusItem}" styleClass="command" />
					</t:column>
				</t:dataTable>
				<t:panelGroup rendered="#{screenViewer.editable}">
					<t:selectOneMenu value="#{screenViewer.newStatusValue}"
						required="false" styleClass="input"
						converter="StatusValueConverter">
						<f:selectItems value="#{screenViewer.newStatusValueSelectItems}" />
					</t:selectOneMenu>
					<t:commandButton value="Add Status Item"
						action="#{screenViewer.addStatusItem}" immediate="false"
						disabled="#{empty screenViewer.newStatusValueSelectItems}"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="publications" value="Publications"
				styleClass="inputLabel" />
			<t:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.publications}" />
				<t:dataTable id="publications" var="publication"
					value="#{screenViewer.publicationsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.publications}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="Pubmed ID" />
						</f:facet>
						<t:inputText value="#{publication.pubmedId}"
							rendered="#{screenViewer.editable}" styleClass="dataText" />
						<h:outputLink
							value="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi"
							rendered="#{screenViewer.readOnly}">
							<f:param name="cmd" value="Search" />
							<f:param name="db" value="PubMed" />
							<f:param name="term" value="#{publication.pubmedId}[PMID]" />
							<t:outputText value="#{publication.pubmedId}" />
						</h:outputLink>
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Year" />
						</f:facet>
						<t:inputText value="#{publication.yearPublished}"
							displayValueOnly="#{screenViewer.readOnly}" maxlength="4"
							styleClass="input" displayValueOnlyStyleClass="dataText" />
						<%-- TODO: f:validator validatorId="YearValidator" />
						</t:inputText --%>
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Title" />
						</f:facet>
						<t:inputText value="#{publication.title}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Authors" />
						</f:facet>
						<t:inputText value="#{publication.authors}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deletePublication}" styleClass="command"
							rendered="#{screenViewer.editable}" />
					</t:column>
				</t:dataTable>
				<t:panelGroup rendered="#{screenViewer.editable}">
					<t:commandButton value="Add Publication"
						action="#{screenViewer.addPublication}" immediate="false"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="lettersOfSupport" value="Letters of Support"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:panelGrid columns="1"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.lettersOfSupport}" />
				<t:dataTable id="lettersOfSupport" var="letterOfSupport"
					value="#{screenViewer.lettersOfSupportDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.lettersOfSupport}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="Written By" />
						</f:facet>
						<t:inputText value="#{letterOfSupport.writtenBy}"
							displayValueOnly="#{screenViewer.readOnlyAdmin}"
							styleClass="input" displayValueOnlyStyleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Written" />
						</f:facet>
						<t:inputDate id="statusDateEditable"
							value="#{letterOfSupport.dateWritten}" popupCalendar="true"
							rendered="#{screenViewer.editable}" styleClass="input" />
						<t:outputText id="statusDate"
							value="#{letterOfSupport.dateWritten}"
							rendered="#{screenViewer.readOnlyAdmin}" styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deleteLetterOfSupport}"
							styleClass="command" disabled="#{!screenViewer.editable}" />
					</t:column>
				</t:dataTable>
				<t:panelGroup rendered="#{screenViewer.editable}">
					<t:commandButton value="Add Letter of Support"
						action="#{screenViewer.addLetterOfSupport}" immediate="false"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="attachedFiles" value="Attached Files"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:panelGrid columns="1"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.attachedFiles}" />
				<t:dataTable id="attachedFiles" var="attachedFile"
					value="#{screenViewer.attachedFilesDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.attachedFiles}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="File Name" />
						</f:facet>
						<t:inputText value="#{attachedFile.filename}"
							displayValueOnly="#{screenViewer.readOnlyAdmin}"
							styleClass="input" displayValueOnlyStyleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Contents" />
						</f:facet>
						<t:inputText value="#{attachedFile.fileContents}"
							displayValueOnly="#{screenViewer.readOnlyAdmin}"
							styleClass="input" displayValueOnlyStyleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deleteAttachedFile}" styleClass="command"
							rendered="#{screenViewer.editable}" />
						<t:commandButton value="View..."
							action="#{screenViewer.viewAttachedFile}" styleClass="command" />
					</t:column>
				</t:dataTable>
				<t:panelGroup rendered="#{screenViewer.editable}">
					<t:commandButton value="Add Attached File"
						action="#{screenViewer.addAttachedFile}" immediate="false"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="fundingSupports" value="Funding Supports"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:panelGrid columns="1"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.fundingSupports}" />
				<t:dataTable id="fundingSupports" var="fundingSupport"
					value="#{screenViewer.fundingSupportsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.fundingSupports}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="Funding Support" />
						</f:facet>
						<t:outputText value="#{fundingSupport.value}"
							styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deleteFundingSupport}"
							styleClass="command" rendered="#{screenViewer.editable}" />
					</t:column>
				</t:dataTable>
				<t:panelGroup rendered="#{screenViewer.editable}">
					<t:selectOneMenu value="#{screenViewer.newFundingSupport}"
						required="false" styleClass="input"
						converter="FundingSupportConverter">
						<f:selectItems
							value="#{screenViewer.newFundingSupportSelectItems}" />
					</t:selectOneMenu>
					<t:commandButton value="Add Funding Support"
						action="#{screenViewer.addFundingSupport}"
						rendered="#{screenViewer.editable}"
						disabled="#{empty screenViewer.newFundingSupportSelectItems}"
						immediate="false" styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="assayReadoutTypes" value="Assay Readout Types"
				styleClass="inputLabel" />
			<t:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.assayReadoutTypes}" />
				<t:dataTable id="assayReadoutTypes" var="assayReadoutType"
					value="#{screenViewer.assayReadoutTypesDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.assayReadoutTypes}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="Assay Readout Type" />
						</f:facet>
						<t:outputText value="#{assayReadoutType.value}"
							styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deleteAssayReadoutType}"
							styleClass="command" rendered="#{screenViewer.editable}" />
					</t:column>
				</t:dataTable>
				<t:panelGroup>
					<t:selectOneMenu value="#{screenViewer.newAssayReadoutType}"
						required="false" rendered="#{screenViewer.editable}"
						styleClass="input" converter="AssayReadoutTypeConverter">
						<f:selectItems
							value="#{screenViewer.newAssayReadoutTypeSelectItems}" />
					</t:selectOneMenu>
					<t:commandButton value="Add Assay Readout Type"
						action="#{screenViewer.addAssayReadoutType}" immediate="false"
						disabled="#{empty screenViewer.newAssayReadoutTypeSelectItems}"
						styleClass="command" rendered="#{screenViewer.editable}" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="keywords" value="Keywords"
				styleClass="inputLabel" />
			<t:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.keywords}" />
				<t:dataTable id="keywords" var="keyword"
					value="#{screenViewer.keywordsDataModel}" preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.keywords}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Keyword" /--%>
						</f:facet>
						<t:outputText value="#{keyword}" styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deleteKeyword}" styleClass="command"
							rendered="#{screenViewer.editable}" />
					</t:column>
				</t:dataTable>
				<t:panelGroup rendered="#{screenViewer.editable}">
					<t:inputText id="newKeyword" value="#{screenViewer.newKeyword}"
						styleClass="input" required="false" />
					<t:commandButton id="addKeywordCommand" value="Add Keyword"
						action="#{screenViewer.addKeyword}" immediate="false"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="visitItems" value="Visits"
				styleClass="inputLabel" />
			<t:panelGrid columns="1">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.visits}" />
				<t:dataTable id="visitItems" var="visitItem"
					value="#{screenViewer.visitsDataModel}" preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.visits}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="Visit Date" />
						</f:facet>
						<t:outputText id="visitDate" value="#{visitItem.visitDate}"
							styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Created" />
						</f:facet>
						<t:outputText id="createdDate" value="#{visitItem.dateCreated}"
							styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Type" />
						</f:facet>
						<t:outputText id="createdDate" value="#{visitItem.visitType}"
							styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Performed By" />
						</f:facet>
						<t:outputText id="visitItemFullName"
							value="#{visitItem.performedBy.fullNameLastFirst}" styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton id="viewVisit" action="#{screenViewer.viewVisit}"
							value="#{screenViewer.editable ? \"View/Edit...\" : \"View...\"}"
							styleClass="command" />
						<t:commandButton id="copyVisit" action="#{screenViewer.copyVisit}"
							rendered="#{screenViewer.editable}" value="#{\"Copy...\"}"
							styleClass="command" alt="Copy to another screen" />
					</t:column>
				</t:dataTable>
				<t:panelGroup>
					<t:commandButton value="Add Cherry Pick Visit..."
						action="#{screenViewer.addCherryPickVisitItem}" immediate="false"
						styleClass="command" rendered="#{screenViewer.editable}" />
					<t:commandButton value="Add Non-Cherry Pick Visit..."
						action="#{screenViewer.addNonCherryPickVisitItem}"
						immediate="false" styleClass="command"
						rendered="#{screenViewer.editable}" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel for="abaseStudyId" value="Abase Study ID"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:inputText id="abaseStudyId"
				value="#{screenViewer.screen.abaseStudyId}"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				displayValueOnly="#{screenViewer.readOnlyAdmin}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="abaseProtocolId" value="Abase Protocol ID"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:inputText id="abaseProtocolId"
				value="#{screenViewer.screen.abaseProtocolId}"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
				displayValueOnlyStyleClass="dataText" />

			<t:outputLabel for="AbaseTestsets" value="Abase Testsets"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}"
				styleClass="inputLabel" />
			<t:panelGrid columns="1"
				rendered="#{screenViewer.readOnlyAdmin || screenViewer.editable}">
				<t:outputText value="<none>"
					rendered="#{empty screenViewer.screen.abaseTestsets}" />
				<t:dataTable id="AbaseTestsets" var="abaseTestset"
					value="#{screenViewer.abaseTestsetsDataModel}"
					preserveDataModel="false"
					rendered="#{!empty screenViewer.screen.abaseTestsets}"
					styleClass="standardTable" rowClasses="row1,row2"
					columnClasses="column" headerClass="tableHeader">
					<t:column>
						<f:facet name="header">
							<t:outputText value="Name" />
						</f:facet>
						<t:inputText value="#{abaseTestset.testsetName}"
							displayValueOnly="#{screenViewer.readOnlyAdmin}"
							styleClass="input" displayValueOnlyStyleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Date" />
						</f:facet>
						<t:inputDate id="abaseTestsetDateEditable"
							value="#{abaseTestset.testsetDate}" popupCalendar="true"
							rendered="#{screenViewer.editable}" styleClass="input" />
						<t:outputText id="abaseTestsetDate"
							value="#{abaseTestset.testsetDate}"
							rendered="#{screenViewer.readOnlyAdmin}" styleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<t:outputText value="Comments" />
						</f:facet>
						<t:inputText value="#{abaseTestset.comments}"
							displayValueOnly="#{screenViewer.readOnly}" styleClass="input"
							displayValueOnlyStyleClass="dataText" />
					</t:column>
					<t:column>
						<f:facet name="header">
							<%--t:outputText value="Action" /--%>
						</f:facet>
						<t:commandButton value="Delete" image="/images/delete.png"
							action="#{screenViewer.deleteAbaseTestset}" styleClass="command"
							rendered="#{screenViewer.editable}" />
					</t:column>
				</t:dataTable>
				<t:panelGroup rendered="#{screenViewer.editable}">
					<t:commandButton value="Add Abase Testset"
						action="#{screenViewer.addAbaseTestset}" immediate="false"
						styleClass="command" />
				</t:panelGroup>
			</t:panelGrid>

			<t:outputLabel value="Billing Information" styleClass="inputLabel"
				rendered="#{screenViewer.editable}" />
			<t:commandButton id="viewBillingInformation" value="View/Edit..."
				action="#{screenViewer.viewBillingInformation}" styleClass="command"
				rendered="#{screenViewer.editable}" enabledOnUserRole="billingAdmin" />



		</t:panelGrid>
		<t:panelGroup id="commandPanel">
			<t:commandButton id="save" value="Save" action="#{screenViewer.saveScreen}"
				styleClass="command" rendered="#{screenViewer.editable}" />
		</t:panelGroup>
	</h:form>
</f:subview>
