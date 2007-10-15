<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentSearchResultsViewer">

	<t:buffer into="#{searchResultsHeader}">
		<t:popup id="showHideAnnotationDialog" closePopupOnExitingPopup="true"
			closePopupOnExitingElement="false" styleClass="popupDialog">
			<t:commandLink>
				<t:outputText value="Show/Hide Annotations >>" styleClass="label" />
			</t:commandLink>
			<f:facet name="popup">
				<t:panelGrid columns="1" styleClass="popupDialog">
					<t:outputLabel for="annotationsList"
						value="Show selected annotations:" styleClass="label" />
					<t:selectManyCheckbox id="annotationsList" layout="lineDirection"
						layoutWidth="#{reagentsBrowser.annotationTypeSelector.size}"
						value="#{reagentsBrowser.annotationTypeSelector.value}"
						valueChangeListener="#{reagentsBrowser.annotationTypesTable.selectionListener}"
						binding="#{reagentsBrowser.annotationTypesTable.selectManyUIComponent}"
						styleClass="label" style="vertical-align: top">
						<f:selectItems id="annotations"
							value="#{reagentsBrowser.annotationTypeSelector.selectItems}" />
					</t:selectManyCheckbox>
					<t:panelGroup>
						<t:commandButton id="updateAnnotationTypesButton" forceId="true"
							value="Update" styleClass="command"
							title="Update the annotations selection" />
						<t:commandButton id="allAnnotationsButton" value="All"
							action="#{reagentsBrowser.annotationTypesTable.selectAll}"
							styleClass="command" title="Show all annotations" />
						<t:commandButton id="noAnnotationsButton" value="First"
							action="#{reagentsBrowser.annotationTypesTable.selectNone}"
							styleClass="command" title="Show the first annotation" />
					</t:panelGroup>
				</t:panelGrid>
			</f:facet>
		</t:popup>
	</t:buffer>

	<h:form id="reagentSearchResultsViewerForm">
		<t:aliasBean alias="#{searchResults}" value="#{reagentsBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>

	<t:panelGroup rendered="#{reagentsBrowser.entityView}">
		<%@ include file="reagentViewer.jsp"%>
	</t:panelGroup>

</f:subview>


