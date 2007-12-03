<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="compoundLibraryContentsImporter">

  <h:panelGroup>
    <%@include file="compoundLibraryContentsUploader.jspf"%>
  </h:panelGroup>

  <h:form id="errorsTableForm" rendered="#{compoundLibraryContentsImporter.hasErrors}">

    <h:outputText value="Import Errors for #{compoundLibraryContentsImporter.uploadedFile.name}"
      styleClass="sectionHeader" />

    <t:dataTable id="importErrorsTable" value="#{compoundLibraryContentsImporter.importErrors}" var="row"
      rows="10" styleClass="standardTable" headerClass="tableHeader" rowClasses="row1,row2"
      columnClasses="">
      <t:column styleClass="column">
        <f:facet name="header">
          <h:outputText value="SD File Record Number" />
        </f:facet>
        <h:outputText value="#{row.recordNumber}" />
      </t:column>
      <t:column styleClass="column">
        <f:facet name="header">
          <h:outputText value="Error" />
        </f:facet>
        <h:outputText value="#{row.errorMessage}" />
      </t:column>
    </t:dataTable>
    <t:dataScroller id="errorsScroller" for="importErrorsTable" firstRowIndexVar="fromRow"
      lastRowIndexVar="toRow" rowsCountVar="rowCount" paginator="true" paginatorMaxPages="10"
      fastStep="10" renderFacetsIfSinglePage="true" styleClass="scroller"
      paginatorActiveColumnClass="scroller_activePage">
      <f:facet name="first">
        <t:graphicImage url="../../images/arrow-first.png" border="1" title="First page"/>
      </f:facet>
      <f:facet name="last">
        <t:graphicImage url="../../images/arrow-last.png" border="1" title="Last page" />
      </f:facet>
      <f:facet name="previous">
        <t:graphicImage url="../../images/arrow-previous.png" border="1" title="Previous page" />
      </f:facet>
      <f:facet name="next">
        <t:graphicImage url="../../images/arrow-next.png" border="1" title="Next page" />
      </f:facet>
      <f:facet name="fastforward">
        <t:graphicImage url="../../images/arrow-fastforward.png" border="1" title="Forward 10 pages" />
      </f:facet>
      <f:facet name="fastrewind">
        <t:graphicImage url="../../images/arrow-fastrewind.png" border="1" title="Back 10 pages" />
      </f:facet>
      <t:outputText value="Showing error #{fromRow} to #{toRow} of #{rowCount}" />
    </t:dataScroller>

  </h:form>

</f:subview>
