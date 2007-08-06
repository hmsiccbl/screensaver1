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
      <span class="helpNB">Please note</span> that Screensaver currently has no information on
      the small molecules in the natural products libraries. While the experimental wells in these
      libraries are labelled as "experimental", no information about the small molecules contained
      in those wells will be displayed.
    </p>
    <p>
      On rare occasions, a compound will fail to display a structure image.
      We apologize for this and we are working on fixing the problem.
      In the meantime, if you encounter this problem, you can copy the SMILES string and paste it
      into <a href="http://demo.eyesopen.com/cgi-bin/depict" target="_blank">OpenEye depict</a>
      as a workaround.
    </p>
    <p>
      <span class="helpTip">Internet Explorer Tip:</span> Are you getting a "Security Information" popup
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
