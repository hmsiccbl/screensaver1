<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="screenDetailsHelpText">
  <f:verbatim escape="false">
    <p>
      The Screen Details section of the Screen Viewer page contains a variety of basic
      information about a screen, including who was involved in the screen, what is being
      screened for, and the protocol that was used. Position your mouse over any of the
      headers in the left-hand column for a description of what that field contains.
    </p>
    <p>
      Some screen details, such as Screening Room Activities, (e.g., screening library plates
      in the screening room), or Cherry Pick Requests, are too large too display in the same
      page. They are summarized here, and "View" buttons are provided to view these items
      in a separate page. However, <span class="helpNB">please note</span> that viewing any Screening Room
      Activities, as well as Cherry Pick Requests for Small Molecule screens, is currently
      not implemented. We intend to provide these views in a later release.
    </p>
    <p>
      Also note that some information available for screens that you are associated with (i.e.,
      lab head, lead screener, or collaborator) is not shown for screens that you are not
      associated with.
    </p>
  </f:verbatim>
  <t:panelGroup visibleOnUserRole="readEverythingAdmin">
    <f:verbatim escape="false">
      <p>
        As a <i>Read-everything Administrator</i>, you are also able to view administrative
        information about the screen by clicking the "Show Admin" button at the top of the
        panel. You can hide it again by clicking "Hide Admin". Administrative information
        administrator comments, as well as important dates such as the original application
        date. <span class="helpNB">Please note</span> that a variety of administrator fields, such as
        attached files and billing information, are not yet implemented.
      </p>
    </f:verbatim>
  </t:panelGroup>
  <t:panelGroup visibleOnUserRole="screensAdmin">
    <f:verbatim escape="false">
      <p>
        As a <i>Screens Administrator</i>, you can make various screen details editable by
        clicking the "Edit" button. <span class="helpNB">Please note</span> that any changes you make here
        will not be permanent. This data is periodically reloaded from the <tt>ScreenDB</tt>
        database, and any changes you make here will be overwritten the next time this reload
        occurs. These editing capabilities are an experimental feature. For the time being,
        changes to screen details information should be made in <tt>ScreenDB</tt>.
      </p>
    </f:verbatim>
  </t:panelGroup>
</f:subview>