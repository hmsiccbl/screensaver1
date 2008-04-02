// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import edu.harvard.med.screensaver.model.libraries.Gene;

/**
 * Retrieves basic information about a gene with a given EntrezGene ID.
 */
public interface NCBIGeneInfoProvider
{
  /**
   * Get the {@link NCBIGeneInfo information needed from NCBI} for a {@link Gene}, based on
   * the EntrezGene ID. If any errors occur, report the error and return null.
   * @param entrezgeneId the EntrezGene ID for the Gene.
   * @param cell the cell to specify when reporting an error
   * @return the NCBIGeneInfo
   */
  public abstract NCBIGeneInfo getGeneInfoForEntrezgeneId(Integer entrezgeneId) throws EutilsException;
}