<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="compoundViewer">

  <t:aliasBean alias="#{navigator}" value="#{compoundViewer.wellSearchResults}" >
    <%@ include file="../searchResultsNavPanel.jspf" %>
  </t:aliasBean>

	<t:panelGroup rendered="#{! empty compoundViewer.compound}">
		<t:aliasBean alias="#{nameValueTable}"
			value="#{compoundViewer.compoundNameValueTable}">
			<%@ include file="../nameValueTable.jspf"%>
		</t:aliasBean>

		<t:aliasBean alias="#{wells}" value="#{compoundViewer.compound.wells}">
			<%@ include file="wellTable.jspf"%>
		</t:aliasBean>
	</t:panelGroup>
	
	<t:panelGroup rendered="#{empty compoundViewer.compound}">
		<t:outputText
			value="There are no compounds in well #{compoundViewer.parentWellOfInterest.wellKey}"
			styleClass="label" />
	</t:panelGroup>
</f:subview>


