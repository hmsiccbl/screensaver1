<%@include file="header.jspf"%>

<h1>
  Screen Result Heat Map Viewer
</h1>

<f:view>
  <h:form id="dataForm" title="Screen Result Heat Map Viewer">

    <h:panelGroup>
      <h:commandButton id="doneCommand" action="#{HeatMapViewer.done}" value="Done" styleClass="command" />
    </h:panelGroup>

    <p />

      <h:messages id="allMessages" globalOnly="true" showDetail="true" styleClass="errorMessage" />
    <p />

      <h:panelGrid columns="2" styleClass="standardTable">
        <h:outputLabel for="screenResultDateCreated" value="Date created" styleClass="keyColumn" />
        <h:outputText id="screenResultDateCreated" value="#{HeatMapViewer.screenResult.dateCreated}" />

        <h:outputLabel for="screenResultDateCreated" value="Replicate count" styleClass="keyColumn" />
        <h:outputText id="screenResultReplicateCount" value="#{HeatMapViewer.screenResult.replicateCount}" />

        <h:outputLabel for="screenResultDateCreated" value="Shareable" styleClass="keyColumn" />
        <h:outputText id="screenResultIsShareable" value="#{HeatMapViewer.screenResult.shareable}" />
      </h:panelGrid>
    <p />

      <h:panelGroup style="align: top">
        <h:panelGrid columns="1">
          <h:outputLabel for="plateNumbers" value="Show plates: " />
          <h:selectManyListbox id="plateNumbers" value="#{HeatMapViewer.plateNumbers}" converter="PlateNumberSelectItemConverter" size="5" styleClass="input">
            <f:selectItems value="#{HeatMapViewer.plateSelectItems}" />
          </h:selectManyListbox>
          <h:commandButton id="allPlatesButton" value="All" action="#{HeatMapViewer.showAllPlates}" styleClass="command" />
        </h:panelGrid>
        <h:panelGrid>
          <h:outputLabel for="dataHeader" value="For data header: " />
          <h:selectOneMenu id="dataHeader" value="#{HeatMapViewer.dataHeaderIndex}" styleClass="input">
            <f:selectItems value="#{HeatMapViewer.dataHeaderSelectItems}" />
          </h:selectOneMenu>
        </h:panelGrid>
        <h:commandButton id="updateButton" value="View" action="#{HeatMapViewer.update}" styleClass="command" />
      </h:panelGroup>
  </h:form>

</f:view>

<%@include file="footer.jspf"%>
