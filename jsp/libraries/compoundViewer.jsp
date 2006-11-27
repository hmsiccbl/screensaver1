<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="compoundViewer">

  <t:aliasBean alias="#{navigator}" value="#{compoundViewer.wellSearchResults}" >
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>

  <t:aliasBean alias="#{compound}" value="#{compoundViewer.compound}">
    <t:aliasBean alias="#{controller}" value="#{compoundViewer}">
      <%@ include file="compoundViewer.jspf" %>
    </t:aliasBean>
  </t:aliasBean>

  <t:dataList
    id="wellList"
    var="well"
    value="#{compoundViewer.compound.wells}"
    layout="simple"
    rendered="#{compoundViewer.compound.numWells <= 2}"
  >
    <%@ include file="wellViewer.jspf" %>
  </t:dataList>
  
  <t:dataTable
    id="wellTable"
    var="well"
    value="#{compoundViewer.compound.wells}"
    rendered="#{compoundViewer.compound.numWells > 2}"
    headerClass="alignLeft"
    border="1"
  >
    <h:column>
      <f:facet name="header">
        <t:outputText value="Library" />
      </f:facet>
      <t:commandLink
        action="#{librariesController.viewLibrary}"
        value="#{well.library.shortName}"
      >
        <f:param name="libraryId" value="#{well.library.libraryId}" />
      </t:commandLink>
    </h:column>
    <h:column>
      <f:facet name="header">
        <t:outputText value="Plate" />
      </f:facet>
      <h:outputText value="#{well.plateNumber}" />
    </h:column>
    <h:column>
      <f:facet name="header">
        <t:outputText value="Well" />
      </f:facet>
      <t:commandLink
        action="#{librariesController.viewWell}"
        value="#{well.wellName}"
      >
        <f:param name="wellId" value="#{well.wellId}" />
      </t:commandLink>
    </h:column>
    <h:column>
      <f:facet name="header">
        <t:outputText value="ICCB Number" />
      </f:facet>
      <h:outputText value="#{well.iccbNumber}" />
    </h:column>
    <h:column>
      <f:facet name="header">
        <t:outputText value="Vendor Identifier" />
      </f:facet>
      <h:outputText value="#{well.fullVendorIdentifier}" />
    </h:column>
  </t:dataTable>
    
</f:subview>


