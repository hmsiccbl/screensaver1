<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="geneViewer">

  <t:aliasBean alias="#{navigator}" value="#{geneViewer.wellSearchResults}">
		<h:form id="navPanelForm">
			<%@ include file="../searchResultsNavPanel.jspf" %>
		</h:form>
  </t:aliasBean>

	<t:panelGrid rendered="#{! empty geneViewer.gene}" columns="1">
		<t:aliasBean alias="#{nameValueTable}" value="#{geneViewer.geneNameValueTable}">
			<%@ include file="../nameValueTable.jspf" %>
		</t:aliasBean>
		
    <t:div style="margin-top: 15px; margin-left: 20px;">
      <t:outputText
        styleClass="subsectionHeader"
        value="Wells in which Silencing Reagents for this Gene are Found"
      />
			<t:aliasBean alias="#{wells}" value="#{geneViewer.gene.wells}">
				<%@ include file="wellTable.jspf" %>
			</t:aliasBean>
		</t:div>
	</t:panelGrid>

	<t:panelGroup rendered="#{empty geneViewer.gene}">
		<t:outputText
			value="There are no genes in well #{compoundViewer.parentWellOfInterest.wellKey}"
			styleClass="label" />
	</t:panelGroup>

</f:subview>


