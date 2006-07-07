<%@include file="header.jspf"%>

<h1>
  Screen Result Viewer
</h1>

<f:view>
  <h:form id="dataForm">
  
    <%--
    <h:outputText id="generatedTime">Generated: <%=new java.util.Date().toString()%></h:outputText><br/>
    Session Info:
    <h:outputText id="sesionInfo" value="#{ScreenResultViewer.sessionInfo}"/><br/>
    --%>

    <h:message for="dataForm"/><p/>

    <h:panelGrid columns="2">
      <h:outputLabel for="screenResultDateCreated" value="Date created" />
      <h:outputText id="screenResultDateCreated" value="#{ScreenResultViewer.screenResult.dateCreated}" />

      <h:outputLabel for="screenResultDateCreated" value="Replicate count" />
      <h:outputText id="screenResultReplicateCount" value="#{ScreenResultViewer.screenResult.replicateCount}" />

      <h:outputLabel for="screenResultDateCreated" value="Shareable" />
      <h:outputText id="screenResultIsShareable" value="#{ScreenResultViewer.screenResult.shareable}" />

    </h:panelGrid>

    <p />

    <h:panelGroup>
        <h:commandButton id="doneCommand" action="#{ScreenResultViewer.done}" value="Done" />
        <h:commandButton action="#{ScreenResultViewer.download}" value="Download" />
    </h:panelGroup>
    
    <p />

      <h:panelGrid columns="1">
        <h:panelGroup>
          <h:selectBooleanCheckbox id="showMetadataTableCheckbox" value="#{ScreenResultViewer.showMetadataTable}" valueChangeListener="#{ScreenResultViewer.showTableOptionListener}"
            onclick="javascript:document.getElementById('dataForm:updateButton1').click()" />
          <h:outputLabel for="showMetadataTableCheckbox">
            <h:outputText value="Show Metadata Table" />
          </h:outputLabel>
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="showRawDataTableCheckbox" value="#{ScreenResultViewer.showRawDataTable}" valueChangeListener="#{ScreenResultViewer.showTableOptionListener}"
            onclick="javascript:document.getElementById('dataForm:updateButton1').click()" />
          <h:outputLabel for="showRawDataTableCheckbox">
            <h:outputText value="Show Raw Data Table" />
          </h:outputLabel>
        </h:panelGroup>
        <h:panelGroup>
          <h:selectManyListbox id="dataHeadersList" value="#{ScreenResultViewer.selectedDataHeaderNames}" valueChangeListener="#{ScreenResultViewer.selectedDataHeadersListener}">
            <f:selectItems id="dataHeaders" value="#{ScreenResultViewer.dataHeaderSelectItems}" />
          </h:selectManyListbox>
          <h:commandButton id="updateButton1" value="Update" action="#{ScreenResultViewer.update}" />
        </h:panelGroup>
      </h:panelGrid>

      <h:dataTable id="metadataTable" binding="#{ScreenResultViewer.metadataTable}" value="#{ScreenResultViewer.metadata}" var="row" border="1" rendered="#{ScreenResultViewer.showMetadataTable}">
        <h:column>
          <f:facet name="header">
            <h:outputText value="Metadata Type" />
          </f:facet>
          <h:outputText value="#{row.rowLabel}" />
        </h:column>
        <%-- Note: columns for Data Headers (aka ResultValueTypes) will be added dynamically in backing bean --%>
      </h:dataTable>

      <h:dataTable id="rawDataTable" binding="#{ScreenResultViewer.dataTable}" value="#{ScreenResultViewer.rawData}" var="row" rows="10" border="1" rendered="#{ScreenResultViewer.showRawDataTable}">
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
  </h:form>

  <h:form id="navigationForm">

    <h:panelGroup rendered="#{ScreenResultViewer.showRawDataTable}">
      <h:commandButton id="updateButton2" value="Update" action="#{ScreenResultViewer.update}"/>
      <h:outputLabel for="plateNumber" value="Jump to plate:"/>
      <h:selectOneMenu id="plateNumber" value="#{ScreenResultViewer.plateNumber}" binding="#{ScreenResultViewer.plateNumberInput}" onchange="javascript:document.getElementById('navigationForm:updateButton2').click()" valueChangeListener="#{ScreenResultViewer.plateNumberListener}" converter="PlateNumberSelectItemConverter">
        <f:selectItems value="#{ScreenResultViewer.plateSelectItems}" />
      </h:selectOneMenu>
      <h:commandButton id="prevPageCommand" action="#{ScreenResultViewer.prevPage}" value="Prev"/>
      <h:commandButton id="nextPageCommand" action="#{ScreenResultViewer.nextPage}" value="Next"/>
      <h:outputLabel id="rowLabel" value="Row" for="firstDisplayedRowNumber" />
      <h:inputText id="firstDisplayedRowNumber" value="#{ScreenResultViewer.firstDisplayedRowNumber}" binding="#{ScreenResultViewer.firstDisplayedRowNumberInput}" valueChangeListener="#{ScreenResultViewer.firstDisplayedRowNumberListener}"/>
      <h:outputLabel id="rowRange" value="#{ScreenResultViewer.rowRangeText}" for="firstDisplayedRowNumber" />
    </h:panelGroup>

  </h:form>

</f:view>

<%@include file="footer.jspf"%>
