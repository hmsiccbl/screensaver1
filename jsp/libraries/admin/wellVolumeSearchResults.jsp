<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellCopyVolumeSearchResultsViewer">


  <t:aliasBean alias="#{searchResults}" value="#{wellVolumeSearchResultsViewer.searchResults}">

		<t:buffer into="#{searchResultsHeader}">
			<t:panelGrid columns="1">
				<t:commandButton value="View Well and Copy"
					action="#{wellVolumeSearchResultsViewer.viewWellCopyVolumeSearchResults}"
					styleClass="command" />
			</t:panelGrid>
		</t:buffer>

		<%@include file="../../searchResults.jspf"%>

	</t:aliasBean>

</f:subview>


