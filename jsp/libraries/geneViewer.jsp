<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="geneViewer">

  <t:aliasBean alias="#{navigator}" value="#{geneViewer.wellSearchResults}">
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>

	<t:panelGroup rendered="#{! empty geneViewer.gene}">
		<t:aliasBean alias="#{nameValueTable}" value="#{geneViewer.geneNameValueTable}">
			<%@ include file="../nameValueTable.jspf" %>
		</t:aliasBean>

		<t:aliasBean alias="#{wells}" value="#{geneViewer.gene.wells}">
			<%@ include file="wellTable.jspf" %>
		</t:aliasBean>
	</t:panelGroup>

	<t:panelGroup rendered="#{empty geneViewer.gene}">
		<t:outputText
			value="There are no genes in well #{compoundViewer.parentWellOfInterest.wellKey}"
			styleClass="label" />
	</t:panelGroup>

</f:subview>


