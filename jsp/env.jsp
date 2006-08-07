<%@ include file="headers.inc" %>

<f:subview id="env">

  <t:outputText value="Database Connection Settings" styleClass="sectionHeader" />
  <h:panelGrid id="dbConfigPanel" columns="1">
    <h:outputText value="Host=#{envInfo.host}" />
    <h:outputText value="Database=#{envInfo.db}" />
    <h:outputText value="User=#{envInfo.user}" />
    <h:outputText value="URL=#{envInfo.url}" />
  </h:panelGrid>

  <t:outputText value="Cookies" styleClass="sectionHeader" />
  <t:dataTable id="cookiesTable" value="#{envInfo.cookiesTableModel}" var="row"
    styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <t:outputText value="Request Parameters" styleClass="sectionHeader" />
  <t:dataTable id="requestParamsTable" value="#{envInfo.requestParamsModel}" var="row"
    styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <t:outputText value="Session Parameters" styleClass="sectionHeader" />
  <t:dataTable id="sessionParamsTable" value="#{envInfo.sessionParamsModel}" var="row"
    styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <t:outputText value="Application Parameters" styleClass="sectionHeader" />
  <t:dataTable id="applicationParamsTable" value="#{envInfo.applicationParamsModel}" var="row"
    styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <t:outputText value="Environment Variables" styleClass="sectionHeader" />
  <t:dataTable id="envVarsTable" value="#{envInfo.envTableModel}" var="row"
    styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <t:outputText value="Java Systems Properties" styleClass="sectionHeader" />
  <t:dataTable id="sysPropsTable" value="#{envInfo.sysPropsTableModel}" var="row"
    styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <h:form>
    <t:commandButton action="#{envInfo.throwAnException}" value="Throw an exception!" />
  </h:form>

</f:subview>

