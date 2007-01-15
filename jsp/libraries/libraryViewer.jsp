<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="libraryViewer">

  <t:aliasBean alias="#{navigator}" value="#{libraryViewer.librarySearchResults}" >
    <%@ include file="../searchResultsNavPanel.jspf"  %>
  </t:aliasBean>

  <t:panelGrid
    id="labelsAndDataColumns"
    columns="2"
    rowClasses="row1,row2"
    columnClasses="keyColumn,column"
  >
    <h:outputText value="Library Name" />
    <h:outputText value="#{libraryViewer.library.libraryName}" />
    <h:outputText value="Short Name" />
    <h:outputText value="#{libraryViewer.library.shortName}" />
    <h:outputText value="Screen Type" />
    <h:outputText value="#{libraryViewer.library.screenType}" />
    <h:outputText value="Library Type" />
    <h:outputText value="#{libraryViewer.library.libraryType}" />
    <h:outputText value="Vendor" />
    <h:outputText value="#{libraryViewer.library.vendor}" />
    <h:outputText value="Description" />
    <h:outputText value="#{libraryViewer.library.description}" />
    <h:outputText value="Number of Wells" />
    <h:outputText value="#{libraryViewer.librarySize}" />
  </t:panelGrid>

  <t:panelGroup>
    <h:form id="libraryContentsForm">
      <h:commandButton
        value="View Library Contents"
        action="#{libraryViewer.viewLibraryContents}"
        styleClass="command"
      />

      <t:panelGroup visibleOnUserRole="librariesAdmin">
        <h:commandButton
          value="Import Library Contents"
          action="#{libraryViewer.importRNAiLibraryContents}"
          rendered="#{libraryViewer.isRNAiLibrary}"
          styleClass="command"
        />
        <h:commandButton
          value="Import Library Contents"
          action="#{libraryViewer.importCompoundLibraryContents}"
          rendered="#{libraryViewer.isCompoundLibrary}"
          styleClass="command"
        />
      </t:panelGroup>

      <h:commandButton
        value="Unload Library Contents"
        action="#{libraryViewer.unloadLibraryContents}"
        onclick="javascript: return confirm('Are you sure you want to unload the contents of this library?');"
        rendered="#{libraryViewer.librarySize > 0}"
        styleClass="command"
      />
    </h:form>
  </t:panelGroup>

</f:subview>


