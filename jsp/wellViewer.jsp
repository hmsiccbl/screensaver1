<%@include file="headers.inc"%>

<f:subview id="well">

  <h:form id="wellForm">

    <h:panelGroup>
      <h:commandButton id="doneCommand" action="#{wellViewer.done}" value="Done" />
    </h:panelGroup>

    <t:div />

    <h:panelGrid columns="2" styleClass="standardTable">
      <h:outputText id="well" value="[Display Well Here]" />
    </h:panelGrid>

    <t:div />
  </h:form>

</f:subview>
