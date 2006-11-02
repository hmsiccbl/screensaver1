<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="geneViewer">

  <t:aliasBean alias="#{navigator}" value="#{geneViewer.wellSearchResults}">
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>

  <t:aliasBean alias="#{gene}" value="#{geneViewer.gene}">
    <t:aliasBean alias="#{controller}" value="#{geneViewer}">
      <%@ include file="geneViewer.jspf" %>
    </t:aliasBean>
  </t:aliasBean>

</f:subview>


