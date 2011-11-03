package edu.harvard.med.screensaver.ui.arch.auth.servlet;
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

import edu.harvard.med.screensaver.policy.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;

public class DataServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(DataServlet.class);

  MimetypesFileTypeMap mimeTypesFileTypeMap = new MimetypesFileTypeMap();
	
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
    String outputRoot = ((AbstractBackingBean) request.getSession().getAttribute("scopedTarget.cellHTS2Runner")).getApplicationProperties().getProperty("cellHTS2.report.directory");
    String relativePath = pathInfo.substring(pathInfo.indexOf("/") + 1);
    Integer userId = ((CurrentScreensaverUser) request.getSession().getAttribute("scopedTarget.currentScreensaverUser")).getScreensaverUser().getEntityId();
    String fileLocation = outputRoot + "/" + userId + "/" + relativePath;
    File f = new File(fileLocation);
    log.debug("serving file " + fileLocation);

    //System.out.println;
    result = new byte[(int) f.length()];
    FileInputStream in = new FileInputStream(fileLocation);

      // Read file into byte array
    in.read(result);
    
    byte[] file = result;

		out.write(file);
  }

}
