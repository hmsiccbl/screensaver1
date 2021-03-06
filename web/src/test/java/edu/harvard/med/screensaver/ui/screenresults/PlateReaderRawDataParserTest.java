package edu.harvard.med.screensaver.ui.screenresults;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataParser.MatrixOrder;
import edu.harvard.med.screensaver.ui.screenresults.PlateReaderRawDataParser.Odometer;


public class PlateReaderRawDataParserTest extends TestCase
{
  private static Logger logger = Logger.getLogger(PlateReaderRawDataParserTest.class);

  
  public static Function<String,WellName> wellNameTransformer = 
      new Function<String,WellName>() {
		@Override
		public WellName apply(String input) {
			return new WellName(input);
		}
	};

	public void testNamedWellRangeParser() {
		String range1Label = "test range 1";
		String range2Label = "test range 2";
		String input = "A1-C2, F3=\"" + range1Label + "\"\nP23-Q24=\"" + range2Label + "\"";
		int plateSize = 384;
		Set<WellName> expected1 = Sets.newHashSet(
				Collections2.transform(
						Lists.newArrayList( 
						    "A1", "A2", "B1", "B2", "C1", "C2", "F3"),wellNameTransformer));
		Set<WellName> expected2 = Sets.newHashSet(
				Collections2.transform(
						Lists.newArrayList( "P23", "P24", "Q23", "Q24"),wellNameTransformer));
		Map<String,Set<WellName>> parsedOutput = 
		    PlateReaderRawDataParser.expandNamedWellRanges(input, plateSize);
		
		assertTrue("does not contain range 1: " + parsedOutput, parsedOutput.containsKey(range1Label));
		assertTrue("does not contain range 2: " + parsedOutput, parsedOutput.containsKey(range2Label));
		assertEquals("range 1 wrong", parsedOutput.get(range1Label), expected1);
		assertEquals("range 2 wrong", parsedOutput.get(range2Label), expected2);
	}
  
  public void testPlateRangeParser() {
    String range = "1-3, 5";
    Integer[] expected = { 1,2,3,5 };
    Integer[] actual = PlateReaderRawDataParser.expandPlatesArg(range);
//    assertEquals("expected, returned: " + Joiner.on(",").join(expected) 
//        + ", " + Joiner.on(",").join(actual) ,expected, actual);
    assertEquals(Joiner.on(",").join(expected),Joiner.on(",").join(actual));
  } 
  
  public void testDescendingPlateRangeParser() {
    String range = "3-1, 5";
    Integer[] expected = { 3,2,1,5 };
    Integer[] actual = PlateReaderRawDataParser.expandPlatesArg(range);
//    assertEquals("expected, returned: " + Joiner.on(",").join(expected) 
//        + ", " + Joiner.on(",").join(actual) ,expected, actual);
    assertEquals(Joiner.on(",").join(expected),Joiner.on(",").join(actual));
  }
	
	public void testWellRangeParser() {
		String input = "O19-Q22";
		int plateSize = 384;
		Set<WellName> expected1 = Sets.newHashSet(
				Collections2.transform(
						Lists.newArrayList( "O19", "O20", "O21", "O22", "P19", "P20", "P21", 
						    "P22", "Q19", "Q20", "Q21", "Q22" ),wellNameTransformer));
		
		Set<WellName> output = PlateReaderRawDataParser.expandWellRange(input, plateSize);
		assertEquals(expected1,output);
		
		input = "P";
		Set<WellName> expected2 = Sets.newHashSet();
//		for(int i=0;i<24;i++) expected2[i-1] = "P" + PlateReaderRawDataParser.getCol(i);
		for(int i=1;i<=24;i++) expected2.add(new WellName("P" + i));
		
		output = PlateReaderRawDataParser.expandWellRange(input, plateSize);
		assertEquals(expected2,output);

		// multiple range
		input = "O19-Q22,P";

		output = PlateReaderRawDataParser.expandWellRange(input, plateSize);
		expected2.addAll(Sets.newHashSet(expected1));
		assertEquals(expected2,output);

		
    // Note; this is the _users_ input, so it should be interpreted as a "4", 
		// which will be column index 3 (zero based)
		int inputI = 4; 
		Set<WellName> expected = Sets.newHashSet();
		for(int i=0;i<16;i++) expected.add(new WellName(i,inputI-1));
		
		output = PlateReaderRawDataParser.expandWellRange("" + inputI, plateSize);
		assertEquals(expected,output);
		
		// 1536
		inputI = 4;
		expected = Sets.newHashSet();
		for(int i=0;i<32;i++) expected.add(new WellName(i,inputI-1));
		
		output = PlateReaderRawDataParser.expandWellRange("" + inputI, 1536);
		assertEquals(expected,output);
	}
	
