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

  <t:panelGroup>
    <h:form id="libraryContentsForm">
			<h:commandButton value="View Library Contents"
				action="#{libraryViewer.viewLibraryContents}" styleClass="command"
				title="View a list of all the wells in the library" />

			<h:commandButton value="Import Library Contents"
				action="#{libraryViewer.importRNAiLibraryContents}"
				rendered="#{libraryViewer.editable && libraryViewer.isRNAiLibrary}"
			    styleClass="command"
			    title="Import the contents of library wells from an Excel spreadsheet" />

			<h:commandButton value="Import Library Contents"
				action="#{libraryViewer.importCompoundLibraryContents}"
				rendered="#{libraryViewer.editable && libraryViewer.isCompoundLibrary}"
			    styleClass="command"
			    title="Import the contents of library wells from an SD File" />

			<h:commandButton value="Unload Library Contents"
				action="#{libraryViewer.unloadLibraryContents}"
				onclick="javascript: return confirm('Are you sure you want to unload the contents of this library?');"
				rendered="#{libraryViewer.editable && libraryViewer.librarySize > 0}"
				styleClass="command"
				title="Mostly useful for reloading data if a problem occurred on a previous laod" />
		</h:form>
  </t:panelGroup>

  <t:panelGroup rendered="#{libraryViewer.isCompoundLibrary}">
    <h:form id="viewDownloadsForm" style="margin-top: 10px">
      <t:outputText value="Visit the " />
      <t:commandLink action="#{mainController.viewDownloads}" value="Data Downloads page" />
      <t:outputText value=" to download SD Files for the ICCB-L compound libraries." />
    </h:form>
  </t:panelGroup>

</f:subview>


