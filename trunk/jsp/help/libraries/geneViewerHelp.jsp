<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="geneViewerHelpText">
  <f:verbatim escape="false">
    <p>
      The Gene Viewer page displays basic information about a gene, and a list of the library
      wells in which silencing reagents targetting this gene are found.
    </p>
    <p>
      You can look up the gene in Entrez Gene by clicking on the Entrez Gene ID, and in
      GenBank by clicking on one of the GenBank Accession Numbers.
    </p>
  </f:verbatim>
</f:subview>
