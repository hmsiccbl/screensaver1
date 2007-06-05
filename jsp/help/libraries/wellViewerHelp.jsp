<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellViewerHelpText">
  <f:verbatim escape="false">
    <p>
      The Well Viewer page displays some basic information about a well, as well as information
      about the compounds contained in the well, and the genes for which silencing reagents
      are contained in the well.
    </p>
    <p>
      If you want to find other wells with similar contents, click on the Gene Name, or the
      SMILES string, to get to the Gene Viewer page, or the Compound Viewer page, respectively.
      These pages display lists of wells containing the same gene or compound.
    </p>
    <p>
      <b><i>Please note</i></b> that Screensaver currently marks some wells as "empty" that
      are not actually empty! This happens in two circumstances:
      <ol>
        <li>Some wells that have DMSO or siRNA buffer.</li>
        <li>
          All of the natural products libraries experimental wells are currently labelled as
          empty. Even though we do not know the structures for most of these compounds, the
          experimental wells should be properly labelled as experimental, and vendor
          identifier information should be included as well.
        </li>
      </ol>
      We are currently working on fixing both of these problems. Thank you for your patience!
    </p>
    <p>
      <b><i>Internet Explorer Tip:</i></b> Are you getting a "Security Information" popup
      window every time you try to view a well with compounds in it? Here's a workaround:
      <ol>
        <li>Open the Tools menu</li>
        <li>Click on Internet Options...</li>
        <li>Click on Security</li>
        <li>Click on Custom Level...</li>
        <li>Scroll down to "Display mixed content"</li>
        <li>Select "Enable"</li>
        <li>Click OK</li>
        <li>Click Yes</li>
        <li>Click OK</li>
      </ol>
      A better solution may be to <a href="http://www.mozilla.com/">download Firefox</a>.
    </p>
  </f:verbatim>
</f:subview>
