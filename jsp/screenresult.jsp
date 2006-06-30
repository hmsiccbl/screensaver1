<%@include file="header.jspf"%>

<h1>
  Screen Result Viewer
</h1>

<f:view>
  <h:form id="rawDataForm">

    <h:message for="rawDataform" />

    <h:panelGroup>
      <h:commandButton id="prevPageCommand" action="#{ScreenResultViewer.prevPage}"
        value="<"/>
      <h:commandButton id="nextPageCommand" action="#{ScreenResultViewer.nextPage}" value=">"/>
      <h:commandButton id="doneCommand" action="#{ScreenResultViewer.done}" value="Done"/>
    </h:panelGroup>

    <h:dataTable id="rawDataTable" binding="#{ScreenResultViewer.dataTable}" value="#{ScreenResultViewer.tableData}" var="row" rows="10" border="1" >
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

</f:view>

<%@include file="footer.jspf"%>
