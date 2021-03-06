// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.policy.cherrypicks;

import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.policy.CherryPickRequestAllowancePolicy;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.common.collect.Lists;

public class RNAiCherryPickRequestAllowancePolicy implements CherryPickRequestAllowancePolicy<RNAiCherryPickRequest>
{
  private static Logger log = Logger.getLogger(CherryPickRequestAllowancePolicy.class);

  private static final int SILENCING_REAGENT_ALLOWANCE = 500 * 4;

  private GenericEntityDAO _dao;
  
  /**
   * @motivation for CGLIB2
   */
  protected RNAiCherryPickRequestAllowancePolicy()
  {
  }

  public RNAiCherryPickRequestAllowancePolicy(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public int getCherryPickAllowance(RNAiCherryPickRequest cpr)
  {
    return SILENCING_REAGENT_ALLOWANCE;
  }

  public int getCherryPickAllowanceUsed(final RNAiCherryPickRequest cpr)
  {
    Query query = new Query() {
      public List execute(Session session)
      {
        org.hibernate.Query q = session.createQuery("select count(r.id) from CherryPickRequest cpr join cpr.screenerCherryPicks scp join scp.screenedWell w join w.reagents r join w.library l where cpr = ? and r.libraryContentsVersion = l.latestReleasedContentsVersion");
        q.setEntity(0, cpr);
        Object result = q.uniqueResult();
        return Lists.newArrayList(result);
      }
    };
    Long silencingReagentsUsed = (Long) _dao.runQuery(query).get(0);
    return silencingReagentsUsed.intValue();
  }

  public boolean isCherryPickAllowanceExceeded(RNAiCherryPickRequest cpr)
  {
    return getCherryPickAllowanceUsed(cpr) > getCherryPickAllowance(cpr);
  }
}