	public void testConvertWell() {
		
		int aps = 96;
		int lps = 384;
		int quadrant = 0;
		WellName sourceWell = new WellName("A1");
		WellName expectedWell = new WellName("A1");
		
		assertEquals(expectedWell, 
		    PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
		
		sourceWell = new WellName("A2");
		expectedWell = new WellName("A3");
		assertEquals(expectedWell, 
		    PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));

    quadrant = 3;
    sourceWell = new WellName("A2");
    expectedWell = new WellName("B4");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    quadrant = 3;
    sourceWell = new WellName("H12");
    expectedWell = new WellName("P24");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    quadrant = 2;
    sourceWell = new WellName("H12");
    expectedWell = new WellName("P23");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));

    quadrant = 2;
    sourceWell = new WellName("G12");
    expectedWell = new WellName("N23");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));

    quadrant = 2;
    sourceWell = new WellName("H11");
    expectedWell = new WellName("P21");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    quadrant = 2;
    sourceWell = new WellName("G11");
    expectedWell = new WellName("N21");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    aps = 384;
    lps = 96;
    quadrant = 0;
    sourceWell = new WellName("P24");
    expectedWell = new WellName("H12");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    sourceWell = new WellName("N21");
    expectedWell = new WellName("G11");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    sourceWell = new WellName("A02");
    expectedWell = new WellName("A01");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    sourceWell = new WellName("A03");
    expectedWell = new WellName("A02");
    assertEquals(expectedWell, 
        PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
    
    quadrant = 0;
		aps = 1536;
		lps = 384;
		sourceWell = new WellName("A2");
		expectedWell = new WellName("A1");
		assertEquals(expectedWell, 
		    PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
		
		quadrant = 0;
		aps = 1536;
		lps = 384;
		sourceWell = new WellName("B2");
		expectedWell = new WellName("A1");
		int expectedQuadrant = 3;
		assertEquals(expectedWell, 
		    PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
		assertEquals(expectedQuadrant, 
		    PlateReaderRawDataParser.deconvoluteMatrix(
		        aps, lps, sourceWell.getRowIndex(), sourceWell.getColumnIndex()));
		
		aps = 1536;
		lps = 384;
		sourceWell = new WellName("B3");
		expectedWell = new WellName("A2");
		expectedQuadrant = 2;
		assertEquals(expectedWell, 
		    PlateReaderRawDataParser.convertWell(sourceWell, aps, lps, quadrant));
		assertEquals(expectedQuadrant, 
		    PlateReaderRawDataParser.deconvoluteMatrix(
		        aps, lps, sourceWell.getRowIndex(), sourceWell.getColumnIndex()));
		
	}
	
  public void testConvertMatrix96()
  {
    // first, convert 96 to 386
    Integer[] plates = new Integer[] {1};
    String[] conditions = new String[] {"a","b","c","d","e","f","g"};
    String[] replicates = new String[] {"1",};
    String[] readouts = new String[] {"1"};
    CollationOrder ordering = CollationOrder.getOrder("RPQC");
    PlateReaderRawDataParser.MatrixOrder matrixOrder = 
        new PlateReaderRawDataParser.MatrixOrder(
            ordering, plates,conditions,readouts, replicates);    
  
    // create the source data
    List<List<String[]>> sourceMatrices = Lists.newArrayList();
    for(int i=0; i<28; i++) {
      List<String[]> sourceMatrix = Lists.newArrayList();
      sourceMatrices.add(sourceMatrix);
      for(int j=0;j<8;j++) {
        String[] row = new String[12];
        sourceMatrix.add(row);
        for(int k=0;k<12;k++) {
          row[k] = "Q"+i+"C"+k+"R"+j;
        }
      }
    }
    
    List<List<String[]>> combinedMatrices = 
        PlateReaderRawDataParser.convertMatrixFormat(
            96,384,matrixOrder.getDeconvolutedMatrixOrder(), sourceMatrices);
    
    assertEquals("wrong number of result matrices", 7, combinedMatrices.size());
    assertEquals("rows wrong", 16, combinedMatrices.get(0).size());
    assertEquals("cols wrong", 24, combinedMatrices.get(0).get(0).length);

    // Test by converting back to 96!
    
    List<List<String[]>> decombinedMatrices = 
        PlateReaderRawDataParser.convertMatrixFormat(
            384, 96, matrixOrder.getDeconvolutedMatrixOrder(), combinedMatrices);

    assertEquals("wrong number of result matrices", 28, decombinedMatrices.size());
    assertEquals("rows wrong", 8, decombinedMatrices.get(0).size());
    assertEquals("cols wrong", 12, decombinedMatrices.get(0).get(0).length);  
    //assertEquals(sourceMatrices,decombinedMatrices);
    for(int i=0;i<28;i++) {
      for(int j=0;j<8;j++) {
        for(int k=0;k<12;k++) {
          assertEquals("M: " + i + " C"+k+"R"+j, 
              sourceMatrices.get(i).get(j)[k], decombinedMatrices.get(i).get(j)[k]);
        }
      }
    }
  }
  
  public void testConvertMatrix1536()
  {
    
    // 1536
    Integer[] plates = new Integer[] {1,2,3,4};
    String[] conditions = new String[] {"a","b","c","d","e","f","g"};
    String[] replicates = new String[] {"1",};
    String[] readouts = new String[] {"1"};
    CollationOrder ordering = CollationOrder.getOrder("RPQC");
    
    PlateReaderRawDataParser.MatrixOrder1536 matrixOrder1536 = 
        new PlateReaderRawDataParser.MatrixOrder1536(
            ordering, plates,conditions,readouts, replicates);  
    
    List<List<String[]>> sourceMatrices = Lists.newArrayList();
    for(int i=0;i<7;i++) {
      List<String[]> sourceMatrix = Lists.newArrayList();
      sourceMatrices.add(sourceMatrix);
      for(int j=0;j<32;j++) {
        String[] row = new String[48];
        sourceMatrix.add(row);
        for(int k=0;k<48;k++) {
          row[k] = "C"+k+"R"+j;
        }
      }
    }
    
    List<List<String[]>> decombinedMatrices = 
        PlateReaderRawDataParser.convertMatrixFormat(
            1536,384,matrixOrder1536, sourceMatrices);
    
    assertEquals("wrong number of result matrices", 28, decombinedMatrices.size());
    assertEquals("rows wrong", 16, decombinedMatrices.get(0).size());
    assertEquals("cols wrong", 24, decombinedMatrices.get(0).get(0).length);

    // Test by converting back to 1536!
    
    List<List<String[]>> combinedMatrices = 
        PlateReaderRawDataParser.convertMatrixFormat(
            384, 1536, matrixOrder1536, decombinedMatrices);

    assertEquals("wrong number of result matrices", 7, combinedMatrices.size());
    assertEquals("rows wrong", 32, combinedMatrices.get(0).size());
    assertEquals("cols wrong", 48, combinedMatrices.get(0).get(0).length);  
    //assertEquals(sourceMatrices,decombinedMatrices);
    for(int i=0;i<7;i++) {
      for(int j=0;j<32;j++) {
        for(int k=0;k<48;k++) {
          assertEquals("M: " + i + " C"+k+"R"+j, 
              sourceMatrices.get(i).get(j)[k], combinedMatrices.get(i).get(j)[k]);
        }
      }
    }
    

  }
  
	public void testConvertMatrix()
	{
		// first, convert 96 to 386
		
		// create the source data
		List<List<String[]>> sourceMatrices = Lists.newArrayList();
		for(int i=0; i<8; i++) {
			List<String[]> sourceMatrix = Lists.newArrayList();
			sourceMatrices.add(sourceMatrix);
			for(int j=0;j<8;j++) {
				String[] row = new String[12];
				sourceMatrix.add(row);
				for(int k=0;k<12;k++) {
					row[k] = "Q"+i+"C"+k+"R"+j;
				}
			}
		}
		
		List<List<String[]>> combinedMatrices = 
		    PlateReaderRawDataParser.convertMatrixFormat(96,384,null, sourceMatrices);
		
		assertEquals("wrong number of result matrices", 2, combinedMatrices.size());
		assertEquals("rows wrong", 16, combinedMatrices.get(0).size());
		assertEquals("cols wrong", 24, combinedMatrices.get(0).get(0).length);
		
		int factor = 4;
    List<String[]> combinedMatrix = combinedMatrices.get(0);
    for(int i=0;i<16;i++) {
      for(int j=0;j<24;j++) {
        int sourceMatrixNumber = 
            PlateReaderRawDataParser.deconvoluteMatrix(384, 96, i, j);
        int sourceRow = PlateReaderRawDataParser.deconvoluteRow(384, 96, i, j);
        int sourceCol = PlateReaderRawDataParser.deconvoluteCol(384, 96, i, j);
        assertEquals(
            "i:"+ i+"j:" + j,
            "Q"+sourceMatrixNumber+"C"+sourceCol+"R"+sourceRow, 
            combinedMatrix.get(i)[j]);
      }
    }
    
    combinedMatrix = combinedMatrices.get(1);
    int cumulativeMatrixCount = 4;
    for(int i=0;i<16;i++) {
      for(int j=0;j<24;j++) {
        int sourceMatrixNumber = cumulativeMatrixCount + 
            PlateReaderRawDataParser.deconvoluteMatrix(384, 96, i, j);
        int sourceRow = PlateReaderRawDataParser.deconvoluteRow(384, 96, i, j);
        int sourceCol = PlateReaderRawDataParser.deconvoluteCol(384, 96, i, j);
        assertEquals(
            "i:"+ i+"j:" + j,
            "Q"+sourceMatrixNumber+"C"+sourceCol+"R"+sourceRow, 
            combinedMatrix.get(i)[j]);
      }
    }
    
		// now 1536 to 384!
		
		sourceMatrices = Lists.newArrayList();
		for(int i=0;i<2;i++) {
			List<String[]> sourceMatrix = Lists.newArrayList();
			sourceMatrices.add(sourceMatrix);
			for(int j=0;j<32;j++) {
				String[] row = new String[48];
				sourceMatrix.add(row);
				for(int k=0;k<48;k++) {
					row[k] = "C"+k+"R"+j;
				}
			}
		}
		
		combinedMatrices = PlateReaderRawDataParser.convertMatrixFormat(
		    1536,384,null, sourceMatrices);
		
		assertEquals("wrong number of result matrices", 8, combinedMatrices.size());
		assertEquals("rows wrong", 16, combinedMatrices.get(0).size());
		assertEquals("cols wrong", 24, combinedMatrices.get(0).get(0).length);
			
		// Test by converting back to 1536!
		
		List<List<String[]>> decombinedMatrices = 
		    PlateReaderRawDataParser.convertMatrixFormat(
		        384, 1536, null, combinedMatrices);

		assertEquals("wrong number of result matrices", 2, decombinedMatrices.size());
		assertEquals("rows wrong", 32, decombinedMatrices.get(0).size());
		assertEquals("cols wrong", 48, decombinedMatrices.get(0).get(0).length);	
		//assertEquals(sourceMatrices,decombinedMatrices);
		// note: only test first, test both if not matrixorder==null, see 
		// testConvertMatrix1536 for example
		for(int i=0;i<1;i++) {
			for(int j=0;j<32;j++) {
				for(int k=0;k<48;k++) {
					assertEquals("M: " + i + " C"+k+"R"+j, 
					    sourceMatrices.get(i).get(j)[k], decombinedMatrices.get(i).get(j)[k]);
				}
			}
		}
	}
  
  public void testOdometerCount(){
    // reverse of testOdometer: get the count from a reading
    List<Integer> plates = ImmutableList.of(new Integer[] {1,4,6,12});
    List<Integer> quadrants = ImmutableList.of(new Integer[] {1,2,3,4});
    List<String> conditions = 
        ImmutableList.of(new String[] {"condition1", "condition2"});
    List<Integer> replicates = ImmutableList.of(new Integer[] {1,2,3});
    
    PlateReaderRawDataParser.Odometer odometer = new Odometer(
        replicates,quadrants, conditions,plates);
    
    List<?> reading = ImmutableList.of(1,1,"condition1", 1);
    assertEquals(0,odometer.getCount(reading));
    
    for(int i=0; i<odometer.getSize(); i++){
      reading = odometer.getReading(i);
      assertEquals(i, odometer.getCount(reading));
    }
  }
  
  /**
   * Test that, for a set of matrices having a collation order with quadrants 
   * not being last, that we can iterate through the matrices such that we pick
   * from each quadrant in succession.
   */
  public void testNextByQuadrant(){
    
    Integer[] plates = new Integer[] {1,4,6,12};
    Integer[] quadrants = new Integer[] {1,2,3,4};
    String[] conditions = new String[] {"condition1", "condition2"};
    String[] replicates = new String[] {"1","2","3"};
    String[] readouts = new String[] {"1","2","3"};
    CollationOrder ordering = CollationOrder.getOrder("PQCR");
    
    PlateReaderRawDataParser.MatrixOrder matrixOrder = 
        new PlateReaderRawDataParser.MatrixOrder(
            ordering, plates,conditions,readouts, replicates,quadrants);
    List<?> reading1;
    for(int i=0; i<matrixOrder.getSize(); i++){
      reading1 = matrixOrder.getReading(i);
      logger.info("count: " + i + ", reading: " + reading1);
      assertEquals(i, matrixOrder.getCount(reading1));
    }
        
    int sizeWithoutQuadrantsPlates = conditions.length*replicates.length;
    
    int quadrantStep = matrixOrder.getQuadrantStep();
    assertEquals(quadrantStep, conditions.length*replicates.length*readouts.length);
    List<?> reading, reading2;
    // test for plate=0, todo: test for plates 4,6,12, by jumping to those plates
    for(int i=0; i<sizeWithoutQuadrantsPlates; i++){
      reading = matrixOrder.getReading(i);
      for(int j=1; j<=quadrants.length; j++){
        int newCount = matrixOrder.getNextByQuadrant(i, j);
        int newQuadrant = matrixOrder.getQuadrant(newCount);
        logger.info("count: " + i + ", ex: " + (i+quadrantStep*(j-1)) +
                    ", newcount: " + newCount + 
                    ", reading: " + matrixOrder.getReading(newCount));
        assertEquals( 
            "reading: "+ matrixOrder.getReading(newCount),
            j,newQuadrant );
        assertEquals(i+quadrantStep*(j-1), newCount);
      }
    }
  }

	public void testOdometer()
	{
		List<Integer> plates = ImmutableList.of(new Integer[] {1,4,6,12});
		List<String> conditions = 
		    ImmutableList.of(new String[] {"condition1", "condition2"});
		List<Integer> replicates = ImmutableList.of(new Integer[] {1,2,3});
		
		PlateReaderRawDataParser.Odometer odometer = 
		    new Odometer(replicates,conditions,plates);
		
		List<?> reading = odometer.getReading(0);
		assertEquals(ImmutableList.of(1,"condition1", 1),reading);
		
		reading = odometer.getReading(1);
		assertEquals(ImmutableList.of(2,"condition1", 1),reading);
		
		reading = odometer.getReading(2);
		assertEquals(ImmutableList.of(3,"condition1", 1),reading);
		
		reading = odometer.getReading(3);
		assertEquals(ImmutableList.of(1,"condition2", 1),reading);
		
		reading = odometer.getReading(4);
		assertEquals(ImmutableList.of(2,"condition2", 1),reading);
		
		reading = odometer.getReading(5);
		assertEquals(ImmutableList.of(3,"condition2", 1),reading);
		
		reading = odometer.getReading(6);
		assertEquals(ImmutableList.of(1,"condition1", 4),reading);
		
		reading = odometer.getReading(7);
		assertEquals(ImmutableList.of(2,"condition1", 4),reading);
		
		reading = odometer.getReading(8);
		assertEquals(ImmutableList.of(3,"condition1", 4),reading);
		
		reading = odometer.getReading(9);
		assertEquals(ImmutableList.of(1,"condition2", 4),reading);
		
		reading = odometer.getReading(19);
		assertEquals(ImmutableList.of(2,"condition1", 12),reading);
		
		reading = odometer.getReading(20);
		assertEquals(ImmutableList.of(3,"condition1", 12),reading);
		
		reading = odometer.getReading(21);
		assertEquals(ImmutableList.of(1,"condition2", 12),reading);
		
		reading = odometer.getReading(22);
		assertEquals(ImmutableList.of(2,"condition2", 12),reading);
		
		reading = odometer.getReading(23);
		assertEquals(ImmutableList.of(3,"condition2", 12),reading);

		odometer = new Odometer(plates,replicates,conditions);
		
    reading = odometer.getReading(0);
    assertEquals(ImmutableList.of(1,1,"condition1"),reading);
    
    reading = odometer.getReading(1);
    assertEquals(ImmutableList.of(4,1,"condition1"),reading);
    
    reading = odometer.getReading(2);
    assertEquals(ImmutableList.of(6,1,"condition1"),reading);
    
    reading = odometer.getReading(3);
    assertEquals(ImmutableList.of(12,1,"condition1"),reading);
    
    reading = odometer.getReading(4);
    assertEquals(ImmutableList.of(1,2,"condition1"),reading);

	}
	
	public void testMatrixCollate(){
	  
    String[] readouts = {"readout1", "readout2"};
    String[] replicates = {"1","2","3"};    
    String[] conditions = {"condition1", "condition2"};
    Integer[] plates = {1,4,6,12};
    
    CollationOrder ordering = new CollationOrder(
        ImmutableList.of(PlateOrderingGroup.Plates, 
                         PlateOrderingGroup.Quadrants, 
                         PlateOrderingGroup.Conditions, 
                         PlateOrderingGroup.Replicates, 
                         PlateOrderingGroup.Readouts));
    
    PlateReaderRawDataParser.MatrixOrder matrixOrder = 
        new PlateReaderRawDataParser.MatrixOrder(
            ordering, plates, conditions, readouts, replicates);
	}
	
	public void testMatrixOrder(){

    String[] readouts = {"readout1", "readout2"};
    String[] replicates = {"1","2","3"};    
    String[] conditions = {"condition1", "condition2"};
	  Integer[] plates = {1,4,6,12};
    
    CollationOrder ordering = new CollationOrder(
        ImmutableList.of(PlateOrderingGroup.Plates, 
                         PlateOrderingGroup.Quadrants, 
                         PlateOrderingGroup.Conditions, 
                         PlateOrderingGroup.Replicates, 
                         PlateOrderingGroup.Readouts));
	  
    PlateReaderRawDataParser.MatrixOrder matrixOrder = 
        new PlateReaderRawDataParser.MatrixOrder(
            ordering, plates, conditions, readouts, replicates);
    
    assertEquals(matrixOrder.getReadout(0), "readout1");
    assertEquals(matrixOrder.getReadout(1), "readout2");
    assertEquals(matrixOrder.getReadout(2), "readout1");
    assertEquals(matrixOrder.getReadout(3), "readout2");
    assertEquals(matrixOrder.getReadout(6), "readout1");
    assertEquals(matrixOrder.getReadout(12), "readout1");
    assertEquals(matrixOrder.getReadout(18), "readout1");
    assertEquals(matrixOrder.getReadout(19), "readout2");
    assertEquals(matrixOrder.getReadout(32), "readout1");
    assertEquals(matrixOrder.getReadout(33), "readout2");
     
    assertEquals(matrixOrder.getReplicate(0), "1");
    assertEquals(matrixOrder.getReplicate(1), "1");
    assertEquals(matrixOrder.getReplicate(2), "2");
    assertEquals(matrixOrder.getReplicate(3), "2");
    assertEquals(matrixOrder.getReplicate(4), "3");
    assertEquals(matrixOrder.getReplicate(5), "3");
    assertEquals(matrixOrder.getReplicate(6), "1");
    assertEquals(matrixOrder.getReplicate(12), "1");
    assertEquals(matrixOrder.getReplicate(18), "1");
    assertEquals(matrixOrder.getReplicate(24), "1");
    assertEquals(matrixOrder.getReplicate(30), "1");
    assertEquals(matrixOrder.getReplicate(31), "1");
    assertEquals(matrixOrder.getReplicate(32), "2");
    assertEquals(matrixOrder.getReplicate(34), "3");
    assertEquals(matrixOrder.getReplicate(35), "3");
    assertEquals(matrixOrder.getReplicate(36), "1");
    
    assertEquals(matrixOrder.getCondtion(0), "condition1");
    assertEquals(matrixOrder.getCondtion(1), "condition1");
    assertEquals(matrixOrder.getCondtion(2), "condition1");
    assertEquals(matrixOrder.getCondtion(3), "condition1");
    assertEquals(matrixOrder.getCondtion(4), "condition1");
    assertEquals(matrixOrder.getCondtion(5), "condition1");
    assertEquals(matrixOrder.getCondtion(6), "condition2");
    assertEquals(matrixOrder.getCondtion(12), "condition1");
    assertEquals(matrixOrder.getCondtion(18), "condition2");
    assertEquals(matrixOrder.getCondtion(24), "condition1");
    assertEquals(matrixOrder.getCondtion(30), "condition2");
    assertEquals(matrixOrder.getCondtion(35), "condition2");
    assertEquals(matrixOrder.getCondtion(36), "condition1");
    
    
    assertEquals(matrixOrder.getPlate(0), new Integer(1));
    assertEquals(matrixOrder.getPlate(1), new Integer(1));
    assertEquals(matrixOrder.getPlate(2), new Integer(1));
    assertEquals(matrixOrder.getPlate(3), new Integer(1));
    assertEquals(matrixOrder.getPlate(4), new Integer(1));
    assertEquals(matrixOrder.getPlate(8), new Integer(1));
    assertEquals(matrixOrder.getPlate(12), new Integer(4));
    assertEquals(matrixOrder.getPlate(23), new Integer(4));
    assertEquals(matrixOrder.getPlate(24), new Integer(6));
    assertEquals(matrixOrder.getPlate(35), new Integer(6));
    assertEquals(matrixOrder.getPlate(36), new Integer(12));
    assertEquals(matrixOrder.getPlate(47), new Integer(12));
	}
	
	public void testMatrixOrder1536(){
    String[] readouts = {"readout1", "readout2"};
    String[] replicates = {"1","2","3"};    
    String[] conditions = {"condition1", "condition2"};
    Integer[] plates = new Integer[]{1,2,3,4,5,6,7,8};
    
    CollationOrder ordering = new CollationOrder(
        ImmutableList.of(
            PlateOrderingGroup.Plates,
            PlateOrderingGroup.Quadrants,
                         PlateOrderingGroup.Conditions, 
                         PlateOrderingGroup.Replicates, 
                         PlateOrderingGroup.Readouts));
    
    PlateReaderRawDataParser.MatrixOrderPattern matrixOrder = 
        new PlateReaderRawDataParser.MatrixOrder1536(
            ordering, plates, conditions, readouts, replicates);

    
    
    
    assertEquals(matrixOrder.getPlate(0), new Integer(1));
    assertEquals(matrixOrder.getPlate(1), new Integer(2));
    assertEquals(matrixOrder.getPlate(2), new Integer(3));
    assertEquals(matrixOrder.getPlate(3), new Integer(4));
    assertEquals(matrixOrder.getPlate(4), new Integer(1));
    assertEquals(matrixOrder.getPlate(5), new Integer(2));
    assertEquals(matrixOrder.getPlate(6), new Integer(3));
    assertEquals(matrixOrder.getPlate(7), new Integer(4));
	  
	}
}
