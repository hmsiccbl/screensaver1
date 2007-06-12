<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="libraryViewerHelpText">
  <f:verbatim escape="false">
    <p>
      The Library Viewer page displays basic information about a library, including the
      plate range and the number of wells in the library containing experimental compounds
      or silencing reagents.
    </p>
    <p>
      <span class="helpNB">Please note</span> that currently, all of the natural products libraries will
      show up as having no experimental wells. Even though we do not know the structures for
      most of the compounds in these libraries, the experimental wells should be properly
      labelled as experimental. We are currently working on fixing this problem. Thank you for
      your patience!
    </p>
    <p>
      Clicking the "View Library Contents" button will bring you to a Well Search Results page
      with all the wells in that library. You can navigate the Well Search Results as
      described in the help section "Viewing Well Search Results".
    </p>
  </f:verbatim>
  <t:panelGroup visibleOnUserRole="librariesAdmin">
  <f:verbatim escape="false">
    <p>
      As a <i>Libraries Adminstrator</i>, you can import and unload library contents from the
      Library Viewer page as well.
      Clicking the "Import Library Contents" will take you to the
      Library Contents Importer page. You basically only need to upload a file in the proper
      format to import the contents of that file. The file formats are described on the ICCBL
      wiki for <a href="https://wiki.med.harvard.edu/ICCBL/CompoundLibrariesSDFileFormat">
      SD Files</a> and <a href="https://wiki.med.harvard.edu/ICCBL/RNAiLibrariesExcelFileFormat">
      RNAi Library Excel Files</a>.
    </p>
    <p>
      If any errors occur in processing the file, the list of errors will appear on the
      Library Contents Importer page. If you are having difficulty handling an error on your
      own, as a developer.
    </p>
    <p>
      You can import library contents from multiple files. You can also unload all the library
      contents by clicking the "Unload Library Contents" button. A small confirmation dialog
      will appear before library contents are actually unloaded, so don't worry about any
      stray mouse clicks!
    </p>
    <p>
      Finally, <span class="helpNB">please note</span> that you can unload and reload library contents at
      any time without disturbing any other data in the system. The worst that will happen is
      that a screener will see a well or wells as empty between the times when you unload and
      reload the library contents.
    </p>
  </f:verbatim>
  </t:panelGroup>
</f:subview>
