<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="reagentViewer">

	<t:saveState value="#{reagentViewer.showNavigationBar}" />

	<t:panelGroup rendered="#{reagentViewer.showNavigationBar}" >
	  <t:aliasBean alias="#{navigator}" value="#{reagentsBrowser}">
			<h:form id="navPanelForm">
				<%@ include file="../searchResultsNavPanel.jspf" %>
			</h:form>
  	</t:aliasBean>
  </t:panelGroup>

  <t:aliasBean alias="#{nameValueTable}" value="#{reagentViewer.nameValueTable}">
    <%@ include file="../nameValueTable.jspf" %>
  </t:aliasBean>

  <t:panelGrid style="margin-top: 10px;">
    <t:panelGroup rendered="#{! empty reagentViewer.well.compounds}">
      <h:form id="reagentContentsDownloadForm">
		  	<h:commandButton value="Dowload SD File"
			  	action="#{reagentViewer.downloadSDFile}" styleClass="command"
				  title="Download the contents of the reagent as an SD File" />
      </h:form>
    </t:panelGroup>
  </t:panelGrid>

  <t:panelGroup rendered="#{! empty reagentViewer.well.compounds}">
    <%@ include file="structureImageNotice.jspf" %>
  </t:panelGroup>

</f:subview>
