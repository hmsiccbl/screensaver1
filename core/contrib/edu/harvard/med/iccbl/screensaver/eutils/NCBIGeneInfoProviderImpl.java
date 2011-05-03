// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.eutils;

import org.apache.log4j.Logger;

public class NCBIGeneInfoProviderImpl
{
  // static members

  private static Logger log = Logger.getLogger(NCBIGeneInfoProviderImpl.class);

  public static void main(String[] argv) throws Exception
  {
//
//    EUtilsServiceStub eUtils = new EUtilsServiceStub();
//    PUGStub pug = new PUGStub();
//
//    // Use eSearch to get compounds that come from KEGG,
//    // and that are tested in BioAssay AID 1200
//    ESearchRequest eSearch = new ESearchRequest();
//    String db = "pccompound";
//    eSearch.setDb(db);
//    eSearch.setTerm("KEGG[SourceName] AND 1200[BioAssayID]");
//    // create a history item, and don't return any actual ids in the
//    // SOAP response
//    eSearch.setUsehistory("y");
//    eSearch.setRetMax("0");
//    // do the search, verify that reasonable results are returned
//    ESearchResult eSearchResult = eUtils.run_eSearch(eSearch);
//    if (Integer.parseInt(eSearchResult.getCount()) <= 0)
//      throw new Exception("eSearch returned no hits");
//    if (eSearchResult.getWebEnv() == null ||
//        eSearchResult.getQueryKey() == null ||
//        Integer.parseInt(eSearchResult.getQueryKey()) <= 0)
//      throw new Exception("eSearch did not return WebEnv + query_key");
//    System.out.println("eSearch returned " + eSearchResult.getCount() + " hits");
//
//    // create ListKey from eSearch result
//    EntrezKey entrezKey = new EntrezKey();
//    entrezKey.setDb(db);
//    entrezKey.setWebenv(eSearchResult.getWebEnv());
//    entrezKey.setKey(eSearchResult.getQueryKey());
//    InputEntrez entrez = new InputEntrez();
//    entrez.setEntrezKey(entrezKey);
//    String entrezListKey = pug.InputEntrez(entrez)
//                              .getListKey();
//    System.out.println("Entrez ListKey = " + entrezListKey);
//
//    // Input AID 1200, with TIDs 1, 2, 8, and 14, and rows
//    // restricted to the compounds from eSearch
//    int[] tids = { 1, 2, 8, 14 };
//    ArrayOfInt arr = new ArrayOfInt();
//    arr.set_int(tids);
//    InputList list = new InputList();
//    list.setIdType(PCIDType.eID_TID);
//    list.setIds(arr);
//    String tidListKey = pug.InputList(list)
//                           .getListKey();
//    System.out.println("TID ListKey = " + tidListKey);
//    InputAssay assay = new InputAssay();
//    assay.setAID(1200);
//    assay.setColumns(AssayColumnsType.eAssayColumns_TIDs);
//    assay.setListKeyTIDs(tidListKey);
//    assay.setListKeySCIDs(entrezListKey);
//    String assayKey = pug.InputAssay(assay)
//                         .getAssayKey();
//    System.out.println("AssayKey = " + assayKey);
//
//    // Initialize the download; request CSV with no compression
//    AssayDownload download = new AssayDownload();
//    download.setAssayKey(assayKey);
//    download.setAssayFormat(AssayFormatType.eAssayFormat_CSV);
//    download.setECompress(CompressType.eCompress_None);
//    String downloadKey = pug.AssayDownload(download)
//                            .getDownloadKey();
//    System.out.println("DownloadKey = " + downloadKey);
//
//    // Wait for the download to be prepared
//    AnyKeyType anyKey = new AnyKeyType();
//    anyKey.setAnyKey(downloadKey);
//    GetOperationStatus getStatus = new GetOperationStatus();
//    getStatus.setGetOperationStatus(anyKey);
//    StatusType status;
//    while ((status = pug.GetOperationStatus(getStatus)
//                        .getStatus()) == StatusType.eStatus_Running ||
//           status == StatusType.eStatus_Queued) {
//      System.out.println("Waiting for download to finish...");
//      Thread.sleep(10000);
//    }
//
//    // On success, get the download URL, save to local file
//    if (status == StatusType.eStatus_Success) {
//      GetDownloadUrl getURL = new GetDownloadUrl();
//      getURL.setDownloadKey(downloadKey);
//      URL url = new URL(pug.GetDownloadUrl(getURL)
//                           .getUrl());
//      System.out.println("Success! Download URL = " + url.toString());
//
//      // get input stream from URL
//      URLConnection fetch = url.openConnection();
//      InputStream input = fetch.getInputStream();
//
//      // open local file based on the URL file name
//      String filename = "E:/Users/Thiessen/Downloads" +
//                        url.getFile()
//                           .substring(url.getFile()
//                                         .lastIndexOf('/'));
//      FileOutputStream output = new FileOutputStream(filename);
//      System.out.println("Writing data to " + filename);
//
//      // buffered read/write
//      byte[] buffer = new byte[10000];
//      int n;
//      while ((n = input.read(buffer)) > 0)
//        output.write(buffer, 0, n);
//    }
//    else {
//      GetStatusMessage message = new GetStatusMessage();
//      message.setGetStatusMessage(anyKey);
//      System.out.println("Error: " + pug.GetStatusMessage(message)
//                                        .getMessage());
//    }
  }

}
