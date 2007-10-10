<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="main">
	<f:verbatim escape="false">
		<p>Check back here regularly for the latest Screensaver news and
		gossip!</p>
		<dl>
			<dt><span class="newsDate">2007.Oct.10, v1.3.0</span></dt>
			<dd>Upgraded the Screensaver object model to use Hibernate
			annotations instead of XDoclet annotations. Other various
			across-the-board model updates that should improve understanding,
			ease-of-use, and ease-of-maintenance of object model code for
			developers. This upgrade should have little to no impact on
			Screensaver users.</dd>
			<dt><span class="newsDate">2007.Sep.13, v1.2.0</span></dt>
			<dd>Added "Study" and "Annotations" data types to Screensaver,
			allowing the system to incorporate and present 3rd-party library
			annotation data, as well as providing a means for sharing and
			presenting biologically relevant results from in-house screens.</dd>
			<dt><span class="newsDate">2007.Aug.24, v1.1.0</span></dt>
			<dd>Screensaver released as open source project.</dd>
			<dt><span class="newsDate">2007.Aug.6</span></dt>
			<dd>Added workaround instructions for structure image viewing
			problems to the Well Viewer and Compound Viewer pages and help pages.
			</dd>
		</dl>
	</f:verbatim>
</f:subview>
