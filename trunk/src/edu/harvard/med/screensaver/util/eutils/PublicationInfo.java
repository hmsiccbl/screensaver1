// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;


public class PublicationInfo
{
  private String _yearPublished;
  private String _authors;
  private String _title;
  
  public PublicationInfo(String yearPublished, String authors, String title)
  {
    _yearPublished = yearPublished;
    _authors = authors;
    _title = title;
  }

  public String getYearPublished()
  {
    return _yearPublished;
  }

  public String getAuthors()
  {
    return _authors;
  }

  public String getTitle()
  {
    return _title;
  }
}

