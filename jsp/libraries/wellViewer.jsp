<%@include file="/headers.inc"%>

<f:subview id="well">

  <h:form id="wellForm">

    <h:panelGroup rendered="#{wellViewer.displayDone}">
      <h:commandButton id="doneCommand" action="#{wellViewer.done}" value="Done" />
    </h:panelGroup>

    <t:div />

    <h:panelGrid columns="2" styleClass="standardTable">
      <h:outputText id="plate" value="#{wellViewer.well.plateNumber}" />
      <h:outputText id="well" value="#{wellViewer.well.wellName}" />
    </h:panelGrid>

    <t:div />
  </h:form>

</f:subview>
