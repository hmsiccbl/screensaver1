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

<!-- 
TODO:
- show parent screen's number (linked), and some basic info, like title, perhaps
- move screen attributes out of data table and into independent UI components (like ScreenViewer)
- fix View Screen button
 -->

<f:subview id="screenResultViewer">

  <h:form id="commandForm">

    <t:panelGroup>
			<t:commandButton action="#{screenResultViewer.viewScreen}"
				value="View Screen" styleClass="command" />
			<t:commandButton action="#{screenResultViewer.download}"
				value="Download" rendered="#{!empty screenResultViewer.screenResult}" styleClass="command" />
			<t:commandButton action="#{screenResultViewer.delete}" value="Delete"
				onclick="javascript: return confirm('Delete this screen result permanently?');"
				styleClass="command"
				rendered="#{! screenResultViewer.readOnly && !empty screenResultViewer.screenResult}" />
			<t:commandButton action="#{screenResultViewer.viewHeatMaps}"
				value="View Heat Maps" rendered="#{!empty screenResultViewer.screenResult}" styleClass="command" />
		</t:panelGroup>

  </h:form>

	<t:panelGroup rendered="#{!screenResultViewer.readOnly}">
		<%@include file="admin/screenResultUploader.jspf"%>
		<%--@ include file="admin/cherryPickUploader.jspf" --%>
	</t:panelGroup>
	

	<t:panelGroup rendered="#{!screenResultViewer.readOnly && empty screenResultViewer.screenResult}">
		<t:outputText value="No screen result loaded." styleClass="sectionHeader"/>
	</t:panelGroup>
	
	<h:form id="dataForm" rendered="#{!empty screenResultViewer.screenResult}">

    <t:panelGrid columns="2" >
      <t:outputLabel for="screenResultDateCreated" value="Date Created" styleClass="keyColumn" />
      <t:outputText id="screenResultDateCreated"
        value="#{screenResultViewer.screenResult.dateCreated}" />

      <t:outputLabel for="screenResultReplicateCount" value="Replicates" styleClass="keyColumn" />
      <t:outputText id="screenResultReplicateCount"
        value="#{screenResultViewer.screenResult.replicateCount}" />

			<t:outputLabel for="screenResultDateCreated" value="Shareable" styleClass="keyColumn" />
			<t:outputText id="screenResultIsShareable"
        value="#{screenResultViewer.screenResult.shareable}" 
        visibleOnUserRole="developer" />
    </t:panelGrid>

    <t:div />

    <t:panelGrid columns="1">
      <t:outputLabel for="dataHeadersList" value="Show selected data headers:" />
      <t:selectManyListbox id="dataHeadersList"
        value="#{screenResultViewer.selectedDataHeaderNames}"
        valueChangeListener="#{screenResultViewer.selectedDataHeadersListener}" styleClass="input">
        <f:selectItems id="dataHeaders" value="#{screenResultViewer.dataHeaderSelectItems}" />
      </t:selectManyListbox>
      <t:panelGroup>
        <t:commandButton id="updateButton1" value="Update" action="#{screenResultViewer.update}"
          styleClass="command" />
        <t:commandButton id="allDataHeadersButton" value="All"
          action="#{screenResultViewer.showAllDataHeaders}" styleClass="command" />
      </t:panelGroup>
    </t:panelGrid>

    <t:panelGrid columns="1">
      <t:panelGroup>
        <t:outputLabel for="metadataTable" value="Data Headers" styleClass="sectionHeader" />
        <f:verbatim>&nbsp;(</f:verbatim>
        <t:selectBooleanCheckbox id="showMetadataTableCheckbox"
          value="#{screenResultViewer.showMetadataTable}"
          valueChangeListener="#{screenResultViewer.showTableOptionListener}"
          onclick="javascript:document.getElementById('screenResultViewer:dataForm:updateButton1').click()" />
        <t:outputLabel for="showMetadataTableCheckbox" value="show" />
        <f:verbatim>)</f:verbatim>
      </t:panelGroup>

      <t:dataTable id="metadataTable" value="#{screenResultViewer.metadata}" var="row"
        rendered="#{screenResultViewer.showMetadataTable}" styleClass="standardTable"
        headerClass="tableHeader" rowClasses="row1,row2">
        <t:column styleClass="keyColumn">
          <f:facet name="header">
            <t:outputText value="Property" />
          </f:facet>
          <t:outputText value="#{row.rowLabel}" />
        </t:column>
        <t:columns value="#{screenResultViewer.dataHeaderColumnModel}" var="columnName"
          styleClass="column">
          <f:facet name="header">
            <t:outputText value="#{columnName}" />
          </f:facet>
          <t:outputText value="#{screenResultViewer.metadataCellValue}" />
        </t:columns>
      </t:dataTable>
    </t:panelGrid>

    <t:panelGrid columns="1">
      <t:panelGroup>
        <t:outputLabel for="rawDataTable" value="Data" styleClass="sectionHeader" />
        <f:verbatim>&nbsp;(</f:verbatim>
        <t:selectBooleanCheckbox id="showRawDataTableCheckbox"
          value="#{screenResultViewer.showRawDataTable}"
          valueChangeListener="#{screenResultViewer.showTableOptionListener}"
          onclick="javascript:document.getElementById('screenResultViewer:dataForm:updateButton1').click()" />
        <t:outputLabel for="showRawDataTableCheckbox" value="show" />
        <f:verbatim>)</f:verbatim>
      </t:panelGroup>

      <t:dataTable id="rawDataTable" binding="#{screenResultViewer.dataTable}"
        value="#{screenResultViewer.rawData}" var="row" rows="10"
        rendered="#{screenResultViewer.showRawDataTable}" styleClass="standardTable"
        headerClass="tableHeader" rowClasses="row1,row2">
        <t:column styleClass="keyColumn">
          <f:facet name="header">
            <t:outputText value="Plate" />
          </f:facet>
          <t:outputText value="#{row.well.plateNumber}" />
        </t:column>
        <t:column styleClass="keyColumn">
          <f:facet name="header">
            <t:outputText value="Well" />
          </f:facet>
          <t:commandLink action="#{screenResultViewer.showWell}">
            <%-- TODO: f:param name="wellIdParam" value="#{row.well.wellId} "/ --%>
            <f:param name="wellIdParam" value="#{row.well.wellName} " />
            <t:outputText value="#{row.well.wellName}" />
          </t:commandLink>
        </t:column>
        <t:columns value="#{screenResultViewer.dataHeaderColumnModel}" var="columnName"
          styleClass="column">
          <f:facet name="header">
            <t:outputText value="#{columnName}" />
          </f:facet>
          <t:outputText value="#{screenResultViewer.rawDataCellValue}" />
        </t:columns>
      </t:dataTable>
    </t:panelGrid>
  </h:form>

  <h:form id="navigationForm" rendered="#{!empty screenResultViewer.screenResult}">
    <t:panelGroup rendered="#{screenResultViewer.showRawDataTable}">
      <t:commandButton id="updateButton2" value="Update" action="#{screenResultViewer.update}"
        style="display: none" />
      <t:outputLabel for="plateNumber" value="Jump to plate:" />
      <t:selectOneMenu id="plateNumber" value="#{screenResultViewer.plateNumber}"
        binding="#{screenResultViewer.plateNumberInput}"
        onchange="javascript:document.getElementById('screenResultViewer:navigationForm:updateButton2').click()"
        valueChangeListener="#{screenResultViewer.plateNumberListener}"
        converter="PlateNumberSelectItemConverter" styleClass="input">
        <f:selectItems value="#{screenResultViewer.plateSelectItems}" />
      </t:selectOneMenu>
      <t:commandButton id="prevPageCommand" action="#{screenResultViewer.prevPage}" value="Prev"
        image="/images/arrow-previous.gif" styleClass="command" />
      <t:commandButton id="nextPageCommand" action="#{screenResultViewer.nextPage}" value="Next"
        image="/images/arrow-next.gif" styleClass="command" />
      <t:outputLabel id="rowLabel" value="Row" for="firstDisplayedRowNumber" />
      <t:inputText id="firstDisplayedRowNumber"
        value="#{screenResultViewer.firstDisplayedRowNumber}"
        binding="#{screenResultViewer.firstDisplayedRowNumberInput}"
        valueChangeListener="#{screenResultViewer.firstDisplayedRowNumberListener}" size="6"
        styleClass="input">
        <f:validateLongRange minimum="1" maximum="#{screenResultViewer.rawDataSize}" />
      </t:inputText>
      <t:outputLabel id="rowRange" value="#{screenResultViewer.rowRangeText}"
        for="firstDisplayedRowNumber" />
    </t:panelGroup>
  </h:form>
</f:subview>
