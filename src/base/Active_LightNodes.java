package base;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import db_info.DB_Calls;

public class Active_LightNodes{

	public static Vector<LightNode> active_nodes; //reference for the nodes

	public static BackgroundFrame bf;
	public static Integer track_configuration_id;
	public static String track_description;
	public static byte[] track_photo_source;
	
	public Active_LightNodes() {
		active_nodes=new Vector<LightNode>();

	}//end class Active_Work_GUIss

	/**This method adds the new light node to our record data set of active nodes
	 * 
	 * @param node
	 */
	public static void addLightNode(LightNode node){
		if(LightManager.DEBUG2) System.out.println("Adding node: "+node.node_panel.getName());
		active_nodes.add(node);
	}

	
	public static void removeLightNode(MouseEvent initial_mouse_event) {
		int i=0;
		while (i<active_nodes.size()){
			LightNode this_node=active_nodes.elementAt(i);
			if (determineNode(this_node, initial_mouse_event)){
				String node_description=((TitledBorder)((CompoundBorder)this_node.node_panel.getBorder()).getOutsideBorder()).getTitle();
				if(LightManager.DEBUG2) System.out.println("Removing "+node_description);
				active_nodes.elementAt(i).background_img_panel.remove(
						active_nodes.elementAt(i).node_panel);
				active_nodes.elementAt(i).background_img_frame.updateUI();
				active_nodes.removeElementAt(i);
				break;
			}
			i++;
		}
		if(LightManager.DEBUG2)printActiveNodes();
	}

	private static void printActiveNodes() {
		int i=0;
		while (i<active_nodes.size()){
			LightNode this_node=active_nodes.elementAt(i);
				String node_description=((TitledBorder)((CompoundBorder)this_node.node_panel.getBorder()).getOutsideBorder()).getTitle();
				System.out.println("\t\tActive_LightNodes: pos "+i+" is "+node_description);
						i++;
		}
	}
	

	public static void removeAllLightNodes() {
		if (LightManager.DEBUG2)System.out.println("Active_LightNodes: Getting ready to remove All Light Nodes");

		for(int i=0;i<active_nodes.size();i++){
			active_nodes.elementAt(i).background_img_panel.remove(active_nodes.elementAt(i).node_panel);
			active_nodes.elementAt(i).background_img_frame.updateUI();
		}
		active_nodes.removeAllElements();
	}	

	/**This method iterates through all active light nodes and returns their
	 * physical slave addresses in a Vector of Strings; it is useful for when
	 * the application needs to address all of the active light nodes at once.
	 * 
	 * @return Modem Slave Addresses of all active nodes
	 */
	public static Vector<String> getValidAddressesAsVector(){
		Vector<String> valid_addresses=new Vector<String>();
		for(int i=0;i<active_nodes.size();i++){
			String potential_address=active_nodes.elementAt(i).slave_address_str;
			if (potential_address!=null){
				valid_addresses.add(potential_address);
			}
		}
		return valid_addresses;
	}

	/**This method iterates through all active light nodes and invokes
	 * the lightbutton action on all lightbuttons whose keyboard shortcuts
	 * are equal to the keyboard shortcut passed in
	 * 
	 * @param input_str
	 */
	public static void invokeLightButtonActionEventAcrossAffectedNodes(String input_str){
		Vector<String> radio_addresses_to_be_invoked=new Vector<String>();
		Boolean turn_on;
		Color summary_color=Color.lightGray;  //default the first color to lightGray
		Color active_node_color=Color.lightGray;

		for(int i=0;i<active_nodes.size();i++){
			if ( active_nodes.elementAt(i).slave_address_str==null ||
				 active_nodes.elementAt(i).slave_address_str.equals("")||
				 active_nodes.elementAt(i).slave_address_str.equals("-1")){
				//when an slave address is present, skip over it as it is not valid
				continue;
			}

			if ( 
				     ((input_str.equals(active_nodes.elementAt(i).keyboard_shortcut)) ||
				      (input_str.equals(active_nodes.elementAt(i).slave_address_str))  )&&
					      (!radio_addresses_to_be_invoked.contains(active_nodes.elementAt(i).slave_address_str))
				){
				//store the physical address of all light nodes whose keyboard shortcut character is equal
				//to the keyboard shortcut or slave address passed in
				radio_addresses_to_be_invoked.add(active_nodes.elementAt(i).slave_address_str);//update the local record
				active_node_color=active_nodes.elementAt(i).lightButton_control_color;
				if (active_node_color==Color.RED){
					//if any of the light nodes are RED, then the full course caution is on;
					//tell the user to turn off the full course caution with the button
					//and we don't do anything further in this method
					errDialog("Turn off by pressing the Full Course Caution Button");
					return;
				}
				if (active_node_color==Color.YELLOW){
					//the summary color is only updated from its default state if the active node color
					//is something other than a shade of gray
					//Therefore, if there are 3 nodes with the same keyboard shortcut, and only one is on-'Yellow'
					//we will assume the summary color is 'Yellow' for the subset
					summary_color=active_node_color;
				}
			}
		}
		if (radio_addresses_to_be_invoked.size()>0){
			if (summary_color==Color.lightGray ||
					summary_color==Color.darkGray){
				//light was off already
				turn_on=true;
			}else{
				//light was on already
				turn_on=false;
			}
			bf.subSetCourseAction(radio_addresses_to_be_invoked, turn_on);
		}
	}//end method invokeLightButtonActionEventAcrossAffectedNodes	

		
	
