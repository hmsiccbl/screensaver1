<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="wellSearchResultsViewer">

	<h:form id="wellSearchResultsViewerForm">

		<t:commandButton id="updateDataTableButton" forceId="true"
			styleClass="hiddenCommand" />
		<t:collapsiblePanel id="dataTablePanel"
			value="#{wellsBrowser.isPanelCollapsedMap['dataTable']}" title="Data"
			var="isCollapsed" titleVar="title">
			<f:facet name="header">
				<t:div styleClass="subsectionHeader">
					<t:headerLink immediate="true" styleClass="subsectionHeader">
						<h:graphicImage
							value="#{isCollapsed ? \"/images/collapsed.png\" : \"/images/expanded.png\"}"
							styleClass="icon" />
						<h:outputText value="#{title}" styleClass="subsectionHeader" />
					</t:headerLink>
				</t:div>
			</f:facet>

			<t:aliasBean alias="#{searchResults}" value="#{wellsBrowser}">
				<%@include file="../searchResults.jspf"%>
			</t:aliasBean>
		</t:collapsiblePanel>
	</h:form>

	<t:panelGroup rendered="#{wellsBrowser.entityView}">
		<%@ include file="wellViewer.jsp"%>
	</t:panelGroup>

</f:subview>


