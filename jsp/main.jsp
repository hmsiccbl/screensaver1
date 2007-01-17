<%-- The html taglib contains all the tags for dealing with forms and other HTML-specific goodies. --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- The core taglib contains all the logic, validation, controller, and other tags specific to JSF. --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- The core taglib for JSTL; commented out until we really need it (we'll try to get by without and instead use pure JSF componentry --%>
<%--@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" --%>
<%-- The Apache Tomahawk JSF components --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Tiles --%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="main">
  <t:outputText value="Welcome #{login.screensaverUser.fullNameFirstLast}!" styleClass="title"/>
  <t:div/>
  <f:verbatim escape="false">
    <p>Thanks for trying out Screensaver!</p>
    <p>To get started, try clicking some of the items in the left menu
		bar. For a detailed description of what you can see the <a href="helpViewer.jsf">Instructions</a>.</p>
		<p>Please note that this is a <i>beta</i> release of Screensaver, and
		is intended to elicit your feedback. Your input will directly
		influence the order in which functionality is added, improved, and
		fixed! Please send your comments to <a
			href="mailto:john_sullivan@hms.harvard.edu,andrew_tolopko@hms_harvard.edu">john_sullivan@hms.harvard.edu,
		andrew_tolopko@hms_harvard.edu</a>.</p>
	</f:verbatim>
</f:subview>
