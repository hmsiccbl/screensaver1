// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries.rnai;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ConfirmedPositiveValue;
import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.util.NullSafeComparator;
import edu.harvard.med.screensaver.util.Pair;

/**
 * [#1476] RNAi duplex validation report:
 * For a given pool siRNA reagent; having 4 duplex reagents; query for screens of these duplex reagents,
 * and report on whether the results score a ConfirmedPositiveValue.CONFIRMED
 */
public class DuplexConfirmationReport
{

  private GenericEntityDAO _dao;

  public DuplexConfirmationReport()
  {}

  public DuplexConfirmationReport(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  public static class ConfirmationReport
  {
    ConfirmationReport(List<SilencingReagent> reagents)
    {
      this.reagents = reagents;
    }

    public List<SilencingReagent> reagents;
    public List<Screen> screens = Lists.newArrayList();
    public Map<Screen,Map<SilencingReagent,ConfirmedPositiveValue>> results = Maps.newHashMap();
  }

  @Transactional
  public ConfirmationReport getDuplexReconfirmationReport(final SilencingReagent reagent)
  {
    if (!reagent.getLibraryContentsVersion().getLibrary().isPool()) {
      throw new IllegalArgumentException("Duplex Confirmation Report is available for pool reagents (wells) only.");
    }

    List<SilencingReagent> duplexReagents = _dao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        HqlBuilder builder = new HqlBuilder();
        builder.select("sr").
          from(SilencingReagent.class, "sr1").
          from("sr1", SilencingReagent.duplexWells.getPath(), "dw", JoinType.INNER).
          from("dw", Well.latestReleasedReagent.getPath(), "sr", JoinType.INNER).
          where("sr1", Operator.EQUAL, reagent);
        return builder.toQuery(session, true).list();
      }
    });

    ConfirmationReport report = new ConfirmationReport(duplexReagents);

    Set<Screen> screens = Sets.newHashSet();
    for (SilencingReagent sr : duplexReagents) {
      for (Pair<Screen,ConfirmedPositiveValue> result : findConfirmationResults(sr)) {
        if (!report.results.containsKey(result.getFirst())) {
          screens.add(result.getFirst());
          Map<SilencingReagent,ConfirmedPositiveValue> map = Maps.newHashMap();
          report.results.put(result.getFirst(), map);
        }
        report.results.get(result.getFirst()).put(sr, result.getSecond());
      }
    }

    report.screens.addAll(screens);
    Collections.sort(report.screens, new NullSafeComparator<Screen>() {
      @Override
      public int doCompare(Screen o1, Screen o2)
      {
        return new NullSafeComparator<Integer>() {
          @Override
          protected int doCompare(Integer o1, Integer o2)
          {
            return o1.compareTo(o2);
          }
        }.compare(o1.getScreenNumber(), o2.getScreenNumber());
      }
    });

    return report;
  }

  // Here, the SilencingReageng is the duplex reagent
  private List<Pair<Screen,ConfirmedPositiveValue>> findConfirmationResults(final SilencingReagent duplexReagent)
  {
    _dao.needReadOnly(duplexReagent, Reagent.libraryContentsVersion.getPath());
    _dao.needReadOnly(duplexReagent.getLibraryContentsVersion(), LibraryContentsVersion.library.getPath());
    _dao.needReadOnly(duplexReagent, Reagent.well.getPath());
    if (duplexReagent.getLibraryContentsVersion().getLibrary().isPool()) {
      throw new IllegalArgumentException("findConfirmationResults can be used with duplex reagent(wells) only.");
    }

    List<ResultValue> resultValues = _dao.runQuery(new edu.harvard.med.screensaver.db.Query() {
      public List<?> execute(Session session)
      {
        HqlBuilder builder = new HqlBuilder();
        // TODO: we will want to create an AssayWell.isConfirmedPositive value and querying off that
        builder.select("rv").
          from(ResultValue.class, "rv").distinctProjectionValues().
          from("rv", ResultValue.Well.getPath(), "w").
          from("rv", ResultValue.DataColumn.getPath(), "dc").
          where("w", "latestReleasedReagent", Operator.EQUAL, duplexReagent).
          where("dc", "dataType", Operator.EQUAL, DataType.CONFIRMED_POSITIVE_INDICATOR);

        return builder.toQuery(session, true).list();
      }
    });

    Map<Screen,ResultValue> screenMap = Maps.newHashMap();
    List<Pair<Screen,ConfirmedPositiveValue>> results = Lists.newArrayList();
    for (ResultValue rv : resultValues) {
      Screen screen = rv.getDataColumn().getScreenResult().getScreen();
      if (screenMap.containsKey(screen)) {
        throw new IllegalArgumentException("screen already recorded with rv: " + screenMap.get(screen)); // TODO: remove this check after sure that the query is correct
      }
      else {
        screenMap.put(screen, rv);
        results.add(Pair.newPair(screen, rv.getConfirmedPositiveValue()));
      }

    }
    return results;
  }
}
