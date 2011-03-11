// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.orm.jpa.JpaSystemException;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.arch.view.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

/**
 */
public class LibraryCopyDetail extends EditableEntityViewerBackingBean<Copy>
{
  private static Logger log = Logger.getLogger(LibraryCopyDetail.class);

  private LibraryCopyViewer _libraryCopyViewer;
  private LibraryViewer _libraryViewer;

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopyDetail()
  {
  }

  public LibraryCopyDetail(LibraryCopyDetail libraryCopyDetailProxy,
                           GenericEntityDAO dao,
                           LibraryCopyViewer libraryCopyViewer,
                           LibraryViewer libraryViewer)
  {
    super(libraryCopyDetailProxy,
          Copy.class,
          EDIT_LIBRARY_COPY,
          dao);
    _libraryCopyViewer = libraryCopyViewer;
    _libraryViewer = libraryViewer;
  }

  @Override
  protected void initializeViewer(Copy entity)
  {
  }

  @Override
  protected void initializeEntity(Copy copy)
  {
    getDao().needReadOnly(copy, Copy.library);
  }
  
  @UICommand
  @Override
  public String delete()
  {
    String copyName = getEntity().getName();
    
    try {
      Library library = getEntity().getLibrary();
      getDao().deleteEntity(getEntity());
      showMessage("deletedEntity", "copy " + copyName);
      _libraryViewer.getContextualSearchResults().reload();
      return _libraryViewer.viewEntity(library);
    }
    catch (JpaSystemException e) {
      if (e.contains(ConstraintViolationException.class)) {
        showMessage("cannotDeleteEntityInUse", "Copy " + copyName);
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      else {
        throw e;
      }
    }
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
      case CANCEL_EDIT:
      case SAVE_EDIT:
        return _libraryCopyViewer.reload();
      case CANCEL_NEW:
      case SAVE_NEW:
        return _libraryViewer.viewEntity(getDao().reloadEntity(getEntity(), true, Copy.library).getLibrary());
      default:
        return null;
    }
  }

  public List<SelectItem> getCopyUsageTypeSelectItems()
  {
    List<CopyUsageType> values = Arrays.asList(CopyUsageType.values());
    if (getEntity().getUsageType() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(values, REQUIRED_VOCAB_FIELD_PROMPT);
    }
    return JSFUtils.createUISelectItems(values);
  }
}
