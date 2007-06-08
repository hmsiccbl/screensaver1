<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="cherryPickRequestViewerHelpText">
  <f:verbatim escape="false">

		<p>The Cherry Pick Request Viewer allows screeners and administrators
		to view the status of a screener's Cherry Pick Request. It also
		enables administrators to manage the physical process of creating
		cherry pick plates for follow-up screenings, as requested by
		screeners. <i>Currently, the Cherry Pick Request Viewer is only
		functional for RNAi screens.</i></p>

		<p>The Cherry Pick Request Viewer page is divided into five
		sections that you can collapse or expand by clicking on their headers.</p>

	</f:verbatim>
  <t:panelGroup rendered="#{empty inHelpViewer}">
    <f:verbatim escape="false">
      <p>
        The five sections are:
        <ul>
          <li><a href="cherryPickRequestViewer/screenSummaryHelp.jsf">Screen Summary</a></li>
          <li><a href="cherryPickRequestViewer/cherryPickRequestDetailsHelp.jsf">Cherry Pick Request Details</a></li>
          <li><a href="cherryPickRequestViewer/screenerCherryPicksHelp.jsf">Screener Cherry Picks</a></li>
          <li><a href="cherryPickRequestViewer/labCherryPicksHelp.jsf">Lab Cherry Picks</a></li>
          <li><a href="cherryPickRequestViewer/cherryPickPlatesHelp.jsf">Cherry Pick Plates</a></li>
        </ul>
        Click on a section name in the above list for help on that particular section.
      </p>
    </f:verbatim>
  </t:panelGroup>
  <f:verbatim escape="false">
  


  
  </f:verbatim>
</f:subview>