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


	<%--
    TODO: fix up this temporary hack with the panelGroup surrounding the searchResultsNavPanel.
    the search results that the well viewer is in should
    be set somewhere and passed to the WellViewerController. if the caller to "showWells" does
    not set the search results with the reigstry, then the wrong search results nav bar is
    displayed. correct solution is to fix this across the board by putting the "showWells" stuff
    in a separate "controller", requiring the search results as arg. i will handle this later,
    but for now, i will not render this if there are no search results, preventing page errors
    from Find Wells
    --%>
    
<f:subview id="well">

  <t:panelGroup
    rendered="#{searchResultsRegistry.searchResults != null}"
  >
    <t:aliasBean
      alias="#{navigator}"
      value="#{searchResultsRegistry.searchResults}"
     >
      <%@ include file="../searchResultsNavPanel.jspf" %>
    </t:aliasBean>
  </t:panelGroup>
  
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

    </h:panelGrid>
      
    <t:dataList
      id="geneList"
      var="gene"
      value="#{wellViewer.well.genes}"
      layout="simple"
    >
	  <t:outputText value="Gene:" />
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
      <t:panelGrid columns="2">
        <t:outputText value="Compound:" />
        <t:aliasBean alias="#{controller}" value="#{wellViewer}">
          <%@ include file="compoundViewer.jspf" %>
        </t:aliasBean>
      </t:panelGrid>
    </t:dataList>
    
  </h:form>

</f:subview>
