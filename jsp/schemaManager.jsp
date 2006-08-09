<%@include file="headers.inc"%>

<f:subview id="schemaManager">

  <h:form id="schemaManagerForm">

    <h:panelGrid columns="1" styleClass="standardTable">
      <h:commandButton id="dropSchema"     action="#{schemaManager.dropSchema}"     value="Drop Schema"                      styleClass="command" />
      <h:commandButton id="createSchema"   action="#{schemaManager.createSchema}"   value="Create Schema"                    styleClass="command" />
      <h:commandButton id="truncateTables" action="#{schemaManager.truncateTables}" value="Truncate Tables or Create Schema" styleClass="command" />
      <h:commandButton id="loadScreenDB"   action="#{schemaManager.loadScreenDB}"   value="Load ScreenDB Data"               styleClass="command" />
    </h:panelGrid>

  </h:form>

</f:subview>