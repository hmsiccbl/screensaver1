package edu.harvard.med.iccbl.platereader.parser;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import com.google.common.collect.Iterables;
import junit.framework.TestCase;
import org.junit.Test;

import edu.harvard.med.iccbl.platereader.PlateMatrix;

public class RawPlateReaderDataInterfaceTest extends TestCase
{
  /** Tests Java-Scala interop */
  // note: being run as JUnit3 test, not JUnit4, due to classpath issues, but keeping in JUnit4 annotation
  @Test
  public void testParseFile()
  {
    InputStream in = getClass().getResourceAsStream("/sample-plate-data.tab");
    assertNotNull("test data file not found", in);
    List<PlateMatrix> result = new TabDelimitedPlateReaderRawDataParser(16, 24).parse(new java.io.InputStreamReader(in));
    assertEquals(650, result.size());
    assertEquals(new BigDecimal(314936), result.get(0).well(0, 0).bigDecimal());
    assertEquals(new BigDecimal(184323), Iterables.getLast(result).well(15, 23).bigDecimal());
    for (PlateMatrix p : result) {
      assertEquals(16, p.height());
      assertEquals(24, p.width());
    }
  }
}
