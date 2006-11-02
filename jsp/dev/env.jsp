<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="env">

	<t:outputText value="Application Metadata" styleClass="sectionHeader" />
	<h:panelGrid id="metadata" columns="2" styleClass="standardTable"
		rowClasses="row1,row2">
		<t:outputText value="Name" />
		<t:outputText value="#{envInfo.applicationName}" />
		<t:outputText value="Version" />
		<t:outputText value="#{envInfo.applicationVersion}" />
		<t:outputText value="Build" />
		<t:outputText value="#{envInfo.applicationBuildNumber}" />
	</h:panelGrid>

	<t:outputText value="Database Connection Settings" styleClass="sectionHeader" />
	<h:panelGrid id="dbConfigPanel" columns="2" styleClass="standardTable"
		rowClasses="row1,row2">
		<h:outputText value="Host" />
		<h:outputText value="#{envInfo.host}" />
		<h:outputText value="Database" />
		<h:outputText value="#{envInfo.db}" />
		<h:outputText value="User" />
		<h:outputText value="#{envInfo.user}" />
		<h:outputText value="URL" />
		<h:outputText value="#{envInfo.url}" />
	</h:panelGrid>

	<t:outputText value="User Security" styleClass="sectionHeader" />
  <t:dataTable id="userSecurityTable" value="#{envInfo.userSecurityTableModel}" var="row"
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

