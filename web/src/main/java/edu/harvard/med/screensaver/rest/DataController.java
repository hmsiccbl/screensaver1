
package edu.harvard.med.screensaver.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

// TODO: use AOP to log requests
// TODO: handle error/exceptions gracefully

@Controller
public class DataController
{
  private static final Logger log = Logger.getLogger(DataController.class);

  private static final String MODEL_KEY = "data";
  private static final String ERROR_OBJECT_ID_NOT_FOUND = "Object ID not found";  //TODO: create an ErrorConverter

  @Autowired
  private GenericEntityDAO genericEntityDao;
  @Autowired
  private ScreenDAO screenDao;
  @Autowired
  private LibrariesDAO librariesDao;
  @Autowired
  private WellsSdfDataExporter sdfWellsDataExporter;
  
  
  @RequestMapping(value = "/screens", method = RequestMethod.GET)
  public ModelAndView screens()
  {
    return makeModel(new EntityCollection.Screens(Screen.class, screenDao.findAllScreens()));
  }
  
  @RequestMapping(value = "/screens/{facilityId}", method = RequestMethod.GET)
  public ModelAndView screen(@PathVariable(value = "facilityId") String facilityId) 
  {
    return makeModel(genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId));
  }

  @RequestMapping(value = "/studies", method = RequestMethod.GET)
  public ModelAndView studies() throws Exception
  {
    return makeModel(new EntityCollection.Studies(Study.class, screenDao.findAllStudies()));
  }

  @RequestMapping(value = "/screens/{facilityId}/results/assaywells", method = RequestMethod.GET)
  public ModelAndView screenResultAssayWells(@PathVariable(value = "facilityId") String facilityId)
  {
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.assayWells));
    if (s != null && s.getScreenResult() != null) {
      return makeModel(new EntityCollection.AssayWells(AssayWell.class, s.getScreenResult().getAssayWells()));
    }
    else {
      return makeNotFoundError();
    }
  }

  @RequestMapping(value = "/screens/{facilityId}/results/assaywells/{plateNumber}", method = RequestMethod.GET)
  public ModelAndView screenResultAssayWells(@PathVariable(value = "facilityId") String facilityId,
                                             @PathVariable(value = "plateNumber") Integer plateNumber)
  {
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.assayWells));
    if (s != null && s.getScreenResult() != null) {
      Set<AssayWell> assayWells = Sets.newHashSet();
      for(AssayWell aw:s.getScreenResult().getAssayWells()) {
        if(aw.getLibraryWell().getPlateNumber().equals(plateNumber) ) assayWells.add(aw);
      }
      return makeModel(new EntityCollection.AssayWells(AssayWell.class, assayWells));  //TODO: consider creating a AssayWellsRequestConverter, like the DataColumnValuesConverter, and stream a list of control type and library well urls -sde4
    }
    else {
      return makeNotFoundError();
    }
  }
  
  @RequestMapping(value = "/screens/{facilityId}/results/assaywells/{plateNumber}/{wellName}", method = RequestMethod.GET)
  public ModelAndView screenResultAssayWells(@PathVariable(value = "facilityId") String facilityId,
                                             @PathVariable(value = "plateNumber") Integer plateNumber,
                                             @PathVariable(value = "wellName") String wellName)
  {
    ModelAndView mav = makeNotFoundError();
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.assayWells));
    if (s != null && s.getScreenResult() != null) {
      for(AssayWell aw:s.getScreenResult().getAssayWells()) {
        if(aw.getLibraryWell().getPlateNumber().equals(plateNumber) && aw.getLibraryWell().getWellName().equals(wellName) ) {
          mav.addObject(MODEL_KEY, aw);
          break;
        }
      }
    }
    return mav;
  }
  
  /**
   * One row of Screen Result data, referenced by assaywell plate/well 
   */
  @RequestMapping(value = "/screens/{facilityId}/results/assaywells/{plateNumber}/{wellName}/values", method = RequestMethod.GET)
  public ModelAndView screenResultAssayWellValues(@PathVariable(value = "facilityId") String facilityId,
                                                  @PathVariable(value = "plateNumber") Integer plateNumber,
                                                  @PathVariable(value = "wellName") String wellName)
  {
    ModelAndView mav = makeNotFoundError();
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.assayWells));
    if (s != null && s.getScreenResult() != null) {
      Set<AssayWell> assayWells = Sets.newHashSet();
      for(AssayWell aw:s.getScreenResult().getAssayWells()) {
        if(aw.getLibraryWell().getPlateNumber().equals(plateNumber) && aw.getLibraryWell().getWellName().equals(wellName) ) {
          mav.addObject(MODEL_KEY, new AssayWellValuesConverter.AssayWellValuesRequest(aw));
          break;
        }
      }
    }
    return mav;
  }
  
	/**
	 * return the experimental cells used TODO: make this write out the ExperimentalCellInformation as soon as this data
	 * is defined and imported - sde4
	 */
	@RequestMapping(value = "/screens/{facilityId}/cells", method = RequestMethod.GET)
	public ModelAndView cellsForScreen(@PathVariable(value = "facilityId") String facilityId) {
		Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true,
				Screen.experimentalCellInfomationSet);
		if(s == null) return makeNotFoundError();
  	Set<Cell> cells = Sets.newHashSet();
		for(ExperimentalCellInformation eci:s.getExperimentalCellInformationSet()) {
			cells.add(eci.getCell());
		}
		return makeModel(new EntityCollection.Cells(cells));
	}	

	/**
	 * return the experimental cells used TODO: make this write out the ExperimentalCellInformation as soon as this data
	 * is defined and imported - sde4
	 */
	@RequestMapping(value = "/studies/{facilityId}/cells", method = RequestMethod.GET)
	public ModelAndView cellsForStudy(@PathVariable(value = "facilityId") String facilityId) {
		return cellsForScreen(facilityId);
	}
 
 /**
  * return the entire screen result 
  */
