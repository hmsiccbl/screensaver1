// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/libraries/compound/CompoundPubchemCidListUpgrader.java $
// $Id: CompoundPubchemCidListUpgrader.java 2060 2007-12-17 01:44:31Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound.upgraders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * Standalone application for grabbing compound names out of a "BioactivesSubmission"
 * ChemBank 1.x XML file.
 * <p>
 * PLEASE NOTE: this is written as a one-off, throwaway utility. the code is extremely ugly
 * and will most likely stay that way until the job is done and the class is svn rm'd!
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class BioactivesSubmissionCompoundNameGrabber
{
  
  // static fields
  
  private static Logger log = Logger.getLogger(BioactivesSubmissionCompoundNameGrabber.class);
  private static int NUM_COMPOUNDS_UPGRADED_PER_TRANSACTION = 100;
  
  
  // static methods
  
  public static void main(String [] args) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException
  {
    if (args.length != 1) {
      throw new IllegalArgumentException(
        "BioactivesSubmissionCompoundNameGrabber has one-required arg: the name of the " +
        "BioactivesSubmission XML file to grab compound names from");
    }
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    GenericEntityDAO dao = (GenericEntityDAO) context.getBean("genericEntityDao");
    BioactivesSubmissionCompoundNameGrabber upgrader =
      new BioactivesSubmissionCompoundNameGrabber(dao, new File(args[0]));
    upgrader.grabCompoundNamesFromBioactivesSubmission();
  }
  
  
  // instance fields
  
  private GenericEntityDAO _dao;
  private File _xmlFile;
  private DocumentBuilder _documentBuilder;


  // constructor and instance methods
  
  public BioactivesSubmissionCompoundNameGrabber(GenericEntityDAO dao, File xmlFile) throws ParserConfigurationException
  {
    _dao = dao;
    _xmlFile = xmlFile;
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    _documentBuilder = documentBuilderFactory.newDocumentBuilder();
  }
  
  public void grabCompoundNamesFromBioactivesSubmission() throws FileNotFoundException, SAXException, IOException
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          grabCompoundNamesFromBioactivesSubmission0();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void grabCompoundNamesFromBioactivesSubmission0() throws FileNotFoundException, SAXException, IOException
  {
    Document document = _documentBuilder.parse(new FileInputStream(_xmlFile));
    NodeList identifiersElements = document.getElementsByTagName("Identifiers");
    for (int i = 0; i < identifiersElements.getLength(); i ++) {
      Element identifiersElement = (Element) identifiersElements.item(i);

      NodeList vendorElements = identifiersElement.getElementsByTagName("Vendor");
      if (vendorElements.getLength() != 1) {
        log.error("expected exactly one Vendor element in Identifiers block. got " + vendorElements.getLength());
        continue;
      }
      Element vendorElement = (Element) vendorElements.item(0);
      String vendorName = vendorElement.getAttribute("name");
      if (vendorName == null) {
        log.error("expected attribute 'name' on Vendor element");
        continue;
      }
      String vendorIdentifier = getTextContent(vendorElement);
      
      // HACKS
      if (vendorName.equals("Biomol")) {
        vendorName = "BIOMOL";
      }
      
      
      List<Well> wellsForVendorInfo = _dao.findEntitiesByHql(
        Well.class,
        "from Well where reagent.reagentId = ?",
        new ReagentVendorIdentifier(vendorName, vendorIdentifier));
      if (wellsForVendorInfo.size() == 0) {
        log.error("could not find well for vendor info: " + vendorName + ", " + vendorIdentifier);
        continue;
      }
      
      for (Well well : wellsForVendorInfo) {
        Compound compound = well.getPrimaryCompound();
        if (compound == null) {
          log.error("no primary compound for well " + well);
          continue;
        }

        NodeList nameElements = identifiersElement.getElementsByTagName("Name");
        for (int j = 0; j < nameElements.getLength(); j ++) {
          Element nameElement = (Element) nameElements.item(j);
          String name = getTextContent(nameElement);
//          log.info(
//            "add name \"" + name + "\" to compound " + compound + " based on vendor info " +
//            vendorName + ", " + vendorIdentifier);
        }
      }
    }
  }

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
