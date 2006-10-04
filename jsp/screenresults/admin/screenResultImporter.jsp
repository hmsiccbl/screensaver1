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

  <h:panelGroup>
    <%@include file="screenResultUploader.jspf"%>
  </h:panelGroup>

  <h:form id="errorsTableForm" rendered="#{screenResultImporter.screenResultParser.hasErrors}">

    <h:outputText value="Import Errors for #{screenResultImporter.uploadedFile.name}"
      styleClass="sectionHeader" />

    <t:dataTable id="importErrorsTable" value="#{screenResultImporter.importErrors}" var="row"
      rows="10" styleClass="standardTable" headerClass="tableHeader" rowClasses="row1,row2"
      columnClasses="">
      <t:column styleClass="column">
        <f:facet name="header">
          <h:outputText value="Worksheet" />
        </f:facet>
        <h:outputText value="#{row.cell.sheetName}" />
      </t:column>
      <t:column styleClass="column">
        <f:facet name="header">
          <h:outputText value="Cell" />
        </f:facet>
        <h:outputText value="#{row.cell.formattedRowAndColumn}" />
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
        <t:graphicImage url="/images/arrow-first.gif" border="1" />
      </f:facet>
      <f:facet name="last">
        <t:graphicImage url="/images/arrow-last.gif" border="1" />
      </f:facet>
      <f:facet name="previous">
        <t:graphicImage url="/images/arrow-previous.gif" border="1" />
      </f:facet>
      <f:facet name="next">
        <t:graphicImage url="/images/arrow-next.gif" border="1" />
      </f:facet>
      <f:facet name="fastforward">
        <t:graphicImage url="/images/arrow-fastforward.gif" border="1" />
      </f:facet>
      <f:facet name="fastrewind">
        <t:graphicImage url="/images/arrow-fastrewind.gif" border="1" />
      </f:facet>
      <t:outputText value="Showing error #{fromRow} to #{toRow} of #{rowCount}" />
    </t:dataScroller>

  </h:form>
  
  <h:form id="commandForm">
    <h:commandButton id="downloadCommand"
      actionListener="#{screenResultImporter.downloadErrorAnnotatedWorkbookListener}"
      value="View Error-Annotated Workbook"
      rendered="#{screenResultImporter.screenResultParser.hasErrors}" styleClass="command" />
  </h:form>



</f:subview>