@RequestMapping(value = "/screens/{facilityId}/results", method = RequestMethod.GET)
public ModelAndView screenResult(@PathVariable(value = "facilityId") String facilityId)
{
  return makeModel(new ScreenResultValuesConverter.ScreenResult(genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId)));
}

/**
 * return the canonical set of reagents used for a screen result
 */
@RequestMapping(value = "/screens/{facilityId}/results/reagents", method = RequestMethod.GET)
public ModelAndView screenResultReagents(@PathVariable(value = "facilityId") String facilityId)
{
  Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.assayWells));
  if (s != null && s.getScreenResult() != null) {
  	Set<Reagent> reagents = Sets.newTreeSet(new Comparator<Reagent>(){
			@Override
			public int compare(Reagent o1, Reagent o2) {
				if(o1==o2) return 0;
				return o1.getWell().getFacilityId().compareTo(o2.getWell().getFacilityId());
			}});
  	for(AssayWell aw:s.getScreenResult().getAssayWells()) {
  		if(aw.getLibraryWell().getLatestReleasedReagent() != null) reagents.add(aw.getLibraryWell().getLatestReleasedReagent());
  	}
  	return makeModel(new EntityCollection.Reagents(reagents));
  }
  else {
    return makeNotFoundError();
  }
}

  @RequestMapping(value = "/screens/{facilityId}/results/columns", method = RequestMethod.GET)
  public ModelAndView screenResultColumns(@PathVariable(value = "facilityId") String facilityId)
  {
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.dataColumns));
    if (s != null && s.getScreenResult() != null) {
      return makeModel(new EntityCollection.ScreenColumns(s.getScreenResult().getDataColumns()));
    }
    else {
      return makeNotFoundError();
    }
  }

  @RequestMapping(value = "/screens/{facilityId}/results/columns/{ordinal}", method = RequestMethod.GET)
  public ModelAndView screenResultColumns(@PathVariable(value = "facilityId") String facilityId,
                                          @PathVariable(value = "ordinal") Integer ordinal)
  {
    ModelAndView mav = makeNotFoundError();
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.dataColumns));
    if(s!= null && s.getScreenResult() != null ) {
      for(DataColumn column:s.getScreenResult().getDataColumns()) {
        if (column.getOrdinal().equals(ordinal)) {
          mav.addObject(MODEL_KEY, column);
          break;
        }
      }
    }
    return mav;
  }
  
  @RequestMapping(value = "/screens/{facilityId}/results/columns/{ordinal}/values", method = RequestMethod.GET)
  public ModelAndView screenResultColumnValues(@PathVariable(value = "facilityId") String facilityId,
                                               @PathVariable(value = "ordinal") Integer ordinal)
  {
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.screenResult.to(ScreenResult.dataColumns));
    if (s != null && s.getScreenResult() != null) {
      for (DataColumn column : s.getScreenResult().getDataColumns()) {
        if (column.getOrdinal().equals(ordinal)) {
          return makeModel(new DataColumnValuesConverter.DataColumnValuesRequest(column));
        }
      }
    }
    return makeNotFoundError();
  }
  
  @RequestMapping(value = "/libraries", method = RequestMethod.GET)
  public ModelAndView libraries()
  {
    return makeModel(new EntityCollection.Libraries(Library.class, genericEntityDao.findAllEntitiesOfType(Library.class)));
  }

  @RequestMapping(value = "/libraries/{shortName}", method = RequestMethod.GET)
  public ModelAndView library(@PathVariable(value = "shortName") String shortName)
  {
    return makeModel(genericEntityDao.findEntityByProperty(Library.class, "shortName", shortName));
  }

  @RequestMapping(value = "/plates/{plateNumber}/wells/{wellName}", method = RequestMethod.GET)
  public ModelAndView well(@PathVariable(value = "plateNumber") Integer plateNumber,
                           @PathVariable(value = "wellName") String wellName)
  {
    return makeModel(librariesDao.findWell(new WellKey(plateNumber, wellName)));
  }
  
  @RequestMapping(value = "/plates/{plateNumber}/wells/{wellName}/molfile", method = RequestMethod.GET)
  public ModelAndView wellMolfile(@PathVariable(value = "plateNumber") Integer plateNumber,
                           @PathVariable(value = "wellName") String wellName, HttpServletResponse response)
  {
    Well well = librariesDao.findWell(new WellKey(plateNumber, wellName));
    if(well == null)
    {
      log.warn("plate/well not found: " + plateNumber + "/" + wellName);  // TODO: report errors to user
      return makeNotFoundError();
    }
    streamMolFile(well, response);
    return null;
  }

  @RequestMapping(value = "/plates/{plateNumber}/wells/{wellName}", method = RequestMethod.GET, headers="accept=chemical/x-mdl-sdfile")
  public void wellSdf(@PathVariable(value = "plateNumber") Integer plateNumber,
                           @PathVariable(value = "wellName") String wellName, HttpServletResponse response)
  {
    Well well = librariesDao.findWell(new WellKey(plateNumber, wellName));
    if(well == null)
    {
      log.warn("plate/well not found: " + plateNumber + "/" + wellName);  // TODO: report errors to user
      return;
    }
    streamSdFile(well, response);
  }

  @RequestMapping(value = "/plates/{plateNumber}/wells/{wellName}", method = RequestMethod.GET, headers="accept=chemical/x-mdl-molfile")
  public void wellMolfile1(@PathVariable(value = "plateNumber") Integer plateNumber,
                           @PathVariable(value = "wellName") String wellName, HttpServletResponse response)
  {
    wellMolfile(plateNumber, wellName, response);
  }

  @RequestMapping(value = "/reagents", method = RequestMethod.GET)
  public ModelAndView reagents()
  {
    return makeModel(new EntityCollection.Reagents(Sets.newHashSet(Iterables.transform(librariesDao.findAllCanonicalReagentWells(), Well.toLatestReleasedReagent))));
  }
  
  /**
   * @return the canonical well for a given compound Facility ID
   */
  @RequestMapping(value = "/reagents/{facilityId}", method = RequestMethod.GET)
  public ModelAndView reagent(@PathVariable(value = "facilityId") String facilityId)
  {
    return makeModel(librariesDao.findCanonicalReagentWell(facilityId, null, null));
  }
  
  @RequestMapping(value = "/reagents/{facilityId}", method = RequestMethod.GET, headers="accept=chemical/x-mdl-sdfile")
  public void reagentSdFile(@PathVariable(value = "facilityId") String facilityId, HttpServletResponse response)
  { 
    Well well = librariesDao.findCanonicalReagentWell(facilityId, null, null);
    if(well == null)
    {
      log.warn("well facility id not found: " + facilityId); // TODO: report errors to user
      return;
    }
    streamSdFile(well, response);
  } 

  @RequestMapping(value = "/reagents/{facilityId}", method = RequestMethod.GET, headers="accept=chemical/x-mdl-molfile")
  public void reagentMolfile(@PathVariable(value = "facilityId") String facilityId, HttpServletResponse response)
  { 
    Well well = librariesDao.findCanonicalReagentWell(facilityId, null, null);
    if(well == null)
    {
      log.warn("well facility id not found: " + facilityId); // TODO: report errors to user
      return;
    }
    streamMolFile(well, response);
  }

  @RequestMapping(value = "/studies/{facilityId}", method = RequestMethod.GET)
  public ModelAndView study(@PathVariable(value = "facilityId") String facilityId)
  {
    return screen(facilityId).addObject("entityClass", Study.class);
  }  
  
  @RequestMapping(value = "/studies/{facilityId}/results/columns", method = RequestMethod.GET)
  public ModelAndView studyColumns(@PathVariable(value = "facilityId") String facilityId)
  {
    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.annotationTypes);
    if (s != null && s.getAnnotationTypes() !=null) {
      Set<AnnotationType> set = Sets.newHashSet(s.getAnnotationTypes());
      return makeModel(new EntityCollection.StudyColumns(AnnotationType.class, set));
    }
    else {
      return makeNotFoundError();
    }
  }
  
  @RequestMapping(value = "/studies/{facilityId}/results/columns/{ordinal}", method = RequestMethod.GET)
  public ModelAndView studyColumn(@PathVariable(value = "facilityId") String facilityId, @PathVariable(value = "ordinal") Integer ordinal)
  {
    ModelAndView mav = makeNotFoundError();

    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.annotationTypes);
    if (s != null && s.getAnnotationTypes() !=null) {
      for(AnnotationType at:s.getAnnotationTypes()) {
        if(at.getOrdinal().equals(ordinal))
        {
          mav.addObject(MODEL_KEY, at);
          break;
        }
      }
    }
    return mav;
  }

  @RequestMapping(value = "/studies/{facilityId}/results/columns/{ordinal}/values", method = RequestMethod.GET)
  public ModelAndView studyColumnValues(@PathVariable(value = "facilityId") String facilityId, @PathVariable(value = "ordinal") Integer ordinal)
  {
    ModelAndView mav = makeNotFoundError();

    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.annotationTypes);
    if (s != null && s.getAnnotationTypes() !=null) {
      for(AnnotationType at:s.getAnnotationTypes()) {
        if(at.getOrdinal().equals(ordinal))
        {
          mav.addObject(MODEL_KEY, new StudyColumnValuesConverter.StudyColumnValuesRequest(at));
          break;
        }
      }
    }
    return mav;
  }
  
  @RequestMapping(value = "/studies/{facilityId}/results/reagents", method = RequestMethod.GET)
  public ModelAndView studyResultReagents(@PathVariable(value = "facilityId") String facilityId)
  {
    ModelAndView mav = makeNotFoundError();

    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.reagents);
    if (s != null && s.getReagents() !=null) {
      mav.addObject(MODEL_KEY, new StudyReagentsCanonicalConverter.StudyReagentsCanonical(Reagent.class, s.getReagents()));
    }
    return mav;
  }  
  
  /**
   * One row of data from the study
   */
  @RequestMapping(value = "/studies/{facilityId}/results/reagents/{wellFacilityId}/values", method = RequestMethod.GET)
  public ModelAndView studyResultReagentValues(@PathVariable(value = "facilityId") String facilityId, @PathVariable(value = "wellFacilityId") String wellFacilityId)
  {
    ModelAndView mav = makeNotFoundError();

    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.reagents.to(Reagent.well));
    if (s != null && s.getReagents() !=null) {
      for(Reagent r:s.getReagents())
      {
        if(r.getWell().getFacilityId().equals(wellFacilityId) ){
          mav.addObject(MODEL_KEY, new StudyRowConverter.StudyRow( s, r));
          break;
        }
      }
    }
    return mav;
  }  
  
  /**
   * Get the whole study at once
   */
  @RequestMapping(value = "/studies/{facilityId}/results", method = RequestMethod.GET)
  public ModelAndView studyResults(@PathVariable(value = "facilityId") String facilityId)
  {
    ModelAndView mav = makeNotFoundError();

    Screen s = genericEntityDao.findEntityByProperty(Screen.class, "facilityId", facilityId, true, Screen.reagents.to(Reagent.well));
    if (s != null && s.getAnnotationTypes() !=null) {
      mav.addObject(MODEL_KEY, new StudyValuesConverter.StudyValues(s));
    }
    return mav;
  }

  @RequestMapping(value = "/persons/{id}", method = RequestMethod.GET)
  public ModelAndView person(@PathVariable(value = "id") Integer id)
  {
    // TODO: note that if the same user is loaded as a ScreeningRoomUser and a LabHead from the same scope (session) then the first of these methods is the *only* one that will be used -sde4
    return makeModel(genericEntityDao.findEntityById(ScreensaverUser.class, id));
  }
    
  @RequestMapping(value = "/publications/{id}", method = RequestMethod.GET)
  public ModelAndView publication(@PathVariable(value = "id") Integer id)
  {
    return makeModel(genericEntityDao.findEntityById(Publication.class, id));
  }
    
  @RequestMapping(value = "/attachedfiles/{id}", method = RequestMethod.GET)
  public ModelAndView downloadFile(@PathVariable(value = "id") final Integer id, final HttpServletResponse response)
  {
    final boolean[] result = new boolean[] { true };
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        AttachedFile attachedFile = genericEntityDao.findEntityById(AttachedFile.class, id, true, AttachedFile.reagent);
        if(attachedFile == null) return;
        attachedFile = (AttachedFile)attachedFile.restrict();
        if(attachedFile.getFileContents() == null ) return;
        response.setContentType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(attachedFile.getFilename()));
        response.setContentLength(attachedFile.getFileContents().length);
        response.setHeader("Content-Disposition","attachment; filename=\"" + attachedFile.getFilename() +"\"");
        try {
          IOUtils.copy(new ByteArrayInputStream(attachedFile.getFileContents()), response.getOutputStream());
        }
        catch (IOException e) {
          log.error("on file download request: id: " + id , e);
          result[0] = false;
        }        
      }
    });
    if(!result[0])  return makeNotFoundError(); // TODO better error handling
    return null;
  }
  

  @RequestMapping(value = "/cells", method = RequestMethod.GET)
  public ModelAndView cells()
  {
    return makeModel(new EntityCollection.Cells(genericEntityDao.findAllEntitiesOfType(Cell.class)));
  }
  
  /**
   * @return the canonical well for a given compound Facility ID
   */
  @RequestMapping(value = "/cells/{facilityId}", method = RequestMethod.GET)
  public ModelAndView cell(@PathVariable(value = "facilityId") String facilityId)
  {
    return makeModel(genericEntityDao.findEntityByProperty(Cell.class, "facilityId", facilityId));
  }
  
  /**
   * @return the screens that have used the cell of the given HMS Facility ID
   */
  @RequestMapping(value = "/cells/{facilityId}/screens", method = RequestMethod.GET)
  public ModelAndView screensForCell(@PathVariable(value = "facilityId") String facilityId)
  {
  	Cell cell = genericEntityDao.findEntityByProperty(Cell.class, "facilityId", facilityId, true, Cell.experimentalCellInformationSetPath);
  	if(cell == null) return makeNotFoundError();
  	// TODO, find out why the fetched cells.getExperimentalInformationSet only ever returns the first attached screen
  	Set<ExperimentalCellInformation> ecis = Sets.newHashSet(genericEntityDao.findEntitiesByProperty(ExperimentalCellInformation.class, "cell", cell));
  	Set<Screen> screens = Sets.newHashSet();
		for(ExperimentalCellInformation eci:ecis) {
//  	for(ExperimentalCellInformation eci:cell.getExperimentalCellInformationSet()) {
  			if(!eci.getScreen().isStudyOnly()) screens.add(eci.getScreen());
  		}
    return makeModel(new EntityCollection.Screens(Screen.class, screens));
  }
  
  /**
   * @return the screens that have used the cell of the given HMS Facility ID
   */
  @RequestMapping(value = "/cells/{facilityId}/studies", method = RequestMethod.GET)
  public ModelAndView studiesForCell(@PathVariable(value = "facilityId") String facilityId)
  {
  	Cell cell = genericEntityDao.findEntityByProperty(Cell.class, "facilityId", facilityId, true, Cell.experimentalCellInformationSetPath);
  	if(cell == null) return makeNotFoundError();
  	// TODO, find out why the fetched cells.getExperimentalInformationSet only ever returns the first attached screen
  	Set<ExperimentalCellInformation> ecis = Sets.newHashSet(genericEntityDao.findEntitiesByProperty(ExperimentalCellInformation.class, "cell", cell));
  	Set<Screen> studies = Sets.newHashSet();
		for(ExperimentalCellInformation eci:ecis) {
			if(eci.getScreen().isStudyOnly()) studies.add(eci.getScreen());
		}
    return makeModel(new EntityCollection.Studies(Study.class, studies));
  }
  
  // Utility methods
  
  private void streamSdFile(Well well, HttpServletResponse response)
  {
    try {
      IOUtils.copy(sdfWellsDataExporter.export(ImmutableSet.of(well.getWellKey().toString()).iterator()), response.getOutputStream());
    }
    catch (IOException e) {
      log.error("Error on file sdf download request: well: " + well.getWellId(), e);
    }
  }  
  
  private void streamMolFile(Well well, HttpServletResponse response)
  {
    try {
      well = genericEntityDao.reloadEntity(well, true, Well.latestReleasedReagent.to(SmallMoleculeReagent.molfileList));
      if(! SmallMoleculeReagent.class.isAssignableFrom(well.getLatestReleasedReagent().getClass())) {
        log.warn("can only return a molfile for small molecule reagents, request for well: " + well.getWellId());
      }
      SmallMoleculeReagent smr = (SmallMoleculeReagent)well.getLatestReleasedReagent().restrict();
      if(smr.getMolfile() == null) return;
      IOUtils.copy( new ByteArrayInputStream((smr).getMolfile().getBytes()), response.getOutputStream());
    }
    catch (IOException e) {
      log.error("Error on file sdf download request: well: " + well.getWellId(), e);
    }
  }  
  
  private ModelAndView makeModel(Object data)
  {
    if (data != null) return new ModelAndView().addObject(MODEL_KEY, data);
    else return makeNotFoundError();
  }

  private ModelAndView makeNotFoundError()
  {
  	log.info("not found!!");
    return makeModel(new ErrorConverter.ErrorContainer(ERROR_OBJECT_ID_NOT_FOUND));
  }

  @ExceptionHandler(Exception.class)
  public ModelAndView handleException(Exception ex) 
  {
    log.info("handling ex: " + ex.getLocalizedMessage(), ex);
    return makeModel(new ErrorConverter.ErrorContainer(ex, ex.getLocalizedMessage()));
  }
}
