package db_info;

import java.sql.*;


import base.LightManager;

public class DB_Calls {


	private static void logPreparedStatement(PreparedStatement p_stmt){
		String whole=p_stmt.toString();
		int class_index=whole.indexOf("call ");
		System.out.println(whole.substring(class_index, whole.length()));
	}

	public static ResultSet getNumberOfTracks() {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			p_stmt=DB_Connection.conn.prepareStatement("call get_number_of_tracks();");
			rs=p_stmt.executeQuery();
		}catch (Exception exc) {
			System.out.println("Problem in Find_Work_Orders_Count.");
			System.out.println(exc.toString());
		}
		return rs;
	}		

	public static ResultSet findTracks(Integer page) {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			p_stmt=DB_Connection.conn.prepareStatement("call find_tracks(?);");
			p_stmt.setInt(1,page);
			rs=p_stmt.executeQuery();
		}catch (Exception exc) {
			System.out.println("Problem in Find_Tracks.");
			System.out.println(exc.toString());
		}
		return rs;
	}

	public static ResultSet deleteTrack(Integer track_id) {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			p_stmt=DB_Connection.conn.prepareStatement("call delete_track(?);");
			p_stmt.setInt(1,track_id);

			if (DB_Connection.DB_LOG==1)logPreparedStatement(p_stmt);
			rs=p_stmt.executeQuery();
		}catch (Exception exc) {
			System.out.println("Problem with delete_track.");
			exc.printStackTrace();
		}
		return rs;
	}

	public static Integer cloneTrack(Integer track_id) {
		PreparedStatement p_stmt;
		ResultSet rs=null;
		Integer new_track_id=0;

		try {
			p_stmt=DB_Connection.conn.prepareStatement("call clone_track(?);");
			p_stmt.setInt(1,track_id);

			if (DB_Connection.DB_LOG==1)logPreparedStatement(p_stmt);
			rs=p_stmt.executeQuery();
			if (rs.first()){
				new_track_id=(rs.getInt("new_track_id"));
			}
		}catch (Exception exc) {
			System.out.println("Problem with clone_track.");
			exc.printStackTrace();
		}
		return new_track_id;
	}	
	public static ResultSet saveTrackPic(Integer track_configuration_id,
			String description, byte[] track_photo_source) {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			if (LightManager.DEBUG) System.out.println("call save_track_pic("+track_configuration_id+
			","+description+
			", bunch_of_bytes);");
			p_stmt=DB_Connection.conn.prepareStatement("call save_track_pic(?,?,?);");
			p_stmt.setInt(1,track_configuration_id);
			p_stmt.setString(2,description);
			p_stmt.setBytes(3,track_photo_source);

			//if (DB_Connection.DB_LOG==1)logPreparedStatement(p_stmt); //Dont wan to print out the text of a JPEG
			rs=p_stmt.executeQuery();

		}catch (Exception exc) {
			System.out.println("Problem saving track info.");
			System.out.println(exc.toString());
		}
		return rs;
	}

	public static ResultSet updateTrackDescription(Integer track_configuration_id,
			String description) {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			if (LightManager.DEBUG) System.out.println("call update_track_description("+track_configuration_id+
					","+description);
			p_stmt=DB_Connection.conn.prepareStatement("call update_track_description(?,?);");
			p_stmt.setInt(1,track_configuration_id);
			p_stmt.setString(2,description);

			if (DB_Connection.DB_LOG==1)logPreparedStatement(p_stmt);
			rs=p_stmt.executeQuery();

		}catch (Exception exc) {
			System.out.println("Problem updating track description.");
			System.out.println(exc.toString());
		}
		return rs;
	}
	
	
	public static ResultSet clearNodesAtTrack(Integer track_configuration_id) {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			if (LightManager.DEBUG) System.out.println("call clear_nodes_at_track("+track_configuration_id+");");
			p_stmt=DB_Connection.conn.prepareStatement("call clear_nodes_at_track(?);");
			p_stmt.setInt(1,track_configuration_id);
	
			if (DB_Connection.DB_LOG==1)logPreparedStatement(p_stmt);
			rs=p_stmt.executeQuery();

		}catch (Exception exc) {
			System.out.println("Problem clearing existing nodes.");
			System.out.println(exc.toString());
		}
		return rs;
	}
	
	public static ResultSet saveLightNode(Integer track_configuration_id,
			String node_description, String slave_address, Double x, Double y, 
			String keyboard_shortcut) {
		PreparedStatement p_stmt;
		ResultSet rs=null;
		
		try {
			if (LightManager.DEBUG) System.out.println("call save_light_node("+track_configuration_id+","+
																				   node_description+", "+
																				   slave_address+", "+
																				   x+", "+y+", "+
																				   keyboard_shortcut+");");
			p_stmt=DB_Connection.conn.prepareStatement("call save_light_node(?,?,?,?,?,?);");
			p_stmt.setInt(1,track_configuration_id);
			p_stmt.setString(2,node_description);
			p_stmt.setString(3,slave_address);
			p_stmt.setDouble(4,x);
			p_stmt.setDouble(5,y);
			p_stmt.setString(6,keyboard_shortcut);
			
			if (DB_Connection.DB_LOG==1)logPreparedStatement(p_stmt);
			rs=p_stmt.executeQuery();

		}catch (Exception exc) {
			System.out.println("Problem saving light info.");
			System.out.println(exc.toString());
		}
		return rs;
	}

	public static ResultSet getTrackInfo(Integer track_id) {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			p_stmt=DB_Connection.conn.prepareStatement("call get_track_info(?);");
			p_stmt.setInt(1,track_id);
			rs=p_stmt.executeQuery();
		}catch (Exception exc) {
			System.out.println("Problem with get_track_info.");
			exc.printStackTrace();
		}
		return rs;
	}	

	public static ResultSet getLightNodes(Integer track_id) {
		PreparedStatement p_stmt;
		ResultSet rs=null;

		try {
			p_stmt=DB_Connection.conn.prepareStatement("call get_light_nodes(?);");
			p_stmt.setInt(1,track_id);
			rs=p_stmt.executeQuery();
		}catch (Exception exc) {
			System.out.println("Problem with get_light_nodes.");
			exc.printStackTrace();
		}
		return rs;
	}		
}//end class DB_Calls
