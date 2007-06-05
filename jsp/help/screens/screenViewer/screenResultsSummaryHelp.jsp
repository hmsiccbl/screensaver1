<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="screenResultsSummaryHelpText">
  <f:verbatim escape="false">
    <p>
      The Screen Results Summary section of the Screen Viewer page provides some basic information
      about the screen results. Position your mouse over any of the
      headers in the left-hand column for a description of what that field contains. You can also
      download the results in an Excel file format by clicking on the "Download" button.
    </p>
    <p>
      The Screen Results Summary section only appears when screen results are available for the
      screen. If results have not been loaded into Screensaver, or you are restricted from viewing
      the results, an empty section with the header "Screen results not available" will
      appear instead.
    </p>
  </f:verbatim>
  <t:panelGroup visibleOnUserRole="screenResultsAdmin">
    <f:verbatim escape="false">
      <p>
        As a <i>Screen Results Administrator</i>, when no screen results have been loaded for
        this screen, you will be able to load screen results from a file. Click the "Browse..."
        button to select the Excel file containing the screen results, and click the "Load"
        button to load the results. Note that loading screen results can sometimes take a long
        time. If any errors occur during the loading process, no results will be loaded into
        the database, and a list of errors in the screen results file is presented. A
        version of the screen result file with annotations for the errors is also available
        for download.
      </p>
      <p>
        If the screen results have already been loaded, then you will be able to delete them
        by pressing the "Delete" button in the Screen Results Summary section. This clears the
        way for reloading the results, which can be useful for correcting errors in a previous
        version of the results, or when results for more plates have come back.
      </p>
    </f:verbatim>
  </t:panelGroup>
</f:subview>