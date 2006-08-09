<%@ include file="headers.inc"%>

<f:subview id="screenResultViewer">

  <h:form id="commandForm">

    <h:panelGroup>
      <h:commandButton action="#{screenResultViewer.download}" value="Download" styleClass="command" />
      <h:commandButton action="#{screenResultViewer.delete}" value="Delete" styleClass="command" />
      <h:commandButton action="#{screenResultViewer.viewHeatMaps}" value="View Heat Maps"
        styleClass="command" />
    </h:panelGroup>

  </h:form>

  <%@ include file="cherryPickUploader.jspf" %>
  
  <h:form id="dataForm">

    <h:panelGrid columns="2" styleClass="standardTable">
      <h:outputLabel for="screenResultDateCreated" value="Date created" styleClass="keyColumn" />
      <h:outputText id="screenResultDateCreated"
        value="#{screenResultViewer.screenResult.dateCreated}" />

      <h:outputLabel for="screenResultDateCreated" value="Replicate count" styleClass="keyColumn" />
      <h:outputText id="screenResultReplicateCount"
        value="#{screenResultViewer.screenResult.replicateCount}" />

      <h:outputLabel for="screenResultDateCreated" value="Shareable" styleClass="keyColumn" />
      <h:outputText id="screenResultIsShareable"
        value="#{screenResultViewer.screenResult.shareable}" />
    </h:panelGrid>

    <t:div />

    <h:panelGrid columns="1">
      <h:outputLabel for="dataHeadersList" value="Show selected data headers:" />
      <h:selectManyListbox id="dataHeadersList"
        value="#{screenResultViewer.selectedDataHeaderNames}"
        valueChangeListener="#{screenResultViewer.selectedDataHeadersListener}" styleClass="input">
        <f:selectItems id="dataHeaders" value="#{screenResultViewer.dataHeaderSelectItems}" />
      </h:selectManyListbox>
      <h:panelGroup>
        <h:commandButton id="updateButton1" value="Update" action="#{screenResultViewer.update}"
          styleClass="command" />
        <h:commandButton id="allDataHeadersButton" value="All"
          action="#{screenResultViewer.showAllDataHeaders}" styleClass="command" />
      </h:panelGroup>
    </h:panelGrid>

    <h:panelGrid columns="1">
      <h:panelGroup>
        <h:outputLabel for="metadataTable" value="Data Headers" styleClass="sectionHeader" />
        <f:verbatim>&nbsp;(</f:verbatim>
        <h:selectBooleanCheckbox id="showMetadataTableCheckbox"
          value="#{screenResultViewer.showMetadataTable}"
          valueChangeListener="#{screenResultViewer.showTableOptionListener}"
          onclick="javascript:document.getElementById('screenResultViewer:dataForm:updateButton1').click()" />
        <h:outputLabel for="showMetadataTableCheckbox" value="show" />
        <f:verbatim>)</f:verbatim>
      </h:panelGroup>

      <t:dataTable id="metadataTable" value="#{screenResultViewer.metadata}" var="row"
        rendered="#{screenResultViewer.showMetadataTable}" styleClass="standardTable"
        headerClass="tableHeader" rowClasses="row1,row2">
        <t:column styleClass="keyColumn">
          <f:facet name="header">
            <h:outputText value="Property" />
          </f:facet>
          <h:outputText value="#{row.rowLabel}" />
        </t:column>
        <t:columns value="#{screenResultViewer.dataHeaderColumnModel}" var="columnName"
          styleClass="column">
          <f:facet name="header">
            <h:outputText value="#{columnName}" />
          </f:facet>
          <h:outputText value="#{screenResultViewer.metadataCellValue}" />
        </t:columns>
      </t:dataTable>
    </h:panelGrid>

    <h:panelGrid columns="1">
      <h:panelGroup>
        <h:outputLabel for="rawDataTable" value="Data" styleClass="sectionHeader" />
        <f:verbatim>&nbsp;(</f:verbatim>
        <h:selectBooleanCheckbox id="showRawDataTableCheckbox"
          value="#{screenResultViewer.showRawDataTable}"
          valueChangeListener="#{screenResultViewer.showTableOptionListener}"
          onclick="javascript:document.getElementById('screenResultViewer:dataForm:updateButton1').click()" />
        <h:outputLabel for="showRawDataTableCheckbox" value="show" />
        <f:verbatim>)</f:verbatim>
      </h:panelGroup>

      <t:dataTable id="rawDataTable" binding="#{screenResultViewer.dataTable}"
        value="#{screenResultViewer.rawData}" var="row" rows="10"
        rendered="#{screenResultViewer.showRawDataTable}" styleClass="standardTable"
        headerClass="tableHeader" rowClasses="row1,row2">
        <t:column styleClass="keyColumn">
          <f:facet name="header">
            <h:outputText value="Plate" />
          </f:facet>
          <h:outputText value="#{row.well.plateNumber}" />
        </t:column>
        <t:column styleClass="keyColumn">
          <f:facet name="header">
            <h:outputText value="Well" />
          </f:facet>
          <h:commandLink action="#{screenResultViewer.showWell}">
            <%-- TODO: f:param name="wellIdParam" value="#{row.well.wellId} "/ --%>
            <f:param name="wellIdParam" value="#{row.well.wellName} " />
            <h:outputText value="#{row.well.wellName}" />
          </h:commandLink>
        </t:column>
        <t:columns value="#{screenResultViewer.dataHeaderColumnModel}" var="columnName"
          styleClass="column">
          <f:facet name="header">
            <h:outputText value="#{columnName}" />
          </f:facet>
          <h:outputText value="#{screenResultViewer.rawDataCellValue}" />
        </t:columns>
      </t:dataTable>
    </h:panelGrid>
  </h:form>

  <h:form id="navigationForm">
    <h:panelGroup rendered="#{screenResultViewer.showRawDataTable}">
      <h:commandButton id="updateButton2" value="Update" action="#{screenResultViewer.update}"
        style="display: none" />
      <h:outputLabel for="plateNumber" value="Jump to plate:" />
      <h:selectOneMenu id="plateNumber" value="#{screenResultViewer.plateNumber}"
        binding="#{screenResultViewer.plateNumberInput}"
        onchange="javascript:document.getElementById('screenResultViewer:navigationForm:updateButton2').click()"
        valueChangeListener="#{screenResultViewer.plateNumberListener}"
        converter="PlateNumberSelectItemConverter" styleClass="input">
        <f:selectItems value="#{screenResultViewer.plateSelectItems}" />
      </h:selectOneMenu>
      <h:commandButton id="prevPageCommand" action="#{screenResultViewer.prevPage}" value="Prev"
        image="images/arrow-previous.gif" styleClass="command" />
      <h:commandButton id="nextPageCommand" action="#{screenResultViewer.nextPage}" value="Next"
        image="images/arrow-next.gif" styleClass="command" />
      <h:outputLabel id="rowLabel" value="Row" for="firstDisplayedRowNumber" />
      <h:inputText id="firstDisplayedRowNumber"
        value="#{screenResultViewer.firstDisplayedRowNumber}"
        binding="#{screenResultViewer.firstDisplayedRowNumberInput}"
        valueChangeListener="#{screenResultViewer.firstDisplayedRowNumberListener}" size="6"
        styleClass="input">
        <f:validateLongRange minimum="1" maximum="#{screenResultViewer.rawDataSize}" />
      </h:inputText>
      <h:outputLabel id="rowRange" value="#{screenResultViewer.rowRangeText}"
        for="firstDisplayedRowNumber" />
    </h:panelGroup>
  </h:form>
</f:subview>
