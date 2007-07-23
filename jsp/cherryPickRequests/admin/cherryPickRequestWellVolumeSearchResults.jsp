<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%-- This page, which is just an include for the wellVolumeSearchResults page, exists to handle web.xml security constraints --%>

<f:subview id="cherryPickRequestWellVolumeSearchResultsViewer">

	<%@include file="../../libraries/admin/wellVolumeSearchResults.jsp"%>

</f:subview>


