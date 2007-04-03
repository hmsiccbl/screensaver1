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


<f:subview id="screenResultImporter">

  <h:form id="errorsTableForm" rendered="#{screenResultImporter.screenResultParser.hasErrors}">

		<h:outputText value="Import of screen result file \"
			#{screenResultImporter.uploadedFile.name}\" failed for
			screen #{screenResultImporter.screen.screenNumber}"
      styleClass="errorMessage" />

		<t:div/>

    <t:dataTable id="importErrorsTable" value="#{screenResultImporter.importErrors}" var="row"
      rows="20" styleClass="standardTable" headerClass="tableHeader" rowClasses="row1,row2">
      <t:column styleClass="column">
        <f:facet name="header">
          <h:outputText value="Where" />
        </f:facet>
				<h:outputText
					value="#{row.cell.sheetName}:#{row.cell.formattedRowAndColumn}"
					rendered="#{! empty row.cell.sheetName}" />
				<h:outputText value="<workbook>"
					rendered="#{empty row.cell.sheetName}" />
			</t:column>
      <t:column styleClass="column">	
        <f:facet name="header">
          <h:outputText value="Error" />
        </f:facet>
        <h:outputText value="#{row.message}" />
      </t:column>
    </t:dataTable>
    
    <t:dataScroller id="errorsScroller" for="importErrorsTable" firstRowIndexVar="fromRow"
      lastRowIndexVar="toRow" rowsCountVar="rowCount" paginator="true" paginatorMaxPages="10"
      fastStep="10" renderFacetsIfSinglePage="true" styleClass="scroller"
      paginatorActiveColumnClass="scroller_activePage">
      <f:facet name="first">
        <t:graphicImage url="/images/arrow-first.png" border="0" title="First page" />
      </f:facet>
      <f:facet name="last">
        <t:graphicImage url="/images/arrow-last.png" border="0" title="Last page"/>
      </f:facet>
      <f:facet name="previous">
        <t:graphicImage url="/images/arrow-previous.png" border="0" title="Previous page" />
      </f:facet>
      <f:facet name="next">
        <t:graphicImage url="/images/arrow-next.png" border="0" title="Next page" />
      </f:facet>
      <f:facet name="fastforward">
        <t:graphicImage url="/images/arrow-fastforward.png" border="0" title="Forward 10 pages"/>
      </f:facet>
      <f:facet name="fastrewind">
        <t:graphicImage url="/images/arrow-fastrewind.png" border="0" title="Back 10 pages" />
      </f:facet>
      <t:outputText value="Showing error #{fromRow}..#{toRow} of #{rowCount}" styleClass="label" />
    </t:dataScroller>

  </h:form>
  
  <h:form id="commandForm">
    <h:commandButton id="cancel"
      action="#{screenResultImporter.cancel}"
      immediate="true"
      value="Cancel"
      styleClass="command" />
    <h:commandButton id="downloadCommand"
      actionListener="#{screenResultImporter.downloadErrorAnnotatedWorkbookListener}"
      value="View Error-Annotated Workbook"
      rendered="#{screenResultImporter.screenResultParser.hasErrors}" styleClass="command" />
	</h:form>
	
	<t:div/>

	<h:panelGroup>
		<%@include file="screenResultUploader.jspf"%>
	</h:panelGroup>

</f:subview>
