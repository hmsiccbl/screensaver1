<%@ page contentType="text/html;charset=UTF-8" language="java"%>

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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<f:subview id="heatMapViewer">

  <h:form id="dataForm">

    <t:panelGroup>
      <t:commandButton id="doneCommand" action="done" value="Done" styleClass="command" />
    </t:panelGroup>

    <t:div />

    <t:panelGrid columns="2">
      <t:outputLabel for="screenNumber" value="Screen Number" styleClass="keyColumn" />
      <t:outputText id="screenNumber" value="#{heatMapViewer.screenResult.screen.screenNumber}" />
      <t:outputLabel for="screenTitle" value="Screen Title" styleClass="keyColumn" />
      <t:outputText id="screenTitle" value="#{heatMapViewer.screenResult.screen.title}" />
      <t:outputLabel for="screenResultDateCreated" value="Screen Result Created" styleClass="keyColumn" />
      <t:outputText id="screenResultDateCreated" value="#{heatMapViewer.screenResult.dateCreated}" />
    </t:panelGrid>

    <t:div />

    <t:panelGrid columns="3">

			<t:outputLabel for="plateNumbers" value="Show plates: " styleClass="inputLabel" />
			<t:outputLabel for="dataHeader" value="For data header: " styleClass="inputLabel" />
			<t:outputLabel for="computationType" value="Computation: " styleClass="inputLabel" />

			<t:selectManyListbox id="plateNumbers"
				value="#{heatMapViewer.plateNumbers}" converter="IntegerConverter"
				size="5" styleClass="input">
				<f:selectItems value="#{heatMapViewer.plateSelectItems}" />
			</t:selectManyListbox>
			<%--t:commandButton id="allPlatesButton" value="All"
				action="#{heatMapViewer.showAllPlates}" styleClass="command" /--%>

			<t:selectOneMenu id="dataHeader"
				value="#{heatMapViewer.resultValueTypeId}"
				converter="IntegerConverter" styleClass="input">
				<f:selectItems value="#{heatMapViewer.dataHeaderSelectItems}" />
			</t:selectOneMenu>

			<t:selectOneRadio id="computationType" layout="pageDirection"
				value="#{heatMapViewer.normalizationType}"
				converter="NormalizationTypeConverter" styleClass="input">
				<f:selectItems value="#{heatMapViewer.normalizationTypeSelectItems}" />
			</t:selectOneRadio>
    </t:panelGrid>

		<t:commandButton id="updateButton" value="View"
			action="#{heatMapViewer.update}" styleClass="command" />

		<%--t:dataTable id="heatMapsTable" value="#{heatMapViewer.plates}" var="plate" --%>

		<t:dataTable id="heatMapTable"
			value="#{heatMapViewer.heatMapDataModel}" var="plateRow"
			rowIndexVar="plateRowIndex" renderedIfEmpty="false"
			styleClass="heatMapTable" headerClass="tableHeader"
			preserveDataModel="false" footerClass="tableFooter">
			<t:column styleClass="keyColumn">
				<f:facet name="header">
					<t:outputText value="Row" />
				</f:facet>
				<t:outputText value="#{plateRowIndex + 1}" />
			</t:column>
			<t:columns value="#{heatMapViewer.heatMapColumnDataModel}"
				var="columnIndex"
				style="background-color: #{heatMapViewer.heatMapCell.hexColor}">
				<f:facet name="header">
					<t:outputText value="#{columnIndex + 1}" styleClass="keyColumn" />
				</f:facet>
				<f:facet name="footer">
					<t:outputText value="#{columnIndex + 1}" styleClass="keyColumn" />
				</f:facet>
				<t:outputText value="#{heatMapViewer.heatMapCell.value}" />
			</t:columns>
			<t:column styleClass="keyColumn">
				<f:facet name="header">
					<t:outputText value="Row" />
				</f:facet>
				<t:outputText value="#{plateRowIndex + 1}" />
			</t:column>
		</t:dataTable>

		<%--/t:dataTable--%>
		
	</h:form>

</f:subview>
