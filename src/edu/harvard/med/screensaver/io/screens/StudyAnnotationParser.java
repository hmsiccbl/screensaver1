// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.Sheet;
import jxl.WorkbookSettings;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.UnrecoverableParseException;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class StudyAnnotationParser
{
  private static Logger log = Logger.getLogger(StudyAnnotationParser.class);
  private static final String UNKNOWN_ERROR = "unknown error";

  private GenericEntityDAO _dao;
  
  /** @motivation for CGLIB2 */
  protected StudyAnnotationParser() {}
    

  public StudyAnnotationParser(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  @Transactional(readOnly=true)
  public void parse(Screen study,
                    InputStream workbookIn)
  {
    try {
      WorkbookSettings workbookSettings = new WorkbookSettings();
      workbookSettings.setGCDisabled(true); // when GC feature is enabled, performance is much slower!
      jxl.Workbook workbook = jxl.Workbook.getWorkbook(workbookIn, workbookSettings); 
      parseAnnotationTypes(workbook, study);
      parseAnnotationValues(workbook, study);
    }
    catch (Exception e) {
      throw new UnrecoverableParseException(e);
    }
    finally {
      IOUtils.closeQuietly(workbookIn);
    }
  }

  private void parseAnnotationTypes(jxl.Workbook workbook, Screen study)
  {
    Sheet sheet = workbook.getSheet(0);
    Cell[] annotationNames = sheet.getRow(0);
    Cell[] annotationDescriptions = sheet.getRow(1);
    Cell[] annotationIsNumericFlags = sheet.getRow(2);
    for (int i = 0; i < annotationNames.length; ++i) {
      /*AnnotationType annotationType =*/
        new AnnotationType(study,
                           annotationNames[i].getContents(),
                           annotationDescriptions[i].getContents(),
                           i,
                           ((BooleanCell) annotationIsNumericFlags[i]).getValue());
    }
  }

  private void parseAnnotationValues(jxl.Workbook workbook, Screen study)
  {
    List<AnnotationType> annotationTypes = Lists.newArrayList(study.getAnnotationTypes());
    for (int iSheet = 1; iSheet < workbook.getNumberOfSheets(); ++iSheet) {
      Sheet sheet = workbook.getSheet(iSheet);
      for (int iRow = 0; iRow < sheet.getRows(); ++iRow) {
        Cell reagentIdCell = sheet.getCell(0, iRow);
        String reagentId = reagentIdCell.getContents();
        ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(reagentId.split(":", 2)[0], 
                                                                  reagentId.split(":", 2)[1]);
        
        Map<String,Object> queryProps = Maps.newHashMap();
        queryProps.put(Reagent.vendorIdentifier.getPath(), rvi.getVendorIdentifier());
        queryProps.put(Reagent.vendorName.getPath(), rvi.getVendorName());
        List<Reagent> reagents = _dao.findEntitiesByProperties(Reagent.class, 
                                                               queryProps, 
                                                               true, 
                                                               Reagent.studies.getPath());
        if (reagents.isEmpty()) {
          throw new UnrecoverableParseException("no such reagent with identifier " + rvi);
        }
        for (Reagent reagent : reagents) {
          _dao.needReadOnly(reagent, Reagent.annotationValues.getPath());
          study.addReagent(reagent);
          for (int iAnnot = 0; iAnnot < study.getAnnotationTypes().size(); ++iAnnot) {
            int iCol = iAnnot + 1;
            AnnotationType annotationType = annotationTypes.get(iAnnot);
            Cell valueCell = sheet.getCell(iCol, iRow);
            annotationType.createAnnotationValue(reagent, valueCell.getContents());
          }
        }
      }
    }
  }
}
