// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;


/**
 * @author s
 */
public class MockNCBIGeneInfoProvider implements NCBIGeneInfoProvider
{
  public NCBIGeneInfo getGeneInfoForEntrezgeneId(Integer entrezgeneId) throws EutilsException
  {
    // special case for row 9 in spreadsheet for RNAiLibraryContentsParserTest.testDataRowErrors
    if (entrezgeneId == 99999999) {
      throw new EutilsException("Error querying NCBI for EntrezGene ID 99999999: no such EntrezGene ID");
    }

    // special case for row 2 in spreadsheet for RNAiLibraryContentsParserTest.testCleanData
    if (entrezgeneId == 22848) {
      return new NCBIGeneInfo(
        "AP2 associated kinase 1",
        "Homo sapiens",
        "AAK1");
    }
    
    // special case for row 4 in spreadsheet for RNAiLibraryContentsParserTest.testCleanData
    if (entrezgeneId == 64781) {
      return new NCBIGeneInfo(
        "ceramide kinase",
        "Homo sapiens",
        "CERK");
    }
    
    return new NCBIGeneInfo(
      "guanine nucleotide binding protein (G protein), beta 4",
      "Mus musculus",
      "Gnb4");
  }
}
