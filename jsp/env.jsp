<%@include file="header.jspf"%>

<h1>
  Screensaver Environment Information
</h1>

<f:view>

  <t:outputLabel for="dbConfigPanel" value="Database Connection Settings" styleClass="sectionHeader"/>
  <h:panelGrid id="dbConfigPanel" columns="1">
    <h:outputText value="Host=#{envInfo.host}" />
    <h:outputText value="Database=#{envInfo.db}" />
    <h:outputText value="User=#{envInfo.user}" />
    <h:outputText value="URL=#{envInfo.url}"/>
  </h:panelGrid>

  <p/>
  
  <t:outputLabel for="requestParamsTable" value="Request Parameters" styleClass="sectionHeader"/>
  <t:dataTable id="requestParamsTable" value="#{envInfo.requestParamsModel}" var="row" styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <p/>

  <t:outputLabel for="sessionParamsTable" value="Session Parameters" styleClass="sectionHeader"/>
  <t:dataTable id="sessionParamsTable" value="#{envInfo.sessionParamsModel}" var="row" styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <p/>

  <t:outputLabel for="applicationParamsTable" value="Application Parameters" styleClass="sectionHeader"/>
  <t:dataTable id="applicationParamsTable" value="#{envInfo.applicationParamsModel}" var="row" styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

  <p/>

  <t:outputLabel for="envVarsTable" value="Environment Variables" styleClass="sectionHeader"/>
  <t:dataTable id="envVarsTable" value="#{envInfo.envTableModel}" var="row" styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>
  
  <p/>

  <t:outputLabel for="sysPropsTable" value="Java Systems Properties" styleClass="sectionHeader"/>
  <t:dataTable id="sysPropsTable" value="#{envInfo.sysPropsTableModel}" var="row" styleClass="standardTable" rowClasses="row1,row2">
    <t:column>
      <f:facet name="header">Name</f:facet>
      <t:outputText value="#{row.name}"></t:outputText>
    </t:column>
    <t:column>
      <f:facet name="header">Value</f:facet>
      <t:outputText value="#{row.value}"></t:outputText>
    </t:column>
  </t:dataTable>

</f:view>

<%@include file="footer.jspf"%>
