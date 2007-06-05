<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="wellFinderHelp">
  <f:verbatim escape="false">

    <p>
      You can easily look up the contents of wells in the ICCB-L libraries collection in
      various ways:
      <ul>
        <li>
          To look up the contents of a single well, enter in the plate number and well name in
          the Plate/Well text boxes in the left menu pane.
        </li>
        <li>
          Or, click on the "Find Wells" link in the left menu pane to get to the Well Finder
          page.
        </li>
      </ul>
     </p>

     <p>
       On the Well Finder page, you can enter in the coordinates for a single well or for
       multiple wells. The Well Finder page contains detailed instructions explaining how the
       well coordinates should be specified.
     </p>

     <p>
       Or you can also try the following:
       <ul>
         <li>
           Open a Screen Results spreadsheet.
         </li>
         <li>
           Highlight the Plate and Well columns by control-clicking or apple-clicking) the
           column headers for those columns.
         </li>
         <li>
           Type control-C (or apple-C on Mac) to copy the Plate/Well list to the clipboard.
         </li>
         <li>
           Click in the "Find Multiple Wells" text box on the Well Finder page, and type
           control-V (or apple-V) to paste the Plate/Well list into the text box.
         </li>
         <li>
           Click the "Find Wells" button. This should bring you to a list of the wells in your
           Screen Results!
         </li>
       </ul>
     </p>

  </f:verbatim>
</f:subview>
