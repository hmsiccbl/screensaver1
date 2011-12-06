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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;


public class PlateReaderRawDataTransformerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(PlateReaderRawDataTransformerTest.class);
  
  @Autowired
  protected PlateReaderRawDataTransformer plateReaderRawDataTransformer;

  public void testForm1Serialization() throws IOException
  {
    PlateReaderRawDataTransformer.FormOne form1a = new PlateReaderRawDataTransformer.FormOne();
    form1a.setNegativeControls("1 A1\n 1 A2");
    form1a.setPositiveControls("2 A1, 3 A1");
    form1a.setOtherControls("4 B1, 5 B2");
    form1a.setPlates("1 2 3");
    
    String serialized = form1a.serialize();
    PlateReaderRawDataTransformer.FormOne form1b = new PlateReaderRawDataTransformer.FormOne(serialized);
    
    assertEquals(form1a.getNegativeControls(), form1b.getNegativeControls());
    assertEquals(form1a.getPositiveControls(), form1b.getPositiveControls());
    assertEquals(form1a.getOtherControls(), form1b.getOtherControls());
    assertEquals(form1a.getPlates(), form1b.getPlates());
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
