<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellSearchResultsViewer">

  <t:aliasBean alias="#{searchResults}" value="#{wellSearchResultsViewer.searchResults}">
    <%@include file="../searchResults.jspf"%>
  </t:aliasBean>

</f:subview>