	/** Convenience routine: Show a standard-form error dialog */
	private static void errDialog(String message) {
		JOptionPane.showMessageDialog(null, message,
				"Error", JOptionPane.ERROR_MESSAGE);
		return;
	}
	
	public static LightNode getLightNodeAtMouseEvent(MouseEvent initial_mouse_event){
		int i=0;
		while (i<active_nodes.size()){
			LightNode this_node=active_nodes.elementAt(i);
			if (determineNode(this_node, initial_mouse_event)){
				if(LightManager.DEBUG2)System.out.println("Moving "+
						                    active_nodes.elementAt(i).node_panel.toString());
				return this_node;
			}
			i++;
		}	
		return null;
	}

	private static boolean determineNode(LightNode t_node, MouseEvent initial_mouse_event){
		MouseEvent adjusted_e=SwingUtilities.convertMouseEvent((Component)initial_mouse_event.getSource(), 
				initial_mouse_event, t_node.background_img_panel);
		Point mouse_point=adjusted_e.getPoint();
		Point top_left_node_point=t_node.node_panel.getLocation();

		Dimension node_dimension=t_node.node_panel.getSize();
		Double x_max=top_left_node_point.getX()+node_dimension.getWidth();
		Double y_max=top_left_node_point.getY()+node_dimension.getHeight();

		if(LightManager.DEBUG2)System.out.println("Active_LightNodes:  mouse was "+mouse_point.toString()+" and \nnode_panel was at "+
				top_left_node_point.toString());
		if(LightManager.DEBUG2)System.out.println("\t max node_panel x="+x_max+", "+y_max);

		boolean x_ok=false;
		boolean y_ok=false;
		//if the x-component of the mouse point is bigger than the leftmost x-component
		//of the node panel and 
		//the x-component of the mouse point is smaller than x_max
		//then the x component is OK

		if ((mouse_point.getX()>=top_left_node_point.getX()) && 
				(mouse_point.getX()<=x_max))
			x_ok=true;
		else x_ok=false;

		//if the y-component of the mouse point is bigger than the leftmost y-component
		//of the node panel and 
		//the y-component of the mouse point is smaller than y_max
		//then the y component is OK
		if ((mouse_point.getY()>=top_left_node_point.getY()) && 
				(mouse_point.getY()<=y_max))
			y_ok=true;
		else y_ok=false;

		return (x_ok&&y_ok);//AND the two components together for an overall result
	}

	/**This method translates the existing keyboard_shortcut and slave_address into a character
	 * it receives a String as the keyboard_shortcut and a String representation of the slave_address
	 * string parameter.
	 * 
	 * @param keyboard_shortcut, slave_address
	 * @return key code
	 */
	public static String determineKeyBoardShortcut(String keyboard_shortcut, String slave_address){
		String result="-1";
		
		if ((keyboard_shortcut== null)||
				(keyboard_shortcut.equals(""))||
				(keyboard_shortcut.equals("-1")) ){
			//compute keyboard_shortcut based on slave_address
			String short_slave_address=slave_address;
			//remove any leading zero
			if (slave_address.equals("-1")){
				//if the slave address is negative return a empty String for the shortcut
				return "";
			}
			if (slave_address.startsWith("0")){
				short_slave_address=slave_address.substring(1);//get the slave address minus any leading zero
			}
			Integer integer_value_of_short_slave_address=Integer.valueOf(short_slave_address);

			//see if the integer_value_of_short_slave_address is less than 10
			if (integer_value_of_short_slave_address<10){
				//if it's less than 10, there is only one digit, so we can just return the value
				//of the single digit
				result=short_slave_address;
			}else{
				//the slave address is greater than or equal to ten, so we need to do some enumeration
				//convert int_val to ASCII
				//subtract from it theASCII value for '9', which is 57Dec
				//therefore, int_valDEC - 57DEC
				//to get the offset from '9'
				//Now we want to start from the ASCII value for 'a' which is 97Dec
				//plus the offset-1 (minus one is so we line we don't skip the initial val)
				//so the calculation becomes 97Dec+(offset-1) for the Dec ASCII value
				//for our desired character
				//example: 10 becomes 'a',  20 becomes 'k'
				int delta=10-integer_value_of_short_slave_address;
				int target_ascii_value=97+delta;
				char tmp_result=(char)target_ascii_value;
				result=tmp_result+"";//make a String out of the single character
			}

		}else{
			//the keyboard shortcut has been set already so we just
			//return the keyboard_shortcut as the first character of the existing keyboard_shortcut
			result=keyboard_shortcut;
		}
		return result;
	}

