<%@include file="/headers.inc"%>

<f:subview id="libraryViewer">

  <h:panelGrid columns="2" rendered="true">
    <h:outputText value="short name:" />
    <h:outputText value="#{libraryViewer.library.shortName}" />
    <h:outputText value="library name:" />
    <h:outputText value="#{libraryViewer.library.libraryName}" />
    <h:outputText value="library type:" />
    <h:outputText value="#{libraryViewer.library.libraryType}" />
  </h:panelGrid>

  <t:div />
  
  <h:form id="loadLibraryContentsForm">
    <t:panelGroup visibleOnUserRole="librariesAdmin">
      <h:commandLink
        value="import RNAi library contents"
        action="#{libraryViewer.goImportRNAiLibraryContents}"
        rendered="#{libraryViewer.isRNAiLibrary}"
      />
      <h:commandLink
        value="import compound library contents"
        action="#{libraryViewer.goImportCompoundLibraryContents}"
        rendered="#{libraryViewer.isCompoundLibrary}"
      />
    </t:panelGroup>
  </h:form>

</f:subview>


