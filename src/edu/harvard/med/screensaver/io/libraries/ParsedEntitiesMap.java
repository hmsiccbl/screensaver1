// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.io.libraries.compound.SDFileCompoundLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.Triple;

/**
 * A map of the entities parsed so far. Intended for use with a single call to {@link
 * RNAiLibraryContentsParser#parseLibraryContents}, or {@link
 * SDFileCompoundLibraryContentsParser}. Tracks loaded entities of the following
 * types:
 * 
 * <ul>
 * <li>{@link Well}
 * <li>{@link SilencingReagent}
 * <li>{@link Gene}
 * </ul>
 * 
 * Entities are tracked by the fields of their business key, with one exception: because this
 * class is intended for use with loading a single library, the {@link Library} portion of the
 * business key for <code>Well</code> is excluded.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ParsedEntitiesMap
{
  
  private Map<Pair<Integer,String>,Well> _wells =
    new HashMap<Pair<Integer,String>,Well>(1000);
  private Map<Triple<Gene,SilencingReagentType,String>,SilencingReagent> _silencingRagents =
    new HashMap<Triple<Gene,SilencingReagentType,String>,SilencingReagent>(4000);
  private Map<Integer,Gene> _genes =
    new HashMap<Integer,Gene>(1000);
  
  public void addWell(Well well)
  {
    _wells.put(new Pair<Integer,String>(well.getPlateNumber(), well.getWellName()), well);
  }
  
  public Well getWell(Integer plateNumber, String wellName)
  {
    return _wells.get(new Pair<Integer,String>(plateNumber, wellName));
  }
  
  public void addSilencingReagent(SilencingReagent silencingReagent)
  {
    _silencingRagents.put(
      new Triple<Gene,SilencingReagentType,String>(
        silencingReagent.getGene(),
        silencingReagent.getSilencingReagentType(),
        silencingReagent.getSequence()),
      silencingReagent);
  }

  public SilencingReagent getSilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence)
  {
    return _silencingRagents.get(
      new Triple<Gene,SilencingReagentType,String>(gene, silencingReagentType, sequence));
  }
  
  public void addGene(Gene gene)
  {
    _genes.put(gene.getEntrezgeneId(), gene);
  }
  
  public Gene getGene(Integer entrezgeneId)
  {
    return _genes.get(entrezgeneId);
  }
}
