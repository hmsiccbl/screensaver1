<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
    
<f:subview id="wellViewer">

  <t:aliasBean alias="#{navigator}" value="#{wellViewer.wellSearchResults}">
		<h:form id="navPanelForm">
			<%@ include file="../searchResultsNavPanel.jspf" %>
		</h:form>
  </t:aliasBean>
  
  <t:aliasBean alias="#{nameValueTable}" value="#{wellViewer.wellNameValueTable}">
    <%@ include file="../nameValueTable.jspf" %>
  </t:aliasBean>

</f:subview>
