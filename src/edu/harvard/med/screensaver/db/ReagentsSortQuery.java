// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Set;

import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Study;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class ReagentsSortQuery implements edu.harvard.med.screensaver.db.Query
{
  // static members

  private static Logger log = Logger.getLogger(ReagentsSortQuery.class);

  public enum SortByReagentProperty {
    ID,
    CONTENTS
  }

  // instance data members

  private Set<Reagent> _reagents;
  private Study _study;
  private AnnotationType _annotationType;
  private SortByReagentProperty _reagentProperty;


  // public constructors and methods

  public ReagentsSortQuery(Study study)
  {
    _study = study;
  }

  public ReagentsSortQuery(Set<Reagent> reagents)
  {
    _reagents = reagents;
  }

  public void setSortByAnnotationType(AnnotationType annotationType)
  {
    _annotationType = annotationType;
  }

  public void setSortByReagentProperty(SortByReagentProperty reagentProperty)
  {
    _reagentProperty = reagentProperty;
  }

  public org.hibernate.Query getQuery(Session session)
  {
    StringBuilder hql = new StringBuilder();
    hql.append("select r from Reagent r ");
    if (_annotationType != null) {
      hql.append("join r.annotationValues v join v.annotationType t ");
    }
    if (_study != null) {
      hql.append("join r.studies s where s = :study ");
    }
    else {
      hql.append("where r in (:reagents) ");
    }
    if (_annotationType != null) {
      hql.append("and t = :annotationType ");
    }
    hql.append("order by ");
    if (_annotationType != null) {
      hql.append(" v.value ");
    }
    else {
      if (_reagentProperty == SortByReagentProperty.CONTENTS) {
        // TODO: really need to move Well contents into Reagent
        log.error("sort by reagent contents not implemented!");
      }
      hql.append("r.id ");
    }

    if (log.isDebugEnabled()) {
      log.debug(hql.toString());
    }

    Query query = session.createQuery(hql.toString());

    if (_study != null) {
      query.setParameter("study", _study);
    }
    if (_annotationType != null) {
      query.setParameter("annotationType", _annotationType);
    }
    if (_reagents != null) {
      query.setParameterList("reagents", _reagents);
    }


    return query;
  }


  // private methods

}
