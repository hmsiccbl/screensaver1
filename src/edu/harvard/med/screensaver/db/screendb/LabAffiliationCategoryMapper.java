// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.users.AffiliationCategory;

public class LabAffiliationCategoryMapper
{
  private static Logger log = Logger.getLogger(LabAffiliationCategoryMapper.class);
  private static final String CATEGORIES_FILE =
    "/edu/harvard/med/screensaver/db/screendb/lab_affiliation_categories.txt";
  
  private Map<String,AffiliationCategory> _labAffiliationToCategory =
    new HashMap<String,AffiliationCategory>();
  private AffiliationCategory.UserType _affiliationCategoryUserType =
    new AffiliationCategory.UserType();

  public LabAffiliationCategoryMapper()
  {
    URL url = getClass().getResource(CATEGORIES_FILE);
    File categoriesFile = new File(url.getFile());
    if (! categoriesFile.exists()) {
      log.error("could not locate lab affiliation categories file resource: " + CATEGORIES_FILE);
      return;
    }

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(categoriesFile)));
      String line;
      while ((line = reader.readLine()) != null) {
        String [] fields = line.split("\t");
        String categoryName = fields[0];
        String labAffiliation = fields[1];
        AffiliationCategory affiliationCategory =
          _affiliationCategoryUserType.getTermForValue(categoryName);
        if (affiliationCategory == null) {
          log.error("could not find lab affiliation to match the text \"" + categoryName + "\"");
          continue;
        }
        _labAffiliationToCategory.put(labAffiliation, affiliationCategory);
      }
    }
    catch (FileNotFoundException e) {
      log.error("could not locate lab affiliation categories file resource: " + CATEGORIES_FILE);
    }
    catch (IOException e) {
      log.error("error reading lab affiliation categories file: " + CATEGORIES_FILE);
    }
  }
  
  public AffiliationCategory getAffiliationCategoryForLabAffiliation(String labAffiliation)
  {
    return _labAffiliationToCategory.get(labAffiliation);
  }
}

