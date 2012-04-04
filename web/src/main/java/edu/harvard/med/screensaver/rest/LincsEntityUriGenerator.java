
package edu.harvard.med.screensaver.rest;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.ServiceActivity;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.NaturalProductReagent;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AbaseTestset;
import edu.harvard.med.screensaver.model.screens.CellLine;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.EquipmentUsed;
import edu.harvard.med.screensaver.model.screens.FundingSupport;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.TransfectionAgent;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;

public class LincsEntityUriGenerator implements EntityUriGenerator<String>
{
  private static final Logger log = Logger.getLogger(LincsEntityUriGenerator.class);

  @Autowired
  private HttpServletRequest request;

  protected LincsEntityUriGenerator()
  {}

  @Override
  public String visit(AttachedFile entity)
  {
    return getUrl("/attachedfiles/" + entity.getEntityId());  
  }

  @Override
  public String visit(LabHead labHead)
  {
    return visit((ScreensaverUser) labHead);
  }

  @Override
  public String visit(Library entity)
  {
      return getUrl("/libraries/" + entity.getShortName());
  }

  @Override
  public String visit(Publication entity)
  {
     return getUrl("/publications/" + entity.getEntityId());
  }

  @Override
  public String visit(ResultValue entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(DataColumn entity)
  {
    return visit(entity.getScreenResult().getScreen()) + "/columns/" + entity.getOrdinal();
  }

  @Override
  public String visit(Screen screen)
  {
    return getUrl("/screens/" + screen.getFacilityId());
  }

  @Override
  public String visit(ScreeningRoomUser screeningRoomUser)
  {
    return visit((ScreensaverUser) screeningRoomUser);
  }

  @Override
  public String visit(Study study)
  {
    return getUrl("/studies/" + study.getFacilityId());
  }

  @Override
  public String visit(Well entity)
  {
    return getUrl("/plates/" + entity.getPlateNumber() + "/wells/" + entity.getWellName());
  }

  @Override
  public String visit(AbaseTestset entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(AdministratorUser administratorUser)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(AdministrativeActivity administrativeActivity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(AnnotationType at)
  {
    return getUrl("/studies/" + at.getStudy().getFacilityId() + "/columns/" + at.getOrdinal() );
  }

  @Override
  public String visit(AnnotationValue annotationValue)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(AssayPlate assayPlate)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(AssayWell assayWell)
  {
    return getUrl("/screens/" + assayWell.getScreenResult().getScreen().getFacilityId() + "/assaywells/" + assayWell.getLibraryWell().getPlateNumber() + "/wells/" + assayWell.getLibraryWell().getWellName() );
  }

  @Override
  public String visit(AttachedFileType attachedFileType)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(ChecklistItemEvent entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(ChecklistItem entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(CherryPickAssayPlate entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(CherryPickLiquidTransfer entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(SmallMoleculeReagent entity)
  {
    return visit((Reagent) entity);
  }

  @Override
  public String visit(SmallMoleculeCherryPickRequest entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(Copy entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(Plate entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(PlateLocation entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(EquipmentUsed entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(FundingSupport fundingSupport)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(Gene entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(LabAffiliation entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(LabCherryPick entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(LibraryContentsVersion libraryContentsVersion)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(LibraryScreening entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(NaturalProductReagent entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(RNAiCherryPickRequest entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(CherryPickScreening entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(ScreenResult screenResult)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(ScreenerCherryPick entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(ServiceActivity serviceActivity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(SilencingReagent entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(WellVolumeCorrectionActivity entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(CellLine cellLine)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String visit(TransfectionAgent transfectionAgent)
  {
    // TODO Auto-generated method stub
    return null;
  }

  private String visit(ScreensaverUser u)
  {
    return getUrl("/persons/" + u.getEntityId());
  }

  private String getUrl(String path)
  {
    return this.request.getRequestURL().substring(0, this.request.getRequestURL().indexOf(this.request.getServletPath()) + this.request.getServletPath().length()) + path;
  }

  private String visit(Reagent entity)
  {
    return getUrl("/reagents/" + entity.getWell().getFacilityId());
  }

	@Override
	public String visit(ExperimentalCellInformation entity) {
		return null; // TODO
	}

	@Override
	public String visit(Cell entity) {
		return getUrl("/cells/" + entity.getFacilityId());
	}
}
