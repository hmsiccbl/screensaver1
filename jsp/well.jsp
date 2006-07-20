<%@include file="header.jspf"%>

<h1>
  Well Viewer
</h1>

<f:view>
  <h:form id="wellForm" title="Well Viewer">

    <h:panelGroup>
      <h:commandButton id="doneCommand" action="#{wellViewer.done}" value="Done" />
    </h:panelGroup>

    <p />

      <h:messages id="allMessages" globalOnly="false" showDetail="true" styleClass="errorMessage"/>
    <p />

      <h:panelGrid columns="2" styleClass="standardTable">
        <h:outputText id="well" value="[Display Well Here]" />
      </h:panelGrid>
    <p />

  </h:form>

</f:view>

<%@include file="footer.jspf"%>
