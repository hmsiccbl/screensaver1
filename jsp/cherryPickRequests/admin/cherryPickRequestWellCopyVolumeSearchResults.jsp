<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="cherryPickRequestWellCopyVolumeSearchResultsViewer">

  <t:aliasBean alias="#{searchResults}" value="#{wellCopyVolumeSearchResultsViewer.searchResults}">
    <%@include file="../../searchResults.jspf"%>
  </t:aliasBean>

</f:subview>

