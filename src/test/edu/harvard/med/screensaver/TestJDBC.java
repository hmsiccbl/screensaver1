// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A basic standalone test for postgres connectivity.
 * @author john sullivan
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
