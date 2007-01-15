<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="libraryViewer">

  <t:aliasBean alias="#{navigator}" value="#{libraryViewer.librarySearchResults}" >
    <%@ include file="../searchResultsNavPanel.jspf"  %>
  </t:aliasBean>

  <h:panelGrid columns="2" rendered="true">
    <h:outputText value="short name:" />
    <h:outputText value="#{libraryViewer.library.shortName}" />
    <h:outputText value="library name:" />
    <h:outputText value="#{libraryViewer.library.libraryName}" />
    <h:outputText value="screen type:" />
    <h:outputText value="#{libraryViewer.library.screenType}" />
    <h:outputText value="library type:" />
    <h:outputText value="#{libraryViewer.library.libraryType}" />
    <h:outputText value="vendor:" />
    <h:outputText value="#{libraryViewer.library.vendor}" />
    <h:outputText value="description:" />
    <h:outputText value="#{libraryViewer.library.description}" />
    <h:outputText value="number of wells:" />
    <h:outputText value="#{libraryViewer.librarySize}" />
  </h:panelGrid>

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


