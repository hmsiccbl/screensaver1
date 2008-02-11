<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<f:subview id="main">
	<f:verbatim escape="false">
		<p>Check back here regularly for the latest Screensaver news and
		gossip!</p>
		<dl>
			<dt><span class="newsDate">2007.Dec.07, v1.4.2</span></dt>
			<dd><p>Cherry Pick Requests now allow an arbitrary set of
			screener-requested empty wells to be specified (rather than entire
			columns and rows).</p>
			</p>
			</dd>

			<dt><span class="newsDate">2007.Dec.06, v1.4.1</span></dt>
			<dd><p>Added Cherry Pick Requests Browser for administrators, allowing
			them to view that status of all cherry pick requests. Added
			additional status fields to the Cherry Pick Request Viewer to
			indicate whether the request has been completed.</p>
			</p>
			</dd>

			<dt><span class="newsDate">2007.Dec.03, v1.4.0</span></dt>
			<dd>
			<p>Updated PubChem CIDs have been assigned to Screensaver's
			compounds, allowing the user to directly navigate to the latest
			PubChem entries for each compound. PubChem provides information on
			the biological activities of small molecules.</p>
			<p>Search results can be filtered by specifying criterion for each
			column in the search result data table. (beta)</p>
			<p>Screeners can now browse (and filter) all of their screening room
			activities in a search result page. Administrators can now browse
			(and filter) screening room users and staff in a search result page.
			(beta)<p>
			</p>
			</dd>

			<dt><span class="newsDate">2007.Oct.12, v1.3.1</span></dt>
			<dd>
			<p>Administrator can now specify the set of rows to be left empty when
			Screensaver generates cherry pick plate mappings.</p>
			</dd>

			<dt><span class="newsDate">2007.Oct.11, v1.3.0</span></dt>
			<dd>
			<p>Upgraded the Screensaver object model to use Hibernate
			annotations instead of XDoclet annotations. Other various
			across-the-board model updates that should improve understanding,
			ease-of-use, and ease-of-maintenance of object model code for
			developers. This upgrade should have little to no impact on
			Screensaver users.</p>
			<p>Users will be pleased that all data tables in Screensaver now
			present a consistent and improved navigation panel for interacting
			with the table. Developers will be pleased that all data tables are
			now implemented in terms of the same DataTable class.</p>
			</dd>

			<dt><span class="newsDate">2007.Sep.13, v1.2.0</span></dt>
			<dd><p>Added "Study" and "Annotations" data types to Screensaver,
			allowing the system to incorporate and present 3rd-party library
			annotation data, as well as providing a means for sharing and
			presenting biologically relevant results from in-house screens.</p></dd>

			<dt><span class="newsDate">2007.Aug.24, v1.1.0</span></dt>
			<dd><p>Screensaver released as open source project.</p></dd>

			<dt><span class="newsDate">2007.Aug.6</span></dt>
			<dd><p>Added workaround instructions for structure image viewing
			problems to the Well Viewer and Compound Viewer pages and help pages.</p>
			</dd>
		</dl>
	</f:verbatim>
</f:subview>
