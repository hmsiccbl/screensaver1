<%@ taglib uri="http://java.sun.com/jsf/html"        prefix="h"     %>
<%@ taglib uri="http://java.sun.com/jsf/core"        prefix="f"     %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk"  prefix="t"     %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<f:subview id="footerSubview">

  <t:div styleClass="footer">
    <t:htmlTag value="hr"/>
    <h:outputText value="#{appInfo.applicationTitle} | "/>
    <h:outputLink value="http://iccb.med.harvard.edu" target="_blank"
      title="The ICCB-L. Without their support, Screensaver wouldn't be here!"
    >
      <h:outputText value="HMS: ICCB-Longwood" />
    </h:outputLink>
    <h:outputText value=" | "/>
    <h:outputLink value="http://nsrb.med.harvard.edu/" target="_blank"
      title="The NSRB. Without their support, Screensaver wouldn't be here!"
    >
      <h:outputText value="HMS: NSRB" />
    </h:outputLink>
    <h:outputText value=" | "/>
    <h:outputLink value="#{appInfo.feedbackUrl}" title="Let us know what you think!">
      <h:outputText value="Feedback" />
    </h:outputLink>
  </t:div>

</f:subview>