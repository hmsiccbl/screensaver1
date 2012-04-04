// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/main/java/edu/harvard/med/screensaver/db/LibrariesDAOImpl.java $
// $Id: LibrariesDAOImpl.java 6951 2012-01-13 19:13:34Z atolopko $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.Screen;

public class CellsDAOImpl extends AbstractDAO implements CellsDAO {
	private static Logger log = Logger.getLogger(CellsDAOImpl.class);

	private GenericEntityDAO _dao;

	private LibrariesDAO _librariesDao;

	/**
	 * @motivation for CGLIB dynamic proxy creation
	 */
	public CellsDAOImpl() {
	}

	public CellsDAOImpl(GenericEntityDAO dao, LibrariesDAO librariesDao) {
		_dao = dao;
		_librariesDao=librariesDao;
	}

	@SuppressWarnings("unchecked")
	public Cell findCellFromCLOId(String cloId) {
		return _dao.findEntityByProperty(Cell.class, "cloid", cloId);
	}

	public Set<ExperimentalCellInformation> findCellInformation(Screen screen) {
		return null;
	}

	@Override
	public SortedSet<ExperimentalCellInformation> findCellExperimentsFromCLOIds(String[] cloIds) {

		Set<Cell> cells = findCellsByCloIds(cloIds);

		SortedSet<ExperimentalCellInformation> icis = Sets.newTreeSet();

		for (Cell cell : cells) {
			icis.addAll(_dao.findEntitiesByProperty(ExperimentalCellInformation.class, "cell", cell));
		}
		log.debug("found: " + icis.size() + " icis for this list of cells");
		return icis;
	}

	@Override
	public Set<Cell> findCellsByCloIds(String[] cloIds) {
		Set<Cell> cells = Sets.newHashSet();
		for (String s : cloIds) {
			Cell temp = _dao.findEntityByProperty(Cell.class, "cloId", s);
			if (temp == null) {
				log.info("no cell found for clo_id: " + s);
				continue;
			}
			cells.add(temp);
		}
		log.debug("found: " + cells.size() + " cells for " + Joiner.on(",").join(cloIds) + ": " + cells);
		return cells;
	}

	@Override
	public Set<Cell> findCellsByHMSID(String[] ids) {
		Set<Cell> cells = Sets.newHashSet();
		for (String s : ids) {
			Cell temp = _dao.findEntityByProperty(Cell.class, "facilityId", s);
			if (temp == null) {
				log.info("no cell found for facility: " + s);
				continue;
			}
			cells.add(temp);
		}
		log.debug("found: " + cells.size() + " cells for " + Joiner.on(",").join(ids) + ": " + cells);
		return cells;
	}

	@Override
	public Set<String> findCanonicalCompoundsScreenedByWellId(final Cell cell) 
	{
		
    List<Well> wells = _dao.runQuery(new edu.harvard.med.screensaver.db.Query<Well>() {
      public List<Well> execute(Session session)
      {
//    		String hql = "select w from ResultValue rv join rv.well w " +
//    				"join rv.dataColumn dc join dc.screenResult sc join sc.screen s join s.experimentalCellInformationSet eci join eci.cell c where c= ? ";
//    		String hql = "select w from SmallMoleculeReagent r left join fetch r.well w left join fetch r.compoundNames cn, ResultValue rv " +
//    				"join rv.dataColumn dc join dc.screenResult sc join sc.screen s join s.experimentalCellInformationSet eci join eci.cell c where w.latestReleasedReagent=r and rv.well = w and c= ? ";
    		String hql = "select w from ResultValue rv join rv.well w " +
    				"join rv.dataColumn dc join dc.screenResult sc join sc.screen s join s.experimentalCellInformationSet eci join eci.cell c where c.facilityId= ? ";

      	org.hibernate.Query q = session.createQuery(hql);
      	q.setParameter(0, cell.getFacilityId());
      	return q.list();
      }
    });
    
    if(wells == null || wells.isEmpty() ) return null;
    
    Set<String> wellIds = Sets.newHashSet(Lists.transform(wells, new Function<Well,String>() {
      @Override
      public String apply(Well from)
      {
        return from.getWellId();
      }
    }));
    
    return _librariesDao.findCanonicalReagentWellIds(Sets.newHashSet(wellIds));
	}
	
}