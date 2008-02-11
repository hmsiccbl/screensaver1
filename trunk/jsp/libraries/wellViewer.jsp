<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellViewer">

	<t:aliasBean alias="#{reagentViewer}" value="#{wellViewer}">
		<%@ include file="reagentViewer.jsp"%>
	</t:aliasBean>

</f:subview>
