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

<f:subview id="well">

  <t:aliasBean alias="#{navigator}" value="#{searchResultsRegistry.searchResults}" >
    <%@ include file="../searchResultsNavPanel.jspf"  %>
  </t:aliasBean>

  <h:form id="wellForm">

    <h:panelGroup rendered="#{wellViewer.displayDone}">
      <h:commandButton id="doneCommand" action="#{wellViewer.done}" value="Done" />
    </h:panelGroup>

    <t:div />

    <h:panelGrid columns="2" styleClass="standardTable">
    
      <h:outputText value="Library:" />
      <t:commandLink
        action="#{wellViewer.showLibrary}"
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
      
      <h:outputText value="Contents:" />
      <t:panelGroup>
        <t:dataList
          id="geneList"
          var="gene"
          value="#{wellViewer.well.genes}"
          layout="simple"
        >
          <t:aliasBean alias="#{controller}" value="#{wellViewer}">
            <%@ include file="geneViewer.jspf" %>
          </t:aliasBean>
        </t:dataList>
        <t:dataList
          id="compoundList"
          var="compound"
          value="#{wellViewer.well.compounds}"
          layout="simple"
        >
          <t:aliasBean alias="#{controller}" value="#{wellViewer}">
            <%@ include file="compoundViewer.jspf" %>
          </t:aliasBean>
        </t:dataList>
      </t:panelGroup>

    </h:panelGrid>
    
  </h:form>

</f:subview>
