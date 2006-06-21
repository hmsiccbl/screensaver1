// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

/**
 * Maintains a list of error messages.
 * @author ant
 */
public class ParseErrorManager
{
  private static Logger log = Logger.getLogger(ParseErrorManager.class);
  
  private static Transformer errorToMessageTransformer = new Transformer() 
  { 
    public Object transform(Object parseError) 
    { 
      return ((ParseError) parseError).getMessage();
    };
  };
  private List<ParseError> _errors = new ArrayList<ParseError>();
  
  /**
   * Add a simple error.
   * 
   * @param error the error
   */
  public void addError(String errorMessage)
  {
    ParseError error = new ParseError(errorMessage);
    _errors.add(error);
    log.info("parse error: " + error);
  }
  
  /**
   * Add an error, noting the particular cell the error is related to.
   * 
   * @param error the error
   * @param dataHeader the data header of the cell containing the error
   * @param row the {@link Row} of the cell containing the error
   */
  public void addError(String errorMessage, CellReader cell)
  {
    ParseError error = new ParseError(errorMessage, cell);
    _errors.add(error);
    log.info("parse error: " + error);
  }
  
  /**
   * Get the list of error messages (as <code>String</code>s)
   * 
   * @return a list of <code>String</code> error messages
   */
  @SuppressWarnings("unchecked")
  public List<String> getErrorMessages()
  {
    return new ArrayList<String>(CollectionUtils.transformedCollection(_errors,
                                                                       errorToMessageTransformer));
  }
  
  /**
   * Get the list of <code>ParseError</code> objects.
   * 
   * @return a list of <code>ParseError</code> objects
   */
  public List<ParseError> getErrors()
  {
    return _errors;
  }

}
