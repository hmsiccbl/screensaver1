<%@ include file="../headers.inc" %>

<f:subview id="heatMapViewer">

  <h:form id="dataForm">

    <h:panelGroup>
      <h:commandButton id="doneCommand" action="done" value="Done" styleClass="command" />
    </h:panelGroup>

    <t:div />

    <h:panelGrid columns="2" styleClass="standardTable">
      <h:outputLabel for="screenResultDateCreated" value="Date created" styleClass="keyColumn" />
      <h:outputText id="screenResultDateCreated" value="#{heatMapViewer.screenResult.dateCreated}" />

      <h:outputLabel for="screenResultDateCreated" value="Replicate count" styleClass="keyColumn" />
      <h:outputText id="screenResultReplicateCount"
        value="#{heatMapViewer.screenResult.replicateCount}" />

      <h:outputLabel for="screenResultDateCreated" value="Shareable" styleClass="keyColumn" />
      <h:outputText id="screenResultIsShareable" value="#{heatMapViewer.screenResult.shareable}" />
    </h:panelGrid>

    <t:div />

    <h:panelGroup style="align: top">
      <h:panelGrid columns="1">
        <h:outputLabel for="plateNumbers" value="Show plates: " />
        <h:selectManyListbox id="plateNumbers" value="#{heatMapViewer.plateNumbers}"
          converter="PlateNumberSelectItemConverter" size="5" styleClass="input">
          <f:selectItems value="#{heatMapViewer.plateSelectItems}" />
        </h:selectManyListbox>
        <h:commandButton id="allPlatesButton" value="All" action="#{heatMapViewer.showAllPlates}"
          styleClass="command" />
      </h:panelGrid>
      <h:panelGrid>
        <h:outputLabel for="dataHeader" value="For data header: " />
        <h:selectOneMenu id="dataHeader" value="#{heatMapViewer.dataHeaderIndex}" styleClass="input">
          <f:selectItems value="#{heatMapViewer.dataHeaderSelectItems}" />
        </h:selectOneMenu>
      </h:panelGrid>
      <h:commandButton id="updateButton" value="View" action="#{heatMapViewer.update}"
        styleClass="command" />
    </h:panelGroup>
  </h:form>

</f:subview>
