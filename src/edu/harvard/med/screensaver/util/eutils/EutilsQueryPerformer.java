// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util.eutils;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

public abstract class EutilsQueryPerformer
{
  private static Logger log = Logger.getLogger(EutilsQueryPerformer.class);

  
  // protected instance methods
  
  /**
   * Recursively traverse the nodal structure of the node, accumulating the accumulate
   * parts of the text content of the node and all its children.
   * @param node the node to traversalate
   * @return the accumulative recursive text content of the traversalated node
   */
  protected String getTextContent(Node node)
  {
    if (node.getNodeType() == Node.TEXT_NODE) {
      return node.getNodeValue();
    }
    String textContent = "";
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      textContent += getTextContent(child);
    }
    return textContent;
  }
}

