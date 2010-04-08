// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.IOException;
import java.io.InputStream;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.util.Pair;

public abstract class LibraryContentsParser<R extends Reagent>
{
  private GenericEntityDAO _dao;
  private InputStream _stream;
  private Library _library;

  /**
   * @param stream containing the Library in the proper format (sdf for Small Molecule, xls...)
   * @param dao persistence bean for writing to the db
   * @param library - library being loaded
   * @throws ParseErrorsException
   * @throws IOException 
   */
  public LibraryContentsParser(GenericEntityDAO dao, InputStream stream, Library library)
  {
    _dao = dao;
    _stream = stream;
    _library = library;
  }

  abstract public Pair<Well,R> parseNext() throws IOException, ParseException;
  
  protected GenericEntityDAO getDao()
  {
    return _dao;
  }

  protected InputStream getStream()
  {
    return _stream;
  }

  protected Library getLibrary()
  {
    return _library;
  }
}
