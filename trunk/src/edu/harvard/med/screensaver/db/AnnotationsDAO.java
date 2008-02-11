// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/db/ScreenResultsDAOImpl.java $
// $Id: ScreenResultsDAOImpl.java 1725 2007-08-20 20:43:25Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;

import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;

import org.apache.log4j.Logger;


public class AnnotationsDAO extends AbstractDAO
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationsDAO.class);

  public static int SORT_BY_VENDOR_ID = -1;

  // instance data members

  // public constructors and methods

  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public AnnotationsDAO()
  {
  }

  @SuppressWarnings("unchecked")
  public List<AnnotationType> findAllAnnotationTypes()
  {
    return getHibernateTemplate().find("from AnnotationType");
  }

  @SuppressWarnings("unchecked")
  public List<AnnotationType> findAllAnnotationTypesForReagent(ReagentVendorIdentifier rvi)
  {
    return getHibernateTemplate().find("select at from AnnotationValue av join av.annotationType at " +
                                       "where av.reagent.id=? ",
                                       new Object[] { rvi } );
  }

  // private methods

}