	public static void setBackgroundVariables(BackgroundFrame background){
		bf=background;
		track_configuration_id=0;//new file so initialize ID to 0
		track_description=background.local_frame.getTitle();
		track_photo_source=background.img_panel.picture_bytes;
	}
	
	public static void setBackgroundVariables(BackgroundFrame background, Integer track_id){
		bf=background;
		track_configuration_id=track_id;
		track_description=background.local_frame.getTitle();
		track_photo_source=background.img_panel.picture_bytes;
	}

	
	private static void updateTrackConfigurationId(Integer track_id){
		track_configuration_id=track_id;
	}
	
	/**Verifies the description of the track configuration to be saved; the existing
	 * track description is passed in and the users choice is returned.
	 * 
	 * @param existing_track_description
	 * @return updated track description
	 */
	private static String verifyTrackDescriptionForSave(String existing_track_description){
		String result=JOptionPane.showInputDialog(null, "Type in track description", existing_track_description);
		if  ((result!=null)&&(!(result.trim()).equals("")))  {
			//if the user hasn't canceled or closed the dialog and the result is not blank, update the frame title
			//and return the result
			track_description=result;//update the track_description with the newly entered value
			if (bf==null){
				System.out.println("Active_LightNodes: bf was null");
				}
			bf.setFrameTitle(track_description);
			return result;
		}else if((result!=null)&&((result.trim()).equals(""))){
			//if the user hasn't canceled or closed the dialog and the result is blank, prompt the user again
			JOptionPane.showMessageDialog(null, "Please enter a track description.", "Warning",
					JOptionPane.WARNING_MESSAGE,null);
			return (verifyTrackDescriptionForSave(existing_track_description)); //recursively call this method until a string is entered
		}else{ 
			//otherwise the result is "null" which means the user has canceled or closed the dialog, so
			//just return null so the save routine is canceled
			return null;
		}
	}

	/**This method saves both the background image and any Active LightNodes
	 * to the backend database. The parameter verify_before_save is used to determine
	 * whether or not to double check with the user that the track description is the 
	 * one they want. 
	 * 
	 * @param verify_before_save  is used for as the determining factor for prompting the user
	 * to verify the track description is the one they want.
	 * @return a boolean value for whether the save completed successfully or not
	 */
	public static Boolean saveState(Boolean verify_before_save){
		String verified_track_description=new String();
		
		if (track_photo_source==null){
			JOptionPane.showMessageDialog(null, "No Track Loaded; Nothing to Save.", "Warning",
					JOptionPane.WARNING_MESSAGE,null);
			return false;
		}
		int i=0;
		ResultSet rs=null;
		
		//if the method was invoked with the verify_before_save parameter
		//check with the user to make sure they want to save the track with this name
		//otherwise, just go with the track description you have on record
		if (verify_before_save){
			verified_track_description=verifyTrackDescriptionForSave(track_description);	
		}else{
			verified_track_description=track_description;
		}
		
		if (verified_track_description==null){
			//the user canceled out of the dialog indicating that they wanted to stop of the save,
			//so just return without taking any further action
			return false;	
		}
		rs=DB_Calls.saveTrackPic(track_configuration_id, verified_track_description, track_photo_source);
		try {
			if (rs.first()) {
				updateTrackConfigurationId(rs.getInt("updated_id")); //update our records
			}
		}catch (SQLException e) {
			System.err.println("Problem retrieving updated track_configuration_id.");
			e.printStackTrace();
			return false;
		}

		//clear any existing light nodes associated with this track_id
		DB_Calls.clearNodesAtTrack(track_configuration_id);
		while (i<active_nodes.size()){
			LightNode this_node=active_nodes.elementAt(i);
			String node_description=((TitledBorder)((CompoundBorder)this_node.node_panel.getBorder()).getOutsideBorder()).getTitle();
			Point top_left_node_point=this_node.node_panel.getLocation();
			DB_Calls.saveLightNode(track_configuration_id,node_description, this_node.slave_address_str,
					top_left_node_point.getX(),top_left_node_point.getY(),
					this_node.keyboard_shortcut);
			i++;
		}
		return true;
	}//end method saveState
	

}//end class Active_LightNodes

