<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
    
<f:subview id="wellViewer">

  <t:aliasBean alias="#{navigator}" value="#{wellViewer.wellSearchResults}">
		<h:form id="navPanelForm">
			<%@ include file="../searchResultsNavPanel.jspf" %>
		</h:form>
  </t:aliasBean>
  
  <t:aliasBean alias="#{nameValueTable}" value="#{wellViewer.wellNameValueTable}">
    <%@ include file="../nameValueTable.jspf" %>
  </t:aliasBean>

  <t:panelGrid style="margin-top: 10px;">
    <t:panelGroup rendered="#{! empty wellViewer.well.compounds}">
      <h:form id="wellContentsDownloadForm">
		  	<h:commandButton value="Dowload SD File"
			  	action="#{wellViewer.downloadWellSDFile}" styleClass="command"
				  title="Download the contents of the well as an SD File" />
      </h:form>
    </t:panelGroup>
  </t:panelGrid>

  <t:panelGroup rendered="#{! empty wellViewer.well.compounds}">
    <%@ include file="structureImageNotice.jspf" %>
  </t:panelGroup>

</f:subview>
