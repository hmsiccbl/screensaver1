<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
    
<f:subview id="well">

  <t:aliasBean alias="#{navigator}" value="#{wellViewer.wellSearchResults}">
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>
  
  <h:form id="wellForm">

    <h:panelGrid columns="2" styleClass="standardTable">
    
      <h:outputText value="Library:" />
      <t:commandLink
        action="#{wellViewer.viewLibrary}"
        value="#{wellViewer.well.library.libraryName}"
      />
      
      <h:outputText value="Plate:" />
      <h:outputText value="#{wellViewer.well.plateNumber}" />
      
      <h:outputText value="Well:" />
      <h:outputText value="#{wellViewer.well.wellName}" />
            
      <h:outputText value="ICCB Number:" />
      <h:outputText value="#{wellViewer.well.iccbNumber}" />
            
      <h:outputText value="Vendor Identifier:" />
      <h:outputText value="#{wellViewer.well.vendorIdentifier}" />

    </h:panelGrid>
      
    <t:dataList
      id="geneList"
      var="gene"
      value="#{wellViewer.well.genes}"
      layout="simple"
    >
      <t:panelGrid columns="2" styleClass="standardTable">
        <t:outputText value="Gene:" />
        <t:aliasBean alias="#{controller}" value="#{wellViewer}">
          <%@ include file="geneViewer.jspf" %>
        </t:aliasBean>
      </t:panelGrid>
    </t:dataList>
    
    <t:dataList
      id="compoundList"
      var="compound"
      value="#{wellViewer.well.compounds}"
      layout="simple"
    >
      <t:panelGrid columns="2" styleClass="standardTable">
        <t:outputText value="Compound:" />
        <t:aliasBean alias="#{controller}" value="#{wellViewer}">
          <t:aliasBean alias="#{compound}" value="#{compound}">
            <%@ include file="compoundViewer.jspf" %>
          </t:aliasBean>
        </t:aliasBean>
      </t:panelGrid>
    </t:dataList>
    
  </h:form>

</f:subview>
