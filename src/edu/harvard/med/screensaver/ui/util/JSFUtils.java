// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class JSFUtils
{
  private static Logger log = Logger.getLogger(JSFUtils.class);

  /**
   * Performs the necessary steps to return a server-side file to an HTTP
   * client. Must be called within a JSF-enabled servlet environment.
   *
   * @param facesContext the JSF FacesContext
   * @param file the File to send to the HTTP client
   * @param mimeType the MIME type of the file being sent
   * @throws IOException
   */
  public static void handleUserFileDownloadRequest(
    FacesContext facesContext,
    File file,
    String mimeType)
    throws IOException
  {
    InputStream in = new FileInputStream(file);
    handleUserDownloadRequest(facesContext,
                              in,
                              file.getName(),
                              mimeType);
  }

  /**
   * Performs the necessary steps to return server-side data, provided as an
   * InputStream, to an HTTP client. Must be called within a JSF-enabled servlet
   * environment.
   *
   * @param facesContext the JSF FacesContext
   * @param dataInputStream an InputStream containing the data to send to the HTTP client
   * @param contentLocation set the "content-location" HTTP header to this value, allowing the downloaded file to be named
   * @param mimeType the MIME type of the file being sent
   * @throws IOException
   */
  public static void handleUserDownloadRequest(
    FacesContext facesContext,
    InputStream dataInputStream,
    String contentLocation,
    String mimeType)
    throws IOException
  {
    HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
    if (mimeType == null && contentLocation != null) {
      mimeType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(contentLocation);
    }
    if (mimeType != null) {
      response.setContentType(mimeType);
    }

    // NOTE: the second line does the trick with the filename. leaving first line in for posterity
    response.setHeader("Content-Location", contentLocation);
    response.setHeader("Content-disposition", "attachment; filename=\"" + contentLocation + "\"");

    OutputStream out = response.getOutputStream();
    IOUtils.copy(dataInputStream, out);
    out.close();

    // skip Render-Response JSF lifecycle phase, since we're generating a
    // non-Faces response
    facesContext.responseComplete();
  }

  /**
   * Creates a UISelectItems object that can be assigned to the "value"
   * attribute of a UISelectItems JSF component.
   */
  public static List<SelectItem> createUISelectItems(Collection items)
  {
    return createUISelectItems(items, false, null);
  }

  /**
   * Creates a UISelectItems object that can be assigned to the "value"
   * attribute of a UISelectItems JSF component. Adds an additional
   * "empty selection" item, whose value is an empty string and label is as
   * specified (empty string if null).
   */
  public static List<SelectItem> createUISelectItemsWithEmptySelection(Collection items,
                                                                       String emptySelectionLabel)
  {
    return createUISelectItems(items, true, emptySelectionLabel);
  }
  
  private static List<SelectItem> createUISelectItems(Collection items,
                                                      boolean addEmptyItem,
                                                      String emptyItemLabel)
  {
    List<SelectItem> result = new ArrayList<SelectItem>();
    if (addEmptyItem) {
      result.add(new SelectItem("", emptyItemLabel == null ? "" : emptyItemLabel));
    }
    for (Object item : items) {
      result.add(new SelectItem(item,
                                item.toString()));
    }
    return result;
  }

  /**
   * Output the names of all registered JSF components to the logger, as debug output.
   *
   * @motivation allows developer to determine the valid set of JSF component
   *             names that can be passed to Application.createComponent();
   * @param component
   * @param level
   */
  private void logComponentNames()
  {
    log.debug("JSF registered component names:");
    Iterator iter = FacesContext.getCurrentInstance().getApplication().getComponentTypes();
    while (iter.hasNext()) {
      log.debug("component name: " + iter.next().toString());
    }
  }

  /**
   * Output the hierarchical structure of a JSF component with nested children.
   */
  private void logComponentStructure(UIComponent component, int level)
  {
    char[] indent = new char[level];
    Arrays.fill(indent, ' ');
    log.debug(new String(indent) + component.getClass() + ": " + component.getId());
    for (Iterator iter = component.getChildren().iterator(); iter.hasNext();) {
      UIComponent child = (UIComponent) iter.next();
      logComponentStructure(child, level + 1);
    }
  }


}
