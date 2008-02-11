<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<f:subview id="schemaManager">

	<h:form id="schemaManagerForm">
		<h:panelGrid columns="1" styleClass="standardTable">
			<h:commandButton id="dropSchema" action="#{schemaManager.dropSchema}"
				value="Drop Schema" styleClass="command" disabled="true" />
			<h:commandButton id="createSchema"
				action="#{schemaManager.createSchema}" value="Create Schema"
				styleClass="command" disabled="true" />
			<h:commandButton id="initializeDatabase"
				action="#{schemaManager.initializeDatabase}"
				value="Initialize Database" styleClass="command" disabled="true" />
			<h:commandButton id="truncateTables"
				action="#{schemaManager.truncateTables}"
				value="Truncate Tables or Create Schema" styleClass="command" disabled="true" />
			<h:commandButton id="grantDeveloperPermissions"
				action="#{schemaManager.grantDeveloperPermissions}"
				value="Grant Developer Permissions" styleClass="command" />
		</h:panelGrid>
	</h:form>

	<t:outputText value="&nbsp;" escape="false" />

	<h:form id="screenDBSynchronizerForm">
		<h:outputText value="ScreenDB Synchronizer:"
			styleClass="sectionHeader" />
		<t:panelGrid columns="2">
			<t:outputText value="ScreenDB server:" />
			<t:inputText id="screenDBServer"
				value="#{schemaManager.screenDBServer}" styleClass="inputText" />
			<t:outputText value="ScreenDB database:" />
			<t:inputText id="screenDBDatabase"
				value="#{schemaManager.screenDBDatabase}" styleClass="inputText" />
			<t:outputText value="ScreenDB username:" />
			<t:inputText id="screenDBUsername"
				value="#{schemaManager.screenDBUsername}" styleClass="inputText" />
			<t:outputText value="ScreenDB password:" />
			<t:inputSecret id="screenDBPassword"
				value="#{schemaManager.screenDBPassword}" styleClass="inputText" />
			<h:commandButton id="screenDBSynchronizerButton"
				action="#{schemaManager.synchronizeScreenDB}" value="Synchronize"
				styleClass="command" />
		</t:panelGrid>
	</h:form>

</f:subview>
