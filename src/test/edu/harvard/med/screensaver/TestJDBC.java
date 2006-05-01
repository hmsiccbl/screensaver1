/**
 * TestJDBC.java
 * a basic standalone test for postgres connectivity
 */
package edu.harvard.med.screensaver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author s
 * 
 */
public class TestJDBC {

	/**
	 * @param args
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
		Connection conn = DriverManager.getConnection(url, props);
	}
}
