package edu.harvard.med.screensaver.analysis.cellhts2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.accesspolicy.DataAccessPolicyInjectorPostLoadEventListener;

public class DataServlet extends HttpServlet {
	MimetypesFileTypeMap mimeTypesFileTypeMap = new MimetypesFileTypeMap();
  private static Logger log = Logger.getLogger(DataServlet.class);

	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void destroy() {
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, FileNotFoundException {
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, FileNotFoundException {
		processRequest(request, response);
	}

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, FileNotFoundException {

		ServletOutputStream out = response.getOutputStream();
		String pathInfo = request.getPathInfo(); 
		String mimeType = mimeTypesFileTypeMap.getContentType(pathInfo);
		response.setContentType(mimeType);
	
    byte[] result = null;
    String outputRoot = ScreensaverProperties.getProperty("cellHTS2report.filepath.base");
    String fileLocation = outputRoot + request.getServletPath() + pathInfo;
    File f = new File(fileLocation);
    log.debug("Trying to read from: " + fileLocation);

    //System.out.println;
    result = new byte[(int) f.length()];
    FileInputStream in = new FileInputStream(fileLocation);

      // Read file into byte array
    in.read(result);
    
    byte[] file = result;

    if (file == null) {
      log.debug("NULL FILE");
    } else {   
      log.debug("Returning File");
    }
    
		out.write(file);
  }

}
