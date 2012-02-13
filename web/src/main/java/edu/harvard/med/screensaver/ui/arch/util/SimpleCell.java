// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util;

import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;

/**
 * Simple abstraction for a cell in a JSF DataTable
 * 
 * NOTE: cannot use an anonymous inner class with er-ri-1.0.jar, (due to
 * reflection limitations), so must create a public inner class implementing
 * {@link SimpleCell#cellAction() } <br/>
 * For: [#3220] Study link in Well Viewer Annotation table is broken
 * TODO: why is this working for {@link TableColumn}?
 * 
 * @see <a
 *      href=http://stackoverflow.com/questions/2998745/how-to-invoke-jsf-action
 *      -on-an-anonymous-class-el-cannot-access-it>link</a>
 * @return facelets nav
 * 
 * @see TableColumn for more robust implementation
 */
public class SimpleCell {
	private String _title;
	private Object _value;
	private String _description;
	private String _groupId;
	private String _style;
	private String _styleClass;
	private Object _linkValue;
	// a secondary table of simple cells to provide grouping/summary information
	// (optional)
	private List<SimpleCell> _metaInformation;

	public SimpleCell(String title, Object value, String description, List<SimpleCell> metaInformation) {
		_title = title;
		_value = value;
		_description = description;
		_metaInformation = metaInformation;
	}

	public SimpleCell(String title, Object value, String description) {
		this(title, value, description, (List<SimpleCell>) null);
	}

	public SimpleCell withStyle(String style) {
		_style = style;
		return this;
	}

	public SimpleCell withLinkValue(Object value) {
		_linkValue = value;
		return this;
	}

	public SimpleCell withStyleClass(String style) {
		_styleClass = style;
		return this;
	}

	public String getStyle() {
		return _style;
	}

	public String getStyleClass() {
		return _styleClass;
	}

	public Object getLinkValue() {
		return _linkValue;
	}

	public String getTitle() {
		return _title;
	}

	public Object getValue() {
		return _value;
	}

	/**
	 * NOTE: cannot use an anonymous inner class with er-ri-1.0.jar, (due to
	 * reflection limitations), so must create a public inner class implementing
	 * {@link SimpleCell#cellAction() } <br/>
	 * For: [#3220] Study link in Well Viewer Annotation table is broken
	 * 
	 * @see <a
	 *      href=http://stackoverflow.com/questions/2998745/how-to-invoke-jsf-action
	 *      -on-an-anonymous-class-el-cannot-access-it>link</a>
	 * @return facelets nav
	 */
	public Object cellAction() {
		return null;
	}

	public boolean isCommandLink() {
		return false;
	}

	public String getDescription() {
		return _description;
	}

	public String getGroupId() {
		return _groupId;
	}

	public SimpleCell setGroupId(String groupId) {
		_groupId = groupId;
		return this;
	}

	public DataModel getMetaInformation() {
		return new ListDataModel(_metaInformation);
	}

	public boolean isMetaInformationAvailable() {
		return _metaInformation != null && !_metaInformation.isEmpty();
	}

	public String toString() {
		return "" + getValue();
	}

}
