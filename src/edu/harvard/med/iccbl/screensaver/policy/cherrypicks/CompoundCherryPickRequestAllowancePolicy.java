// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy.cherrypicks;

import java.math.BigDecimal;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.model.cherrypicks.CompoundCherryPickRequest;
import edu.harvard.med.screensaver.policy.CherryPickRequestAllowancePolicy;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.common.collect.Lists;

/**
 * For compound screens, the cherry pick limit is 0.3% of the number of distinct
 * compounds selected (and not wells, which may have overlapping compounds).
 */
public class CompoundCherryPickRequestAllowancePolicy implements CherryPickRequestAllowancePolicy<CompoundCherryPickRequest>
{
  private static Logger log = Logger.getLogger(CherryPickRequestAllowancePolicy.class);

  private static final BigDecimal SOURCE_WELL_COUNT_PCT_LIMIT = new BigDecimal("0.003");

  private GenericEntityDAO _dao;

  /**
   * @motivation for CGLIB2
   */
  protected CompoundCherryPickRequestAllowancePolicy()
  {
  }

  public CompoundCherryPickRequestAllowancePolicy(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public int getCherryPickAllowance(final CompoundCherryPickRequest cpr)
  {
    Query query = new Query() {
      public List execute(Session session)
      {
        org.hibernate.Query q = session.createQuery("select count(distinct c.id) from CherryPickRequest cpr join cpr.screen s join s.screenResult sr join sr.wells w join w.compounds c where cpr = ?");
        q.setEntity(0, cpr);
        Object result = q.uniqueResult();
        return Lists.newArrayList(result);
      }
    };
    Long distinctCompounds = (Long) _dao.runQuery(query).get(0);
    return SOURCE_WELL_COUNT_PCT_LIMIT.multiply(new BigDecimal(distinctCompounds)).intValue();
  }

  public int getCherryPickAllowanceUsed(CompoundCherryPickRequest cpr)
  {
    return cpr.getScreenerCherryPicks().size();
  }

  public boolean isCherryPickAllowanceExceeded(CompoundCherryPickRequest cpr)
  {
    if (cpr.getScreen().getScreenResult() == null) {
      return false;
    }
    return getCherryPickAllowanceUsed(cpr) > getCherryPickAllowance(cpr);
  }
}
