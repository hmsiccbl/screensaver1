<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="libraryViewer">

  <t:aliasBean alias="#{navigator}" value="#{libraryViewer.librarySearchResults}" >
    <%@ include file="../searchResultsNavPanel.jspf"  %>
  </t:aliasBean>

  <t:aliasBean alias="#{nameValueTable}" value="#{libraryViewer.libraryNameValueTable}" >
    <%@ include file="../nameValueTable.jspf"  %>
  </t:aliasBean>

  <t:panelGroup rendered="#{libraryViewer.editable}">
    <h:form id="libraryContentsForm">
			<h:commandButton value="View Library Contents"
				action="#{libraryViewer.viewLibraryContents}" styleClass="command" />

			<h:commandButton value="Import Library Contents"
				action="#{libraryViewer.importRNAiLibraryContents}"
				rendered="#{libraryViewer.isRNAiLibrary}" styleClass="command" />

			<h:commandButton value="Import Library Contents"
				action="#{libraryViewer.importCompoundLibraryContents}"
				rendered="#{libraryViewer.isCompoundLibrary}" styleClass="command" />

			<h:commandButton value="Unload Library Contents"
				action="#{libraryViewer.unloadLibraryContents}"
				onclick="javascript: return confirm('Are you sure you want to unload the contents of this library?');"
				rendered="#{libraryViewer.librarySize > 0}" styleClass="command" />
		</h:form>
  </t:panelGroup>

</f:subview>


