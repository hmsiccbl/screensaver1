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
    <h:outputText value="library type:" />
    <h:outputText value="#{libraryViewer.library.libraryType}" />
    <h:outputText value="description:" />
    <h:outputText value="#{libraryViewer.library.description}" />
    <h:outputText value="number of wells:" />
    <h:outputText value="#{libraryViewer.librarySize}" />
  </h:panelGrid>

  <h:form id="viewLibraryContentsForm">
    <h:commandLink
      value="view library contents"
      action="#{libraryViewer.viewLibraryContents}"
    />
  </h:form>
  
  <h:form id="loadLibraryContentsForm">
    <t:panelGroup visibleOnUserRole="librariesAdmin">
      <h:commandLink
        value="import RNAi library contents"
        action="#{libraryViewer.importRNAiLibraryContents}"
        rendered="#{libraryViewer.isRNAiLibrary}"
      />
      <h:commandLink
        value="import compound library contents"
        action="#{libraryViewer.importCompoundLibraryContents}"
        rendered="#{libraryViewer.isCompoundLibrary}"
      />
    </t:panelGroup>
  </h:form>

</f:subview>


