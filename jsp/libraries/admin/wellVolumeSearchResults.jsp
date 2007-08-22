<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="wellVolumeSearchResultsViewer">

	<h:form id="wellVolumeSearchResultsViewerForm">

		<t:panelTabbedPane serverSideTabSwitch="true"
			activeTabStyleClass="activeTab" inactiveTabStyleClass="inactiveTab"
			activePanelTabVar="isActivePanel">

			<t:panelTab id="wellVolumeSearchResultsTab" label="Well Volumes">
				<t:aliasBean alias="#{searchResults}"
					value="#{wellVolumesBrowser}">
					<%@include file="../../searchResults.jspf"%>
				</t:aliasBean>
			</t:panelTab>

			<t:panelTab id="wellCopyVolumeSearchResultsTab"
				label="Well/Copy Volumes">
				<t:aliasBean alias="#{searchResults}"
					value="#{wellCopyVolumesBrowser}">

					<t:buffer into="#{searchResultsFooter}">
						<t:panelGrid rendered="#{searchResults.editMode}" columns="1">
							<t:outputLabel for="wellVolumeAdjustmentActivityComments"
								value="Comments for well volume adjustment(s):"
								styleClass="label"
								title="Comments to associated with the well volume adjustment(s)" />
							<t:inputTextarea id="wellVolumeAdjustmentActivityComments"
								rows="3" cols="80"
								value="#{searchResults.wellVolumeAdjustmentActivityComments}"
								styleClass="inputText" />
						</t:panelGrid>
					</t:buffer>

					<%@include file="../../searchResults.jspf"%>
				</t:aliasBean>
			</t:panelTab>
		</t:panelTabbedPane>

	</h:form>

</f:subview>
