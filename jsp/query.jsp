<%@ include file="headers.inc"%>

<f:subview id="query">
  <h:form id="queryForm">
    <t:panelGrid columns="2">
      <t:outputLabel for="stockPlateNumber" value="Stock Plate Number:" styleClass="inputLabel" />
      <t:inputText id="stockPlateNumber" value="#{query.stockPlateNumber}" size="5" styleClass="input" />
      <t:outputLabel for="wellName" value="Well:" styleClass="inputLabel" />
      <t:inputText id="wellName" value="#{query.wellName}" size="3" styleClass="input" />
    </t:panelGrid>
    <t:panelGroup>
      <t:commandButton value="Search" styleClass="command" />
    </t:panelGroup>
  </h:form>

</f:subview>
