<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="compoundViewerHelpText">
  <f:verbatim escape="false">
    <p>
      The Compound Viewer page displays basic information about a compound, as well as 2D
      structure image, and a list of the library wells where the compound can be found.
    </p>
    <p>
      When "Is Salt" has the value "true", this generally indicates that the compound is
      not potentially bioactive, but is present in solution with other potentially bioactive
      compounds.
    </p>
    <p>
      <span class="helpNB">Please note</span> we are currently working on getting PubChem IDs for all of
      our compounds. Currently, about 50% of our compounds have PubChem IDs. Thank you for
      your patience!
    </p>
    <p>
      On rare occasions, a compound will fail to display a structure image.
      We apologize for this and we are working on fixing the problem.
      In the meantime, if you encounter this problem, you can copy the SMILES string and paste it
      into <a href="http://demo.eyesopen.com/cgi-bin/depict" target="_blank">OpenEye depict</a>
      as a workaround.
    </p>
    <p>
      <span class="helpTip">Internet Explorer Tip: </span>Are you getting a "Security Information" popup
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
