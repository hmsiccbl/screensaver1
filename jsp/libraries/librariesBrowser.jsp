<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="librariesBrowser">
	<h:form id="librariesBrowserForm">
		<t:aliasBean alias="#{searchResults}" value="#{librariesBrowser}">
			<%@include file="../searchResults.jspf"%>
		</t:aliasBean>
	</h:form>
</f:subview>


