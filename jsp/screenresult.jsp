<%@include file="header.jspf"%>

<h1>
  Screen Result Viewer
</h1>

<f:view>
  <h:form id="rawDataForm">

    <h:message for="rawDataForm" />

    <h:commandButton id="doneCommand" action="#{ScreenResultViewer.done}" value="Done"/>

    <h:panelGroup>
      <h:selectBooleanCheckbox id="showMetadataTableCheckbox" value="#{ScreenResultViewer.showMetadataTable}" immediate="true" valueChangeListener="refresh" onclick="javascript:forms.rawDataForm.submit()"/>
      <h:outputLabel for="showMetadataTableCheckbox"><h:outputText value="Show Metadata Table"/></h:outputLabel>
      <h:selectBooleanCheckbox id="showRawDataTableCheckbox" value="#{ScreenResultViewer.showRawDataTable}" immediate="true" valueChangeListener="refresh" onclick="javascript:forms.rawDataForm.submit()"/>
      <h:outputLabel for="showRawDataTableCheckbox"><h:outputText value="Show Raw Data Table"/></h:outputLabel>
    </h:panelGroup>

    <h:dataTable id="metadataTable" binding="#{ScreenResultViewer.metadataTable}" value="#{ScreenResultViewer.metadata}" var="row" border="1" rendered="#{ScreenResultViewer.showMetadataTable}">
      <h:column>
        <f:facet name="header">
          <h:outputText value="Metadata Type" />
        </f:facet>
        <h:outputText value="#{row.rowLabel}"/>
      </h:column>
      <%-- Note: columns for Data Headers (aka ResultValueTypes) will be added dynamically in backing bean --%>
    </h:dataTable>

    <h:panelGrid id="rawDataPanel" columns="1" rendered="#{ScreenResultViewer.showRawDataTable}">
      <h:panelGroup>
        <h:commandButton id="prevPageCommand" action="#{ScreenResultViewer.prevPage}" value="Prev" />
        <h:commandButton id="nextPageCommand" action="#{ScreenResultViewer.nextPage}" value="Next" />
      </h:panelGroup>

      <h:dataTable id="rawDataTable" binding="#{ScreenResultViewer.dataTable}" value="#{ScreenResultViewer.rawData}" var="row" rows="10" border="1">
        <h:column>
          <f:facet name="header">
            <h:outputText value="Plate" />
          </f:facet>
          <h:outputText value="#{row.well.plateNumber}" />
        </h:column>
        <h:column>
          <f:facet name="header">
            <h:outputText value="Well" />
          </f:facet>
          <h:outputText value="#{row.well.wellName}" />
        </h:column>
        <%-- Note: columns for Data Headers (aka ResultValueTypes) will be added dynamically in backing bean --%>
      </h:dataTable>
    </h:panelGrid>

  </h:form>

</f:view>

<%@include file="footer.jspf"%>
