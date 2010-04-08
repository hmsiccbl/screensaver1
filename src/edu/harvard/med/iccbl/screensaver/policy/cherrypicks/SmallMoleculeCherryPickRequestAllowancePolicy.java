// $HeadURL$
// $Id$
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
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.policy.CherryPickRequestAllowancePolicy;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.common.collect.Lists;

/**
 * For small molecule screens, the cherry pick limit is 0.3% of the number of distinct
 * compounds selected (and not wells, which may have overlapping compounds).
 */
public class SmallMoleculeCherryPickRequestAllowancePolicy implements CherryPickRequestAllowancePolicy<SmallMoleculeCherryPickRequest>
{
  private static Logger log = Logger.getLogger(CherryPickRequestAllowancePolicy.class);

  private static final BigDecimal SOURCE_WELL_COUNT_PCT_LIMIT = new BigDecimal("0.003");

  private GenericEntityDAO _dao;

  /**
   * @motivation for CGLIB2
   */
  protected SmallMoleculeCherryPickRequestAllowancePolicy()
  {
  }

  public SmallMoleculeCherryPickRequestAllowancePolicy(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public int getCherryPickAllowance(final SmallMoleculeCherryPickRequest cpr)
  {
    Query query = new Query() {
      public List execute(Session session)
      {
        org.hibernate.Query q = session.createQuery("select count(distinct r.smiles) from CherryPickRequest cpr join cpr.screen s join s.screenResult sr join sr.wells w join w.reagents r where cpr = ? and r.libraryContentsVersion = w.library.latestReleasedContentsVersion");
        q.setEntity(0, cpr);
        Object result = q.uniqueResult();
        return Lists.newArrayList(result);
      }
    };
    Long distinctCompounds = (Long) _dao.runQuery(query).get(0);
    return SOURCE_WELL_COUNT_PCT_LIMIT.multiply(new BigDecimal(distinctCompounds)).intValue();
  }

  public int getCherryPickAllowanceUsed(SmallMoleculeCherryPickRequest cpr)
  {
    return cpr.getScreenerCherryPicks().size();
  }

  public boolean isCherryPickAllowanceExceeded(SmallMoleculeCherryPickRequest cpr)
  {
    if (cpr.getScreen().getScreenResult() == null) {
      return false;
    }
    return getCherryPickAllowanceUsed(cpr) > getCherryPickAllowance(cpr);
  }
}
