<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
    
<f:subview id="well">

  <t:aliasBean alias="#{navigator}" value="#{wellViewer.wellSearchResults}">
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>
  
  <t:aliasBean alias="#{well}" value="#{wellViewer.well}">
    <%@ include file="wellViewer.jspf" %>
  </t:aliasBean>
  
  <h:form id="wellForm">
      
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
        <t:aliasBean alias="#{compound}" value="#{compound}">
          <%@ include file="compoundViewer.jspf" %>
        </t:aliasBean>
      </t:aliasBean>
    </t:dataList>
    
  </h:form>

</f:subview>
