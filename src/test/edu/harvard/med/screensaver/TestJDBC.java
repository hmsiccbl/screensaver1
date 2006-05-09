// TestJDBC.java
// by john sullivan 2006.05

package edu.harvard.med.screensaver;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A basic standalone test for postgres connectivity.
 * @author s
 */
public class TestJDBC {

	/**
	 * @param args Not used.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost/screensaver";
		Properties props = new Properties();
		props.setProperty("user", "screensaver");
		props.setProperty("password", "screensaver");
		DriverManager.getConnection(url, props);
	}
}
