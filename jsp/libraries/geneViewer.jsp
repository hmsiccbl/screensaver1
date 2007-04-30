<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="geneViewer">

  <t:aliasBean alias="#{navigator}" value="#{geneViewer.wellSearchResults}">
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>

	<t:panelGrid rendered="#{! empty geneViewer.gene}" columns="1">
		<t:aliasBean alias="#{nameValueTable}" value="#{geneViewer.geneNameValueTable}">
			<%@ include file="../nameValueTable.jspf" %>
		</t:aliasBean>

		<t:div />
		<t:div />
		
		<t:panelGroup>
			<t:outputText value="Wells in which silencing reagents for this gene are found:" />
			<t:aliasBean alias="#{wells}" value="#{geneViewer.gene.wells}">
				<%@ include file="wellTable.jspf" %>
			</t:aliasBean>
		</t:panelGroup>
	</t:panelGrid>

	<t:panelGroup rendered="#{empty geneViewer.gene}">
		<t:outputText
			value="There are no genes in well #{compoundViewer.parentWellOfInterest.wellKey}"
			styleClass="label" />
	</t:panelGroup>

</f:subview>


