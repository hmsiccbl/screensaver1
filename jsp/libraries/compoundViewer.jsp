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

</f:subview>


