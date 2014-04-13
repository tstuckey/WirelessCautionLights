package base;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.MouseInputAdapter;

import base.LightNode.BlinkerJob;
import utilities.MyKeyAdapter;
import modem_comms.ModemActions;
import modem_comms.ReceivedMessagedPool;
import db_info.DB_Calls;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;


/* Used by Setup_Desktop.java. */
//public class ImageInternalFrame extends JInternalFrame implements InternalFrameListener,
//ActionListener, MouseListener, MouseMotionListener{
public class BackgroundFrame extends JInternalFrame 
implements ActionListener,InternalFrameListener{
	Integer my_track_id;
	JDesktopPane local_desktop;   
	ModemActions modemActions;
	JInternalFrame local_frame;
	JScrollPane areaScrollPane;
	ImageJPanel img_panel;
	JPopupMenu popup;
	MouseEvent initial_mouse_event;

	JButton fullCourseButton;
	JButton everythingOffButton;
	
	GridBagConstraints class_c;
	LightNode moving_node;
	
	MyMouseAdapter mymouseadapter;
	MyKeyAdapter mykeyadapter;
	
	TrackInfo the_track_info;
	
	int fullCourseButtonWidth=200;
	int fullCourseButtonHeight=50;
	
	int everythingOffButtonWidth=100;
	int everythingOffButtonHeight=30;

	/**This constructor is used when a photo from the computer is used as the background
	 * @param desktop
	 * @param modemActions
	 * @param imageFileName
	 */
	public BackgroundFrame(JDesktopPane desktop,ModemActions modemActions,File imageFileName) {
		local_desktop=desktop;
		this.modemActions=modemActions;
		initializeClassVariables(imageFileName);
		local_frame.addInternalFrameListener(this);
		local_frame.add(img_panel,class_c);
		class_c.insets=getCenterInsets(img_panel.getPreferredSize().width,fullCourseButtonWidth );

		JPanel button_panel=new JPanel();
		GridBagConstraints gb=new GridBagConstraints();
		gb.anchor=GridBagConstraints.CENTER;
		gb.gridwidth=GridBagConstraints.REMAINDER;
		button_panel.add(fullCourseButton,gb);
		button_panel.add(everythingOffButton,gb);
		local_frame.add(button_panel,class_c);		
		
		local_frame.pack();
		local_frame.setVisible(true);
		
		local_desktop.add(local_frame);
		setFrameTitle(getTrackTitle());
		enableComponents();
		singleAutoClickToActiveFrame();
		enableBackgroundFrameKeyListener(true);
	}
	
	/**This constructor is used when the user requests a photo from the database;
	 * it is identified by the track id
	 * 
	 * @param desktop
	 * @param modemActions
	 * @param track_id
	 */
	public BackgroundFrame(JDesktopPane desktop,ModemActions modemActions,Integer track_id) {
		local_desktop=desktop;
		this.modemActions=modemActions;
		my_track_id=track_id;
		the_track_info=getBackgroundInfo(track_id);
		initializeClassVariables(the_track_info);
		local_frame.addInternalFrameListener(this);
		local_frame.add(img_panel, class_c);
		class_c.insets=getCenterInsets(img_panel.getPreferredSize().width,fullCourseButtonWidth );
		
		JPanel button_panel=new JPanel();
		GridBagConstraints gb=new GridBagConstraints();
		gb.anchor=GridBagConstraints.CENTER;
		gb.gridwidth=GridBagConstraints.REMAINDER;
		button_panel.add(fullCourseButton,gb);
		button_panel.add(everythingOffButton,gb);
		local_frame.add(button_panel,class_c);
		
		local_frame.pack();
		local_frame.setVisible(true);

		local_desktop.add(local_frame);
		setFrameTitle(the_track_info.track_description);
		enableComponents();
		singleAutoClickToActiveFrame();
		enableBackgroundFrameKeyListener(true);
	}

	public void singleAutoClickToActiveFrame(){
		/*
		 * UNABLE TO GET THE direct invocation of the mouseevents to pass
		 * focus to the child components of the JInternalFrame
		 * this means the user would still have to click in the JInternalFrame
		 * before the keyboard input would be acknowledged by the individual
		 * components
		 */
		 //local_frame.dispatchEvent(new MouseEvent(local_frame,MouseEvent.MOUSE_PRESSED,0,MouseEvent.BUTTON1_MASK,1,1,1, false));
		 //local_frame.dispatchEvent(new MouseEvent(local_frame,MouseEvent.MOUSE_RELEASED,0,MouseEvent.BUTTON1_MASK,1,1,1, false));
	
		local_desktop.getDesktopManager().activateFrame(local_frame);//make this the frame at the front
		try {
			this.requestFocusBackToBackground();
			java.awt.Robot tmp= new java.awt.Robot();
			tmp.mousePress(InputEvent.BUTTON1_MASK);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}	
	}
	
	/**This method makes the call to the stored procedure to query the database for the background
	 * image and description
	 * 
	 * @param track_id
	 * @return TrackInfo
	 */
	private TrackInfo getBackgroundInfo(Integer track_id){
		TrackInfo result=new TrackInfo();
		ResultSet rs=null;

		rs=DB_Calls.getTrackInfo(track_id);
		try {
			if (rs.first()) {
				result.track_description=rs.getString("description");
				result.track_image = new byte[rs.getBytes("photo").length];
				result.track_image=rs.getBytes("photo");
			}
		}catch (SQLException e) {
			System.err.println("BackgroundFrameUsingImg: Problem retrieving data.");
			e.printStackTrace();
		}
		result.db_info_on_light_nodes=getLightNodeInfo(track_id);
		
		return result;
	}

	/**This method makes the call to the stored procedure to query the database for the light
	 * node information for the given track_id; it returns a Vector of LightNodeAttributes;
	 * each entry in the Vector contains the attributes of each light node
	 * 
	 * @param track_id
	 * @return Vector<LightNodeAttributes>
	 */
	private Vector<LightNodeAttributes> getLightNodeInfo(Integer track_id){
		ResultSet rs=null;
		Vector<LightNodeAttributes> local_nodes=new Vector<LightNodeAttributes>();

		rs=DB_Calls.getLightNodes(track_id);
		try {
			if (rs.first()) {
				do {
					LightNodeAttributes t_node=new LightNodeAttributes();
					t_node.description=trimMe(rs.getString("description"));
					t_node.slave_address=trimMe(rs.getString("slave_address"));
					t_node.x_coord=rs.getInt("x_coord");
					t_node.y_coord=rs.getInt("y_coord");
					t_node.keyboard_shortcut=Active_LightNodes.determineKeyBoardShortcut(trimMe(rs.getString("keyboard_shortcut")),
							                                           t_node.slave_address);
					local_nodes.add(t_node);
				} while (rs.next());
			}
		}catch (SQLException e) {
			System.err.println("BackgroundFrameUsingImg: Problem retrieving data.");
			e.printStackTrace();
		}
		return local_nodes;
	}

	private String trimMe(String input){
		if (input!=null){
			return input.trim();
		}
			return input;
	}
	
/**This method initializes the class variable when the user has selected a new image as a background.
 * It takes the filename of the image as the input parameter. 
 * 
 * @param imageFileName
 */
	private void initializeClassVariables(File imageFileName) {
		Active_Backgrounds.disposeActiveBackgrounds(local_desktop);
		local_frame=new JInternalFrame("",true,true,true,true);
		local_frame.setLayout(new GridBagLayout());
		class_c = new GridBagConstraints();
		class_c.anchor=GridBagConstraints.NORTHWEST;
		class_c.gridwidth=GridBagConstraints.REMAINDER;
		popup=new JPopupMenu();
		initial_mouse_event=null;
		moving_node=null;

		img_panel = new ImageJPanel(imageFileName,local_desktop,
				fullCourseButtonWidth,fullCourseButtonHeight); // create new panel
		
		mymouseadapter = new MyMouseAdapter();
		mykeyadapter= new MyKeyAdapter(this);

		fullCourseButton=setupFullCourseButton();
		everythingOffButton=setupEverythingOffButton();
		setUpPopup();
	}
	private String getTrackTitle(){
		String result=JOptionPane.showInputDialog(null, "Enter Track Description:");
		return result;
	}
	
	public void setFrameTitle(String description) {
		String title_string = (description);
		local_frame.setTitle(title_string);
	}

	/**This method initializes the class variable when the user has selected a track; the
	 * track info already pulled from the db is passed into this method.
	 * 
	 * @param db_track_info
	 */
	private void initializeClassVariables(TrackInfo db_track_info) {
		Active_Backgrounds.disposeActiveBackgrounds(local_desktop);
		local_frame=new JInternalFrame("",true,true,true,true);
		local_frame.setLayout(new GridBagLayout());
		class_c = new GridBagConstraints();
		class_c.anchor=GridBagConstraints.NORTHWEST;
		class_c.gridwidth=GridBagConstraints.REMAINDER;
		popup=new JPopupMenu();
		initial_mouse_event=null;
		moving_node=null;

		img_panel = new ImageJPanel(db_track_info.track_image,local_desktop,
				fullCourseButtonWidth,fullCourseButtonHeight); // create new panel
		
		//go through each node we received from the db query earlier
		//add a light node in the appropriate place
		for(int i=0;i<db_track_info.db_info_on_light_nodes.size();i++){
			Active_LightNodes.addLightNode(
					new LightNode(local_desktop,local_frame,this,modemActions,img_panel,db_track_info.db_info_on_light_nodes.elementAt(i)));
		}
		
		mymouseadapter = new MyMouseAdapter();
		mykeyadapter= new MyKeyAdapter(this);

		fullCourseButton=setupFullCourseButton();
		everythingOffButton=setupEverythingOffButton();
		setUpPopup();
	}
	
	private JButton setupFullCourseButton(){
		JButton tButton=new JButton("<html>Full Course Caution<p><b>Shortcut:</b> <i>Spacebar</i></html>");
		tButton.setBackground(Color.lightGray);
		tButton.setPreferredSize(new Dimension(fullCourseButtonWidth,fullCourseButtonHeight));
		tButton.addActionListener (new java.awt.event.ActionListener () {
			public void actionPerformed (java.awt.event.ActionEvent evt) {
				fullCourseButtonPerformed (evt);
			}
		}
		);
	return tButton;
	}
	
	private JButton setupEverythingOffButton(){
		JButton tButton=new JButton("<html>Everything<p><b>OFF</b></html>");
		tButton.setBackground(Color.cyan);
		tButton.setPreferredSize(new Dimension(everythingOffButtonWidth,everythingOffButtonHeight));
		tButton.addActionListener (new java.awt.event.ActionListener () {
			public void actionPerformed (java.awt.event.ActionEvent evt) {
				everythingOffButtonPerformed (evt);
			}
		}
		);
	return tButton;
	}

	
	
	
	/**This method returns the insets that will center the button below the panel
	 * 
	 * @param panelWidth
	 * @param buttonWidth
	 * @return Insets to center the button
	 */
	private Insets getCenterInsets(int panelWidth, int buttonWidth){
		int left_space=(panelWidth-buttonWidth)/2;
		if (LightManager.DEBUG2)System.out.println("BackgroundFrame: panelWidth is "+panelWidth+
                " and buttonWidth is "+buttonWidth+" and left_space is "+left_space);
		return (new Insets(5,left_space,5,0));
	}

	
	/** Convenience routine: Show a standard-form error dialog */
	private void errDialog(String message) {
		JOptionPane.showMessageDialog(null, message,
				"Error", JOptionPane.ERROR_MESSAGE);
		return;
	}
	
    public  void setCursorWait(Boolean state){
    	//We had to make a private method to get the cursor to render correctly
    	//for the JInternalFrame; using the setCursorWait in The_Desktop class
    	//wasn't getting the results we needed
    	if (state){
    		    RootPaneContainer root = (RootPaneContainer)local_frame.getTopLevelAncestor();
    		    root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    		    root.getGlassPane().setVisible(true);
 
    	}else{
		    RootPaneContainer root = (RootPaneContainer)local_frame.getTopLevelAncestor();
		    root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		    root.getGlassPane().setVisible(true);
        	}
    }
	
	public void setUpPopup() {
		JMenuItem discover_row= new JMenuItem("Discover Towers");
		discover_row.setActionCommand("discover towers");
		discover_row.addActionListener(this);
		popup.add(discover_row);

		JMenuItem add_row= new JMenuItem("Add Light Tower");
		add_row.setActionCommand("add tower");
		add_row.addActionListener(this);
		popup.add(add_row);

		JMenuItem save_row= new JMenuItem("Save State");
		save_row.setActionCommand("save");
		save_row.addActionListener(this);
		popup.add(save_row);
		
	}//end setUp_Table_Popup

	public void disableComponents() {
		local_frame.removeMouseListener(mymouseadapter);
	}

	public void enableComponents() {
		local_frame.addMouseMotionListener(mymouseadapter);
		local_frame.addMouseListener(mymouseadapter);
	}	

	class TrackInfo{
		public String track_description=null;
		byte [] track_image=null;
		Vector<LightNodeAttributes> db_info_on_light_nodes=new Vector<LightNodeAttributes>();
	}


	class MyMouseAdapter extends MouseInputAdapter{

		public void mouseClicked(MouseEvent e){
			//System.out.println("processing mouseClicked event "+e);
		}

		public void mouseReleased(MouseEvent e) {
			//System.out.println("processing mouseReleased event "+e);
		}		
		
		public void mousePressed(MouseEvent e) {
			//System.out.println("processing mousePressed event "+e);
			//if it is a right-click
			if (e.getButton()>MouseEvent.BUTTON1) {
				initial_mouse_event=e;
				popup.show(e.getComponent(),e.getX(), e.getY());
			}
		}
	}//end class MyMouseAdapter

	/**This requests the JPanel with the background image to become the focused component
      *this, in turn, allows the keyboardlistener to accept keyboard inputs and, therefore,
      *the keyboard shortcuts work as well.
	 */
	public void requestFocusBackToBackground(){
		img_panel.requestFocusInWindow(); 
	}
	
	public void fullCourseButtonPerformed (ActionEvent evt){
		Boolean turn_on;
		if (fullCourseButton.getBackground()==Color.lightGray){
			turn_on=true;
		}else{
			turn_on=false;
		}
		
		//we are not connected OR they're aren't any
		//valid nodes, so abort
		if ((modemActions.state==0)||(Active_LightNodes.active_nodes.size()==0)){
			errDialog("No Light Towers are Valid!");
			this.requestFocusBackToBackground();
			return;
		}
		
		this.setCursorWait(true);
		invokeCMDsentMessage(Active_LightNodes.getValidAddressesAsVector());
		modemActions.fullCourseCaution(local_frame,turn_on,Active_LightNodes.getValidAddressesAsVector(),this);
		this.requestFocusBackToBackground();
	}

	/**This method is invoked by the execution thread in ModemActions.FullCourseJob_asIndividuals after the full course command has completed;
	 *   This method takes the received pool and the original color request for the full course as the input parameter.
	 * 
	 * @param t_final_pool, org_turn_on_request, light_color
	 */
	public void updateFullCourse_asIndividuals(ReceivedMessagedPool t_final_pool, Boolean org_turn_on_request, Color light_color){
		if (base.LightManager.COMM_DEBUG)System.out.println("BackgroundFrame: updateFullCourse: starting processing");
		Color requested_color;

		if (org_turn_on_request){
			//if the request was true; the user wanted to turn on the light color to the specified light_color
			requested_color=light_color;
		}else{
			//if the request was false; the user wanted to turn off the red-light
			requested_color=Color.lightGray;	
		}

		Boolean response_received;

		fullCourseButton.setBackground(requested_color);
		
		for (int act_light_index=0; act_light_index<Active_LightNodes.active_nodes.size();act_light_index++){
			response_received=false;//initialize the flag for this act_light_index
			LightNode active_node_ref=Active_LightNodes.active_nodes.elementAt(act_light_index);

			//we assume the requested command gets through for the state of the lightnode
			doCorrectBlink(requested_color, active_node_ref);
			
			//Now we process the ack's and notify accordingly
			for (int rec_pool_index=0; rec_pool_index<t_final_pool.received_pool.size();rec_pool_index++){
				if (t_final_pool.received_pool.elementAt(rec_pool_index).received_from_address.equals(active_node_ref.slave_address_str)){
					//if the addresses match, we got a response for the active slave address
					//set the flag to indicate we received a response
					response_received=true;

					//Check if it was ACKED
					if (t_final_pool.received_pool.elementAt(rec_pool_index).acked==1){
						//if a response's received from address matches the slave address of this node AND
						//it received an ACK, change its color
						//doCorrectBlink(requested_color, active_node_ref);
						active_node_ref.setACKfield(LightNode.POSITIVE_RESPONSE);
					}

					//Check if it was NACKED						
					if (t_final_pool.received_pool.elementAt(rec_pool_index).acked==0){
						//if a response's received from address matches the slave address of this node AND
						//it received a NACK, color it darkGray
						//doCorrectBlink(Color.darkGray, active_node_ref);
						active_node_ref.setACKfield(LightNode.NEGATIVE_RESPONSE);
					}

					break; //we found the match, so don't iterate any more
				}//end if the addresses match
			} //end for rec_pool_index loop

			if (!response_received){
				//At this point, we've either found a matching address in the received_message_pool or iterated through
				//all of the addresses in received_message_pool unsuccessfully;
				//if we were unsuccessful, we need to indicate that we didn't get anything back
				//we are going to color the light node the same as if we received as NACK
				//doCorrectBlink(Color.darkGray, active_node_ref);
				active_node_ref.setACKfield(LightNode.NEGATIVE_RESPONSE);
			}
		}//end for act_light_index loop

		this.requestFocusBackToBackground();
		this.setCursorWait(false);
		if (base.LightManager.COMM_DEBUG)System.out.println("BackgroundFrame: updateFullCourse_asIndividuals: done processing");
	}//end method updateFullCourse_asIndividuals

	
	public void subSetCourseAction (Vector<String> actionable_addresses, Boolean turn_on){
		//we are not connected OR they're aren't any
		//valid nodes, so abort
		if ((modemActions.state==0)||(Active_LightNodes.active_nodes.size()==0)){
			errDialog("No Light Towers are Valid!");
			this.requestFocusBackToBackground();
			return;
		}
		this.setCursorWait(true);
		invokeCMDsentMessage(actionable_addresses);
		modemActions.sendIndividualCommandToMultipleAddresses(local_frame,turn_on,actionable_addresses,this);
		this.requestFocusBackToBackground();
	}	

	/**This method is invoked by the execution thread in ModemActions.SubsetCourseJob after the full course command has completed;
	 *   This method takes the received pool and the original color request for the full course as the input parameter.
	 * 
	 * @param t_final_pool, org_turn_on_request, light_color
	 */
	public void updateSubsetCourse(Vector<String> original_addresses_talked_to,ReceivedMessagedPool t_final_pool, Boolean org_turn_on_request, Color light_color){
		if (base.LightManager.COMM_DEBUG)System.out.println("BackgroundFrame: updateSubsetCourse: starting to process");
		Color requested_color;

		if (org_turn_on_request){
			//if the request was true; the user wanted to turn on the light color to the specified light_color
			requested_color=light_color;
		}else{
			//if the request was false; the user wanted to turn off the red-light
			requested_color=Color.lightGray;	
		}

		Boolean response_received;

		for (int act_light_index=0; act_light_index<Active_LightNodes.active_nodes.size();act_light_index++){
			response_received=false;//initialize the flag for this act_light_index
			LightNode active_node_ref=Active_LightNodes.active_nodes.elementAt(act_light_index);
			if (!original_addresses_talked_to.contains(active_node_ref.slave_address_str)){
				//if this_node, the current node in the Active_LightNodes is NOT
				//in the list of original addresses we were trying to communicate with, then
				//this_node is not of interest to this update routine; so we just continue to
				//the next light node in the list
				continue;
			}

			//we assume the requested command gets through for the state of the lightnod	
			doCorrectBlink(requested_color, active_node_ref);

			//Now we process the ack's and notify accordingly
				for (int rec_pool_index=0; rec_pool_index<t_final_pool.received_pool.size();rec_pool_index++){
					if (t_final_pool.received_pool.elementAt(rec_pool_index).received_from_address.equals(active_node_ref.slave_address_str)){
						//if the addresses match, we got a response for the active slave address
						//set the flag to indicate we received a response
						response_received=true;

						//Check if it was ACKED
						if (t_final_pool.received_pool.elementAt(rec_pool_index).acked==1){
							//if a response's received from address matches the slave address of this node AND
							//it received an ACK, change its color
							//doCorrectBlink(requested_color, active_node_ref);
							active_node_ref.setACKfield(LightNode.POSITIVE_RESPONSE);
						}

						//Check if it was NACKED						
						if (t_final_pool.received_pool.elementAt(rec_pool_index).acked==0){
							//if a response's received from address matches the slave address of this node AND
							//it received a NACK, color it darkGray
							//doCorrectBlink(Color.darkGray, active_node_ref);
							active_node_ref.setACKfield(LightNode.NEGATIVE_RESPONSE);
						}

						break; //we found the match, so don't iterate any more
					}//end if the addresses match
				} //end for rec_pool_index loop

				if (!response_received){
					//At this point, we've either found a matching address in the received_message_pool or iterated through
					//all of the addresses in received_message_pool unsuccessfully;
					//if we were unsuccessful, we need to indicate that we didn't get anything back
					//we are going to color the light node the same as if we received as NACK
					active_node_ref.setACKfield(LightNode.NEGATIVE_RESPONSE);
				}

		}//end for act_light_index loop

		this.requestFocusBackToBackground();
		this.setCursorWait(false);
		if (base.LightManager.COMM_DEBUG)System.out.println("BackgroundFrame: updateSubsetCourse: done processing");
	}//end method updateSubsetCourse

	
	public void everythingOffButtonPerformed (ActionEvent evt){
		//we are not connected OR they're aren't any
		//valid nodes, so abort
		if ((modemActions.state==0)||(Active_LightNodes.active_nodes.size()==0)){
			errDialog("No Light Towers are Valid!");
			this.requestFocusBackToBackground();
			return;
		}
		
		this.setCursorWait(true);
		modemActions.everythingOff(local_frame,this);
		this.requestFocusBackToBackground();

	}

	
	public void updateEverythingOff(){
		if (base.LightManager.COMM_DEBUG)System.out.println("BackgroundFrame: updateFullCourse: starting processing");
		Color requested_color;

		requested_color=Color.lightGray;	
		
		fullCourseButton.setBackground(requested_color);
		
		
		for (int act_light_index=0; act_light_index<Active_LightNodes.active_nodes.size();act_light_index++){
			LightNode active_node_ref=Active_LightNodes.active_nodes.elementAt(act_light_index);

			//we assume the requested command gets through for the state of the lightnode
			doCorrectBlink(requested_color, active_node_ref);
			
			//Now we process the ack's and notify accordingly

			active_node_ref.setACKfield(LightNode.INITIAL_VALUE);
			
		}//end for act_light_index loop

		this.requestFocusBackToBackground();
		this.setCursorWait(false);
		JOptionPane.showMessageDialog(null, "All Lights Are OFF!", "Information",
				JOptionPane.INFORMATION_MESSAGE,null);
		if (base.LightManager.COMM_DEBUG)System.out.println("BackgroundFrame: updateFullCourse_asIndividuals: done processing");
	}//end method updateFullCourse_asIndividuals
	
	
	
	/**This method determines whether or not to cause the light node to blink based on the
	 * requested_color passed in as a parameter.  It invokes the blinking sub-routines when
	 * the requested color is Yellow or RED.
	 * 
	 * @param requested_color
	 * @param active_node_ref
	 */
	private void doCorrectBlink(Color requested_color, LightNode active_node_ref){
		if ((requested_color.equals(Color.yellow))||
				(requested_color.equals(Color.RED   )) ){
				//only blink if Color is RED or Yellow
				active_node_ref.blinkLight(requested_color, true);
			}else{
				active_node_ref.blinkLight(requested_color, false);
			}
	}

	/**This method turns on the notification so the user can see the command has been sent.
	 * 
	 * @param actionable_addresses
	 */
	private void invokeCMDsentMessage(Vector<String> actionable_addresses){
		LightNode node_ref=null;
		for (int act_light_index=0; act_light_index<Active_LightNodes.active_nodes.size();act_light_index++){
			node_ref=Active_LightNodes.active_nodes.elementAt(act_light_index);
			if (actionable_addresses.contains(node_ref.slave_address_str)){
				node_ref.setACKfield(LightNode.PROCESSING);
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if ((e.getActionCommand()).equals("add tower")) {
			this.setCursorWait(true);
			Active_LightNodes.addLightNode(
					new LightNode(local_desktop,local_frame,this,modemActions,img_panel,initial_mouse_event)
			);
			this.setCursorWait(false);
		}	
		if ((e.getActionCommand()).equals("discover towers")) {
			performDiscovery();
		}
		
		if ((e.getActionCommand()).equals("save")) {
			this.setCursorWait(true);
			saveState(true);
			this.setCursorWait(false);
		}
		
	}

	private void performDiscovery(){
		this.setCursorWait(true);
		//delete any existing nodes on this background image
		Active_LightNodes.removeAllLightNodes();
		fullCourseButton.setBackground(Color.lightGray);//resets color in case user had full course caution going and then
		                                                //decided to hit the discovery button without 
		modemActions.discoverNodes(local_frame,this);
	}
	
	/**This method is invoked by the execution thread in ModemActions.SetupScan after discovery has completed;
	 * although this tight coupling is less than desirable, it is necessary due to the use of the Progress Monitor
	 * which requires separate threads to monitor progress effectively.  This method takes the valid_addresses as
	 * the input parameter.
	 * 
	 * @param valid_addresses
	 */
	public void updateWithDiscoveredLightNodes(Vector<String> valid_addresses){
		//create a new light tower for each slave address
		for (int a=0; a<valid_addresses.size();a++){
			LightNodeAttributes attributes=new LightNodeAttributes();
			attributes.slave_address=valid_addresses.elementAt(a);
			//paint the nodes across the desktop starting at a point 25,25
			//and displacing each node 50 pixels x and 50 pixels y
			attributes.x_coord=25+50*a;
			attributes.y_coord=25+50*a;
			attributes.keyboard_shortcut=Active_LightNodes.determineKeyBoardShortcut("-1",attributes.slave_address);
			Active_LightNodes.addLightNode(
					new LightNode(local_desktop,local_frame,this,modemActions,img_panel,attributes));
		}
		local_desktop.repaint();
		this.setCursorWait(false);
	}
	

	/**This is a wrapper method that invokes the safe routine in the Active_LightNodes method.
	 * The parameter verify_before_save is used to determine
	 * whether or not to double check with the user that the track description is the 
	 * one they want. This parameter is passed through to the other method 
	 * 
	 * @param verify_before_save  is used for as the determining factor for prompting the user
	 * to verify the track description is the one they want.
	 */
    public void saveState(Boolean verify_before_save) {
    	this.setCursorWait(true);
    	Boolean result=Active_LightNodes.saveState(verify_before_save);
		if (result){
			JOptionPane.showMessageDialog(null, "Successful Save!", "Information",
				JOptionPane.INFORMATION_MESSAGE,null);
		}else{
			JOptionPane.showMessageDialog(null, "Save Canceled.", "Information",
					JOptionPane.INFORMATION_MESSAGE,null);
		}
		this.setCursorWait(false);
    }	

	/**This method adds or removes the keylistener on the background frame basd on the input Boolean parameter
	 * 
	 * @param enable
	 */
		public void enableBackgroundFrameKeyListener(Boolean enable){
			if (enable){
				//System.out.println("BackgroundFrame:  adding keyadapter to panel");
				img_panel.addKeyListener(mykeyadapter);	
			}else{
				//System.out.println("BackgroundFrame:  removing keyadapter from panel");
				img_panel.removeKeyListener(mykeyadapter);
			}
		}    
   
    @Override
	public void internalFrameActivated(InternalFrameEvent e) {

    }

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {

	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		Active_LightNodes.removeAllLightNodes();

	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}

}
