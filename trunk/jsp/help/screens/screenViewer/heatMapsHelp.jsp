<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="heatMapsHelpText">
  <f:verbatim escape="false">
    <p>
      The Heat Maps section of the Screen Viewer page allows you to look at heat maps for the
      assay plates. Each well on the plate is colored from blue to red to indicate the range of the
      value from low to high. The legend on the right side of the chart tells you what colors are
      used for what values, and also provides various plate-level statistics. You can customize your
      heat maps in many ways:
      <ul>
        <li>
          Select a plate number to view in the pulldown menu at the top left
          labelled "Plate". Or use the blue arrow buttons to scroll through the plates.
        </li>
        <li>
          Click on the checkbox next to "Show values" to show or hide the numerical values in the
          cells. Change the number format for numerical values by selecting from the
          "Numeric format" pulldown menu to the left of the heat map, and clicking the "Update"
          button below.
        </li>
        <li>
          View values for a different data header by making a selection from the
          "Data header" pulldown menu to the left of the heat map, and clicking the "Update" button
          below.
        </li>
        <li>
          Normalize values using a z-score by selecting from the "Scoring" pulldown menu to the left
          of the heat map. Click on the checkboxes next to "Control wells" and "Edge wells" to
          exclude those values from your normalization. Be sure to click the "Update" button
          to redisplay the results.
        </li>
        <li>
          Click the "Add Heat Map" button at the top to compare two or more heat maps simultaneously.
          You can compare two different columns of data by selecting different data headers for the
          each heat map.
        </li>
        <li>
        	Click on any heat map cell to view that well in the Well Viewer page. You can also hover
         the mouse cursor over the cell to get cell coordinates and data value.
        </li>
      </ul>
    </p>
  </f:verbatim>
</f:subview>