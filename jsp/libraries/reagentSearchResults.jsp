<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="reagentSearchResultsViewer">

	<h:form id="reagentSearchResultsViewerForm">

		<t:collapsiblePanel id="reagentsDataPanel"
		value="#{reagentsBrowser.isPanelCollapsedMap['reagentsData']}"
			title="Reagents" var="isCollapsed" titleVar="title">
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

			<t:aliasBean alias="#{searchResults}" value="#{reagentsBrowser}">
				<%@include file="../searchResults.jspf"%>
			</t:aliasBean>
		</t:collapsiblePanel>
	</h:form>

	<t:panelGroup rendered="#{reagentsBrowser.entityView && !reagentsBrowser.isPanelCollapsedMap['annotationValues']}">
		<%@ include file="reagentViewer.jsp"%>
	</t:panelGroup>
</f:subview>


