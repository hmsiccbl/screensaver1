<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
    
<f:subview id="wellViewer">

  <t:aliasBean alias="#{navigator}" value="#{wellViewer.wellSearchResults}">
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>
  
  <t:aliasBean alias="#{nameValueTable}" value="#{wellViewer.wellNameValueTable}">
    <%@ include file="../nameValueTable.jspf" %>
  </t:aliasBean>

</f:subview>
