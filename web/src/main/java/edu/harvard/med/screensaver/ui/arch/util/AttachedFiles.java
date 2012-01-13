// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/lincs/ui-cleanup/core/src/main/java/edu/harvard/med/screensaver/ui/arch/util/AttachedFiles.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.AttachedFilesEntity;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

public class AttachedFiles extends AbstractBackingBean
{
  private GenericEntityDAO _dao;
  private SortedSet<AttachedFileType> _attachedFileTypes;

  private AttachedFilesEntity _entity;
  private String _newAttachedFileName;
  private UISelectOneBean<AttachedFileType> _newAttachedFileType;
  private LocalDate _newAttachedFileDate;
  private UploadedFile _uploadedAttachedFileContents;
  private String _newAttachedFileContents;
  private Predicate<AttachedFile> _attachedFileFilter = Predicates.alwaysTrue();

  protected AttachedFiles()
  {}

  public AttachedFiles(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  protected GenericEntityDAO getDao()
  {
    return _dao;
  }

  protected AttachedFilesEntity getAttachedFilesEntity()
  {
    return _entity;
  }

  public void initialize(AttachedFilesEntity entity,
                         SortedSet<AttachedFileType> attachedFileTypes,
                         Predicate<AttachedFile> predicate)
  {
    _entity = entity;
    _attachedFileTypes = attachedFileTypes;
    _attachedFileFilter = predicate == null ? Predicates.<AttachedFile>alwaysTrue() : predicate;
    reset();
  }

  public void initialize()
  {
    initialize(null, null, null);
  }

  private void reset()
  {
    _newAttachedFileName = null;
    _newAttachedFileContents = null;
    _newAttachedFileType = null;
    _newAttachedFileDate = null;
    _uploadedAttachedFileContents = null;
  }

  public String getNewAttachedFileName()
  {
    return _newAttachedFileName;
  }

  public void setNewAttachedFileName(String newAttachedFileName)
  {
    _newAttachedFileName = newAttachedFileName;
  }

  public UISelectOneBean<AttachedFileType> getNewAttachedFileType()
  {
    if (_newAttachedFileType == null) {
      _newAttachedFileType = new UISelectOneBean<AttachedFileType>(_attachedFileTypes,
                                                                   _attachedFileTypes.size() == 1 ? _attachedFileTypes.first()
                                                                     : null,
                                                                     _attachedFileTypes.size() > 1) {
        @Override
        protected String getEmptyLabel()
        {
          return ScreensaverConstants.REQUIRED_VOCAB_FIELD_PROMPT;
        }
      };
    }
    return _newAttachedFileType;
  }

  public LocalDate getNewAttachedFileDate()
  {
    return _newAttachedFileDate;
  }

  public void setNewAttachedFileDate(LocalDate newAttachedFileDate)
  {
    _newAttachedFileDate = newAttachedFileDate;
  }

  public String getNewAttachedFileContents()
  {
    return _newAttachedFileContents;
  }

  public void setNewAttachedFileContents(String newAttachedFileContents)
  {
    _newAttachedFileContents = newAttachedFileContents;
  }

  public void setUploadedAttachedFileContents(UploadedFile uploadedFile)
  {
    _uploadedAttachedFileContents = uploadedFile;
  }

  public UploadedFile getUploadedAttachedFileContents()
  {
    return _uploadedAttachedFileContents;
  }

  public DataModel getAttachedFilesDataModel()
  {
    if (_entity == null) {
      return new ListDataModel();
    }
    List<AttachedFile> attachedFilesUnfiltered = Lists.newArrayList(Iterables.filter(_entity.getAttachedFiles(), _attachedFileFilter));
    List<AttachedFile> attachedFiles =
      Lists.newArrayList(Iterators.transform(attachedFilesUnfiltered.iterator(),
                                             new Function<AttachedFile,AttachedFile>() {

                                               @Override
                                               public AttachedFile apply(AttachedFile from)
                                              {
                                                return (AttachedFile) from.restrict();
                                              }
                                             }));
    Collections.sort(attachedFiles);
    return new ListDataModel(attachedFiles);
  }

  @UICommand
  public String addAttachedFile()
  {
    if (_entity == null) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    doAddAttachedFile();
    reset();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @SuppressWarnings("unchecked")
  protected void doAddAttachedFile()
  {
    String filename;
    InputStream contentsInputStream;
    if (_uploadedAttachedFileContents != null) {
      filename = _uploadedAttachedFileContents.getName();
      try {
        contentsInputStream = _uploadedAttachedFileContents.getInputStream();
      }
      catch (IOException e) {
        reportApplicationError(e.getMessage());
        return;
      }
      _uploadedAttachedFileContents = null;
    }
    else {
      filename = _newAttachedFileName;
      contentsInputStream = new ByteArrayInputStream(_newAttachedFileContents.getBytes());
    }

    try {
      if (filename == null || filename.trim().length() == 0) {
        showMessage("requiredValue", "Attached File Name");
        return;
      }
      if (_newAttachedFileType.getSelection() == null) {
        showMessage("requiredValue", "Attached File Type");
        return;
      }

      _entity.createAttachedFile(filename,
                                 _newAttachedFileType.getSelection(),
                                 _newAttachedFileDate,
                                 contentsInputStream);
    }
    catch (IOException e) {
      reportApplicationError("could not attach the file contents");
    }
  }

  @UICommand
  public String deleteAttachedFile()
  {
    AttachedFile attachedFile = (AttachedFile) getRequestMap().get("element");
    _entity.removeAttachedFile(attachedFile);
    reset();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  @Transactional
  public String downloadAttachedFile() throws IOException, SQLException
  {
    AttachedFile attachedFile = (AttachedFile) getRequestMap().get("element");
    return doDownloadAttachedFile(attachedFile, getFacesContext(), _dao);
  }

  public static String doDownloadAttachedFile(final AttachedFile attachedFileIn,
                                              final FacesContext facesContext,
                                              final GenericEntityDAO dao)
    throws IOException, SQLException
  {
    if (attachedFileIn != null) {
      dao.doInTransaction(new DAOTransaction() {

        @Override
        public void runTransaction()
        {
          // attachedFile must be Hibernate-managed in order to access the contents (a LOB type)
          // if attachedFile is transient, we should not attempt to reattach, and contents will be accessible
          AttachedFile attachedFile = attachedFileIn;
          if (attachedFileIn.getAttachedFileId() != null) {
            attachedFile = dao.reloadEntity(attachedFileIn, true);
          }
          if (attachedFile != null) {
            try {
              JSFUtils.handleUserDownloadRequest(facesContext,
                                                 new ByteArrayInputStream(attachedFile.getFileContents()),
                                                 attachedFile.getFilename(),
                                                 null);
            }
            catch (IOException e) {
              throw new DAOTransactionRollbackException(e);
            }
          }
        }
      });
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public boolean getIsEmpty()
  {
    return getAttachedFilesDataModel().getRowCount() == 0;
  }
}
