package db_info;
import java.sql.*;
import java.util.*;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;

import modem_comms.ModemActions;

import base.LightManager;
import base.The_Desktop;

public class DB_Connection {
	The_Desktop parent_desktop;
	static JDesktopPane local_desktop;
	ModemActions theModel;
	public DB_UserCredentials the_user_info;
	static String DB_BASE_URL = null;
	static String DB_HOSTNAME = null;
	static String DB_INSTANCE = null;
	static String DB_USR = null;
	static String DB_USR_PASSWD = null;
	static Connection conn=null;
	static int DB_LOG=0; //for logging when not connected to primary db

	public DB_Connection(The_Desktop desktop_class,ModemActions theModel,JDesktopPane desktop) {
		parent_desktop=desktop_class;
		local_desktop=desktop;
		this.theModel=theModel;
		parent_desktop.menuBar.setVisible(false);
		the_user_info=new DB_UserCredentials(this,theModel,desktop);
	}//end Connect_to_Database

	public Boolean tryConnect(){
		String DB_URL=null;

		ResourceBundle labels = ResourceBundle.getBundle("Connection_Props");
		DB_BASE_URL = labels.getString("DB_BASE_URL");
		DB_HOSTNAME = the_user_info.user_fields.the_hostname;
		DB_INSTANCE = labels.getString("DB_INSTANCE");
		DB_USR=the_user_info.user_fields.the_username;
		DB_USR_PASSWD=the_user_info.user_fields.the_password;
		try {
			//DB_URL=DB_BASE_URL+DB_HOSTNAME+"/"+DB_INSTANCE+"?cacheResultSetMetadata=false";
			//DB_URL=DB_BASE_URL+DB_HOSTNAME+"/"+DB_INSTANCE+"?autoReconnect=true";
			DB_URL=DB_BASE_URL+DB_HOSTNAME+"/"+DB_INSTANCE;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(DB_URL, DB_USR, DB_USR_PASSWD);
			conn.setAutoCommit(true);
			//if (DB_HOSTNAME.contains("localhost")) DB_LOG=1;//turn on logging when connected to the localhost
			//else DB_LOG=0;
			parent_desktop.connectedToDatabase(true);//set the title off the main frame as "connected"
			parent_desktop.menuBar.setVisible(true);
			try{
				parent_desktop.findTracks();//invoke the find interface as soon as the user is connected	
			}catch(Exception exc){
				JOptionPane.showMessageDialog(null, "Connected, but unable to render results.\n Please try again.", "Warning",
						JOptionPane.WARNING_MESSAGE,null);
				exc.printStackTrace();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Invalid Username or Password\n Please try again.", "Warning",
					JOptionPane.WARNING_MESSAGE,null);

			the_user_info=new DB_UserCredentials(this,theModel,local_desktop);//bring up the credentials again if we fail
			System.err.println("Cannot connect to database server;\n");
			e.printStackTrace();
			return (false);
		}
		if (LightManager.DEBUG) {
			try {
				System.out.println("Connector's autocommit status is "+DB_Connection.conn.getAutoCommit());
			}catch (SQLException e) {
				System.err.println("Problem getting status.");
			}
		}
		return (true);
	}

}//end class Database_Connection
