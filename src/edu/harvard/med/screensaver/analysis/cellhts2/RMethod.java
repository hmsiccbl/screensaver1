// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

public enum RMethod
{
  START("start",-1),
  // TODO: maybe collapse the next 3 stages into a single PREPARE stage?
  READ_PLATELIST("readPlateList",0),
  CONFIGURE("configure",1) ,
  ANNOTATE("annotate",2),
  NORMALIZE_PLATES("normalize_plates",3),
  SCORE_REPLICATES("score_replicates",4),
  SUMMARIZE_REPLICATES("summarize_replicates",5),
  WRITE_REPORT("write_report",6)
  ;

  private String argumentValue;
  private Integer index;

  RMethod(String argumentValue, Integer index)
  {
    this.argumentValue = argumentValue;
    this.index = index;
  }

  public String getArgumentValue()
  {
    return this.argumentValue;
  }

  // TODO: maybe eliminate this method and use Stage.getOrdinal()?
  public Integer getIndex()
  {
    return this.index;
  }

}