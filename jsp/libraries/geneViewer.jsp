<%@include file="/headers.inc"%>

<f:subview id="geneViewer">

  <h:panelGrid columns="2" rendered="true">
    <h:outputText value="gene name:" />
    <h:outputText value="#{geneViewer.gene.geneName}" />
  </h:panelGrid>

</f:subview>


