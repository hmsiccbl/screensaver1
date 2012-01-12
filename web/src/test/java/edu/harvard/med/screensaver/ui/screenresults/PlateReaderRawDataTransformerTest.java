// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/infrastructure-upgrade/test/edu/harvard/med/screensaver/ui/users/UserViewerTest.java $
// $Id: UserViewerTest.java 5202 2011-01-21 22:16:57Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFileDefaultMemoryImpl;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataTransformer.FormOne;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataTransformer.InputFileParams;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataTransformer.OutputFormat;


public class PlateReaderRawDataTransformerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(PlateReaderRawDataTransformerTest.class);
  
  @Autowired
  protected PlateReaderRawDataTransformer plateReaderRawDataTransformer;

  public void testForm1Serialization() throws IOException
  {
    PlateReaderRawDataTransformer.FormOne form1a = new PlateReaderRawDataTransformer.FormOne();
    form1a.setAssayPlateSize(PlateSize.WELLS_96);
    form1a.setAssayNegativeControls("1 A1\n 1 A2");
    form1a.setAssayPositiveControls("2 A1, 3 A1");
    form1a.setAssayOtherControls("4 B1, 5 B2");
    form1a.setLibraryControls("A4=plk1");
    form1a.setPlates("1 2 3");
    form1a.setOutputFormat(OutputFormat.AllPlatesInSingleWorksheet);
    
    String serialized = form1a.serialize();
    PlateReaderRawDataTransformer.FormOne form1b = new PlateReaderRawDataTransformer.FormOne(serialized);
    
    assertEquals(form1a.getAssayPlateSize(), form1b.getAssayPlateSize());
    assertEquals(form1a.getAssayNegativeControls(), form1b.getAssayNegativeControls());
    assertEquals(form1a.getAssayPositiveControls(), form1b.getAssayPositiveControls());
    assertEquals(form1a.getAssayOtherControls(), form1b.getAssayOtherControls());
    assertEquals(form1a.getLibraryControls(), form1b.getLibraryControls());
    assertEquals(form1a.getPlates(), form1b.getPlates());
    assertEquals(form1a.getOutputFormat(), form1b.getOutputFormat());
  }

  public void testForm2Serialization() throws IOException
  {
    PlateReaderRawDataTransformer.InputFileParams form2a = new PlateReaderRawDataTransformer.InputFileParams();
    List<PlateOrderingGroup> list = Lists.newArrayList(PlateOrderingGroup.Conditions, PlateOrderingGroup.Replicates, PlateOrderingGroup.Readouts, PlateOrderingGroup.Plates);
    form2a.setCollationOrderOrdering(list);
    form2a.setConditions("x y z");
    form2a.setReadouts("1 2 3");
    form2a.setReplicates(7);
    form2a.setReadoutTypeSelection(AssayReadoutType.PHOTOMETRY);
    
    String serialized = form2a.serialize();
    PlateReaderRawDataTransformer.InputFileParams form2b = PlateReaderRawDataTransformer.InputFileParams.deserialize(serialized);
    
    assertEquals(form2a.getCollationOrderOrdering(), form2b.getCollationOrderOrdering());
    assertEquals(form2a.getConditions(), form2b.getConditions());
    assertEquals(form2a.getReadouts(), form2b.getReadouts());
    assertEquals(form2a.getReplicates(), form2b.getReplicates());
    assertEquals(form2a.getReadoutTypeSelection(), form2b.getReadoutTypeSelection());
  }

  public void testParseConditions() throws IOException
  {
    PlateReaderRawDataTransformer.InputFileParams p = new PlateReaderRawDataTransformer.InputFileParams();
    p.setConditions("x,y y, z");
    assertEquals(ImmutableList.of("x", "y y", "z"), p.getParsedConditions());
    p.setConditions("x,y y, z");
    assertEquals(ImmutableList.of("x", "y y", "z"), p.getParsedConditions());
    p.setConditions("x\r\ny y \n z");
    assertEquals(ImmutableList.of("x", "y y", "z"), p.getParsedConditions());
  }
}
