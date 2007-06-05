<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="heatMapsHelpText">
  <f:verbatim escape="false">
    <p>
      Heat Maps help is currently under construction. Check back soon!
    </p>
    <p>
      TODO: rework the following material:
      <ul>
          <li>
            Be sure to try the "Add Heat Map" button to view two heat maps for different data headers
            side by side;
          </li>
          <li>
            You can exclude control wells and edge wells from Z-score normalization calculations and
            color scaling by checking the "Control wells" and "Edge wells" checkboxes to the left
            of the heat map.
          </li>
          <li>
          	Clicking on a heat map cell will take to the Well Viewer.  Hovering your the mouse cursor 
          	over the cell will popup some information about the cell.
          </li>
        </ul>
    </p>
  </f:verbatim>
</f:subview>