// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFilesEntity;
import edu.harvard.med.screensaver.model.screens.AttachedFileType;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AttachedFiles extends AbstractBackingBean
{
  private GenericEntityDAO _dao;
  private Set<AttachedFileType> _attachedFileTypes;

  private AttachedFilesEntity _entity;
  private String _newAttachedFileName;
  private UISelectOneBean<AttachedFileType> _newAttachedFileType;
  private UploadedFile _uploadedAttachedFileContents;
  private String _newAttachedFileContents;
  private Predicate<AttachedFile> _attachedFileFilter = Predicates.alwaysTrue();
  
  AttachedFiles() {}
  
  public AttachedFiles(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public void setAttachedFilesEntity(AttachedFilesEntity entity)
  {
    _entity = entity;
  }

  public void setAttachedFileTypes(Set<AttachedFileType> attachedFileTypes)
  {
    _attachedFileTypes = attachedFileTypes; 
  }
  
  public void setAttachedFilesFilter(Predicate<AttachedFile> predicate)
  {
    _attachedFileFilter = predicate;
  }

  public void reset()
  {
    _newAttachedFileName = null;
    _newAttachedFileContents = null;
    _newAttachedFileType = null;
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
      _newAttachedFileType = new UISelectOneBean<AttachedFileType>(_attachedFileTypes, null, true) {
        @Override
        protected String getEmptyLabel() { return "<select>"; }
      };
    }
    return _newAttachedFileType;
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
    SortedSet<AttachedFile> attachedFiles = Sets.newTreeSet(new Comparator<AttachedFile>() {
      public int compare(AttachedFile af1, AttachedFile af2)
      {
        return af1.getFilename().compareTo(af2.getFilename());
      }
    });
    
    attachedFiles.addAll(Sets.filter(_entity.getAttachedFiles(), _attachedFileFilter));
    return new ListDataModel(Lists.newArrayList(attachedFiles));
  }


  @UIControllerMethod
  public String addAttachedFile()
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
        return REDISPLAY_PAGE_ACTION_RESULT;
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
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      if (_newAttachedFileType.getSelection() == null) {
        showMessage("requiredValue", "Attached File Type");
        return REDISPLAY_PAGE_ACTION_RESULT;
      }

      _entity.createAttachedFile(filename,
                                 _newAttachedFileType.getSelection(),
                                 contentsInputStream);
    }
    catch (IOException e) {
      reportApplicationError("could not attach the file contents");
    }
    reset();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteAttachedFile()
  {
    AttachedFile attachedFile = (AttachedFile) getRequestMap().get("element");
    _entity.removeAttachedFile(attachedFile);
    reset();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  @UIControllerMethod
  @Transactional
  public String downloadAttachedFile() throws IOException, SQLException
  {
    AttachedFile attachedFile = (AttachedFile) getRequestMap().get("element");
    return doDownloadAttachedFile(attachedFile);
  }

  public String doDownloadAttachedFile(AttachedFile attachedFile)
  throws IOException, SQLException
  {
    if (attachedFile != null) {
      // attachedFile must be Hibernate-managed in order to access the contents (a LOB type)
      // if attachedFile is transient, we should not attempt to reattach, and contents will be accessible
      if (attachedFile.getAttachedFileId() != null) {
        attachedFile = _dao.reloadEntity(attachedFile, true);
      }
      if (attachedFile != null) {
        JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                           attachedFile.getFileContents().getBinaryStream(),
                                           attachedFile.getFilename(),
                                           null);
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
}
