// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;


/**
 * A global UI backing bean that provides basic, static information about the
 * application (e.g., application name). This is the only UI bean that can be
 * accessed by login.jsp. It is intended to be defined as a 'singleton' Spring
 * bean.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ApplicationInfo extends AbstractBackingBean
{
}

