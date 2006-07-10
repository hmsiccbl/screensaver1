<%@include file="header.jspf"%>

<h1>
  Screen Result Viewer
</h1>

<f:view>
  <h:form id="dataForm" title="Screen Result Viewer">

    <%--
    <h:outputText id="generatedTime">Generated: <%=new java.util.Date().toString()%></h:outputText><br/>
    Session Info:
    <h:outputText id="sesionInfo" value="#{ScreenResultViewer.sessionInfo}"/><br/>
    --%>

    <h:panelGroup>
      <h:commandButton id="doneCommand" action="#{ScreenResultViewer.done}" value="Done" />
      <h:commandButton action="#{ScreenResultViewer.download}" value="Download" />
    </h:panelGroup>

    <p />

      <h:messages id="allMessages" globalOnly="false" showDetail="true" styleClass="errorMessage"/>
    <p />

      <h:panelGrid columns="2" styleClass="standardTable">
        <h:outputLabel for="screenResultDateCreated" value="Date created" styleClass="keyColumn"/>
        <h:outputText id="screenResultDateCreated" value="#{ScreenResultViewer.screenResult.dateCreated}" />

        <h:outputLabel for="screenResultDateCreated" value="Replicate count" styleClass="keyColumn" />
        <h:outputText id="screenResultReplicateCount" value="#{ScreenResultViewer.screenResult.replicateCount}" />

        <h:outputLabel for="screenResultDateCreated" value="Shareable" styleClass="keyColumn" />
        <h:outputText id="screenResultIsShareable" value="#{ScreenResultViewer.screenResult.shareable}" />

      </h:panelGrid>
    <p />

      <h:panelGrid columns="1">
        <h:outputLabel for="dataHeadersList" value="Show selected data headers:"/>
        <h:selectManyListbox id="dataHeadersList" value="#{ScreenResultViewer.selectedDataHeaderNames}" valueChangeListener="#{ScreenResultViewer.selectedDataHeadersListener}" styleClass="input">
          <f:selectItems id="dataHeaders" value="#{ScreenResultViewer.dataHeaderSelectItems}" />
        </h:selectManyListbox>
        <h:commandButton id="updateButton1" value="Update" action="#{ScreenResultViewer.update}" />
      </h:panelGrid>

      <h:panelGrid columns="1">
        <h:panelGroup>
          <h:outputLabel for="metadataTable" value="Data Headers" styleClass="sectionHeader" />
          <f:verbatim>&nbsp;(</f:verbatim>
          <h:selectBooleanCheckbox id="showMetadataTableCheckbox" value="#{ScreenResultViewer.showMetadataTable}" valueChangeListener="#{ScreenResultViewer.showTableOptionListener}"
            onclick="javascript:document.getElementById('dataForm:updateButton1').click()" />
          <h:outputLabel for="showMetadataTableCheckbox" value="show" />
          <f:verbatim>)</f:verbatim>
        </h:panelGroup>

        <t:dataTable id="metadataTable" value="#{ScreenResultViewer.metadata}" var="row" border="1" rendered="#{ScreenResultViewer.showMetadataTable}" styleClass="standardTable" headerClass="" rowClasses="row1,row2" columnClasses="">
          <t:column styleClass="keyColumn">
            <f:facet name="header">
              <h:outputText value="Property" />
            </f:facet>
            <h:outputText value="#{row.rowLabel}" />
          </t:column>
          <t:columns value="#{ScreenResultViewer.dataHeaderColumnModel}" var="columnName">
            <f:facet name="header">
              <h:outputText value="#{columnName}" />
            </f:facet>
            <h:outputText value="#{ScreenResultViewer.metadataCellValue}"/>
          </t:columns>
        </t:dataTable>
      </h:panelGrid>

      <h:panelGrid columns="1">
        <h:panelGroup>
          <h:outputLabel for="rawDataTable" value="Data" styleClass="sectionHeader" />
          <f:verbatim>&nbsp;(</f:verbatim>
          <h:selectBooleanCheckbox id="showRawDataTableCheckbox" value="#{ScreenResultViewer.showRawDataTable}" valueChangeListener="#{ScreenResultViewer.showTableOptionListener}"
            onclick="javascript:document.getElementById('dataForm:updateButton1').click()" />
          <h:outputLabel for="showRawDataTableCheckbox" value="show" />
          <f:verbatim>)</f:verbatim>
        </h:panelGroup>

        <t:dataTable id="rawDataTable" binding="#{ScreenResultViewer.dataTable}" value="#{ScreenResultViewer.rawData}" var="row" rows="10" border="1" rendered="#{ScreenResultViewer.showRawDataTable}" styleClass="standardTable" headerClass="" rowClasses="row1,row2" columnClasses="" >
          <t:column styleClass="keyColumn">
            <f:facet name="header">
              <h:outputText value="Plate"/>
            </f:facet>
            <h:outputText value="#{row.well.plateNumber}" />
          </t:column>
          <t:column styleClass="keyColumn">
            <f:facet name="header">
              <h:outputText value="Well"/>
            </f:facet>
            <h:outputText value="#{row.well.wellName}" />
          </t:column>
          <t:columns value="#{ScreenResultViewer.dataHeaderColumnModel}" var="columnName">
            <f:facet name="header">
              <h:outputText value="#{columnName}" />
            </f:facet>
            <h:outputText value="#{ScreenResultViewer.rawDataCellValue}"/>
          </t:columns>
        </t:dataTable>
      </h:panelGrid>
  </h:form>

  <h:form id="navigationForm">


    <h:panelGroup rendered="#{ScreenResultViewer.showRawDataTable}">
      <h:commandButton id="updateButton2" value="Update" action="#{ScreenResultViewer.update}" style="display: none"/>
      <h:outputLabel for="plateNumber" value="Jump to plate:" />
      <h:selectOneMenu id="plateNumber" value="#{ScreenResultViewer.plateNumber}" binding="#{ScreenResultViewer.plateNumberInput}" onchange="javascript:document.getElementById('navigationForm:updateButton2').click()"
        valueChangeListener="#{ScreenResultViewer.plateNumberListener}" converter="PlateNumberSelectItemConverter" styleClass="input">
        <f:selectItems value="#{ScreenResultViewer.plateSelectItems}" />
      </h:selectOneMenu>
      <h:commandButton id="prevPageCommand" action="#{ScreenResultViewer.prevPage}" value="Prev" styleClass="command"/>
      <h:commandButton id="nextPageCommand" action="#{ScreenResultViewer.nextPage}" value="Next" styleClass="command"/>
      <h:outputLabel id="rowLabel" value="Row" for="firstDisplayedRowNumber"/>
      <h:inputText id="firstDisplayedRowNumber" value="#{ScreenResultViewer.firstDisplayedRowNumber}" binding="#{ScreenResultViewer.firstDisplayedRowNumberInput}" valueChangeListener="#{ScreenResultViewer.firstDisplayedRowNumberListener}" size="6" styleClass="input">
        <f:validateLongRange minimum="1" maximum="#{ScreenResultViewer.rawDataSize}" />
      </h:inputText>
      <h:outputLabel id="rowRange" value="#{ScreenResultViewer.rowRangeText}" for="firstDisplayedRowNumber" />
    </h:panelGroup>

  </h:form>

</f:view>

<%@include file="footer.jspf"%>
