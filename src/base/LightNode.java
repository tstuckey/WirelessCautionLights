package base;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.MouseInputAdapter;


import modem_comms.ModemActions;
import modem_comms.ReceivedMessagedPool;

public class LightNode implements ActionListener, ItemListener {
	JDesktopPane local_desktop;
	JInternalFrame background_img_frame;
	BackgroundFrame bf;
	ModemActions theModel;
	ImageJPanel background_img_panel;
	GridBagConstraints class_c;
	JPanel node_panel;
	JPanel inner_panel;
	JPopupMenu popup;
	MouseEvent initial_mouse_event;
	MyJButton lightButton;
	Color lightButton_control_color; //this is the control color of the light node
	JTextField keyboard_shortcut_display;
	JTextField acknowledgement_field;
	JCheckBox anchor_box;

	BlinkerJob blink_job=null;
	
	String keyboard_shortcut;   //the character keyboard shortcut
	String slave_address_str; //the slave address of the modem
	
	
	MyMouseAdapter mymouseadapter;
	
	//These are the states for the Light Nodes
	final static int PROCESSING=2;
	final static int POSITIVE_RESPONSE=1;
	final static int NEGATIVE_RESPONSE=0;
	final static int INITIAL_VALUE=-1;
	
	/**Constructor for a new LightNode when then user is manually adding a node.
	 * 
	 * @param desktop
	 * @param t_frame
	 * @param bf
	 * @param theModel
	 * @param t_panel
	 * @param initial_mouse_event
	 */
	public LightNode(JDesktopPane desktop, JInternalFrame t_frame,BackgroundFrame bf,ModemActions theModel,
			ImageJPanel t_panel, MouseEvent initial_mouse_event) {
		this.local_desktop=desktop;
		background_img_frame=t_frame;
		this.bf=bf;
		this.theModel=theModel;
		background_img_panel=t_panel;
		initializeClassVariables(initial_mouse_event);
		enableComponents();
		node_panel.repaint();
	}

	/**Constructor for a new LightNode when the user is adding a light nodes based on attributes from the database.
	 * 
	 * @param desktop
	 * @param t_frame
	 * @param bf
	 * @param theModel
	 * @param t_panel
	 * @param light_attribs
	 */
	public LightNode(JDesktopPane desktop,JInternalFrame t_frame,BackgroundFrame bf,ModemActions theModel, 
			ImageJPanel t_panel, LightNodeAttributes light_attribs) {
		this.local_desktop=desktop;
		background_img_frame=t_frame;
		this.bf=bf;
		this.theModel=theModel;
		background_img_panel=t_panel;
		initializeClassVariables(light_attribs);
		enableComponents();
		node_panel.repaint();
	}
	
	private void initializeClassVariables(MouseEvent e) {
		class_c = new GridBagConstraints();
		class_c.anchor=GridBagConstraints.CENTER;
		class_c.gridwidth=GridBagConstraints.REMAINDER;
		
		popup=new JPopupMenu();
		initial_mouse_event=null;
		mymouseadapter = new MyMouseAdapter();
		setUpPopup();

		inner_panel=new JPanel();//a panel without the normal borders
		inner_panel.setLayout(new GridBagLayout());
		
		//setup the light itself
		setupLightButton(inner_panel);
		
		node_panel=initializeJPanel(getNodeTitle());
		node_panel.add(inner_panel,class_c);
		
		//the acknowledgment area
		acknowledgement_field=new JTextField(7);
		setACKfield(INITIAL_VALUE);
		acknowledgement_field.setEditable(false);//we don't want to let the ack display to be directly edited
		node_panel.add(acknowledgement_field,class_c);
		
		keyboard_shortcut_display=new JTextField(1); 
		keyboard_shortcut_display.setEditable(false);//we don't want to let the shortcut be directly edited
		addTextField(node_panel,"Shortcut", keyboard_shortcut_display);
		
		slave_address_str="-1"; //default address of -1
		
		class_c.anchor=GridBagConstraints.NORTHWEST;
		anchor_box=new JCheckBox("Anchor");
		anchor_box.setSelected(true);
		node_panel.add(anchor_box,class_c);
		
		
		node_panel.setSize(node_panel.getPreferredSize());
		background_img_panel.add(node_panel, class_c);
		//Have to do a conversion to make sure we are talking the same coordinate references
		MouseEvent adjusted_e=SwingUtilities.convertMouseEvent((Component)e.getSource(), e, background_img_panel);
		node_panel.setLocation(adjusted_e.getX(),adjusted_e.getY());
		
		keyboard_shortcut="-1";//initialize the keyboard_shortcut key
		background_img_frame.updateUI();//this refreshes the JInternalFrame and allows the node_panel to show up
	}

	private void initializeClassVariables(LightNodeAttributes light_attribs) {
		class_c = new GridBagConstraints();
		class_c.anchor=GridBagConstraints.CENTER;
		class_c.gridwidth=GridBagConstraints.REMAINDER;

		popup=new JPopupMenu();
		initial_mouse_event=null;
		mymouseadapter = new MyMouseAdapter();
		setUpPopup();

		inner_panel=new JPanel();//a panel without the normal borders
		inner_panel.setLayout(new GridBagLayout());
				
		//setup the light itself
		setupLightButton(inner_panel);
		
		node_panel=initializeJPanel(light_attribs.description);
		node_panel.add(inner_panel,class_c);

		//the acknowledgment area
		acknowledgement_field=new JTextField(7);
		setACKfield(INITIAL_VALUE);
		acknowledgement_field.setEditable(false);//we don't want to let the ack display to be directly edited
		node_panel.add(acknowledgement_field,class_c);

		keyboard_shortcut_display=new JTextField(1);
		keyboard_shortcut_display.setText(light_attribs.keyboard_shortcut+"");
		keyboard_shortcut_display.setEditable(false);//we don't want to let the shortcut directly
		addTextField(node_panel,"Shortcut", keyboard_shortcut_display);
		
		slave_address_str=light_attribs.slave_address;

		class_c.anchor=GridBagConstraints.NORTHWEST;
		anchor_box=new JCheckBox("Anchor");
		anchor_box.setSelected(true);
		node_panel.add(anchor_box,class_c);
		
		node_panel.setSize(node_panel.getPreferredSize());
		background_img_panel.add(node_panel, class_c);
		node_panel.setLocation(light_attribs.x_coord,light_attribs.y_coord);

		keyboard_shortcut=light_attribs.keyboard_shortcut;//set the keyboard_shortcut key
		background_img_frame.updateUI();//this refreshes the JInternalFrame and allows the node_panel to show up
	}
	
	private String getNodeTitle(){
		String result=JOptionPane.showInputDialog(null, "Enter Description for this Light Tower:");
		return result;
	}

	private void setupLightButton(JPanel inner_panel){
		lightButton=new MyJButton("");
		lightButton.addMouseListener(new LightButtonActionAdapter());
		inner_panel.add(lightButton,class_c);
	}

	private void addTextField(JPanel t_panel, String t_label, JTextField t_field) {
		GridBagConstraints local_c = new GridBagConstraints();
		local_c.anchor=GridBagConstraints.CENTER;
		local_c.gridwidth=1;
		local_c.insets=new Insets(5,3,0,0);
		
		JPanel local_panel=new JPanel();
		local_panel.setLayout(new GridBagLayout());
		local_panel.add(t_field,local_c);
		local_c.gridwidth=GridBagConstraints.REMAINDER;
		local_panel.add(new JLabel(t_label),local_c);
		
		t_panel.add(local_panel,class_c);
	}   
	
	public void lightButtonAction (java.awt.event.ActionEvent evt) {
		Boolean turn_on;
		
		//we are not connected so abort
		if ((theModel.state==0)||(this.slave_address_str==null)||
			(this.slave_address_str.equals(""))||(this.slave_address_str.equals("-1"))){
			notifyUser();
			bf.requestFocusBackToBackground();
			return;
		}

		//if the button is red from a full course caution
		//notify the user to press the full course button again
		//and return
		if (lightButton_control_color==Color.RED){
			errDialog("Turn off by pressing the Full Course Caution Button");
			return;
		}
		
		if (lightButton_control_color==Color.lightGray ||
				lightButton_control_color==Color.darkGray){
			//light was off already
			turn_on=true;
		}else{
			//light was on already
			turn_on=false;
		}
		
		bf.setCursorWait(true);//this just makes the GUI make a little more sense to the user;
		this.setACKfield(PROCESSING);//tell the user we are processing their request
		//theModel.sendIndividualCommand(background_img_frame, turn_on,this.slave_address_str, this);
		Active_LightNodes.invokeLightButtonActionEventAcrossAffectedNodes(this.slave_address_str);
	}


	/**This method is invoked by the execution thread in ModemActions.SingleJob after the singleJob command has completed;
	 *   This method takes the received pool as the input parameter.
	 * 
	 * @param received pool
	 */
/*	public void updateLightNode(ReceivedMessagedPool t_final_pool, Boolean org_turn_on_request){
		if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: updateLightNode: starting to process");
		Color requested_color;

		if (org_turn_on_request){
			//if the request was true; the user wanted to turn on the yellow-light
			requested_color=Color.YELLOW;
		}else{
			//if the request was false; the user wanted to turn off the yellow-light
			requested_color=Color.lightGray;	
		}

		Boolean response_received;

		for (int act_light_index=0; act_light_index<Active_LightNodes.active_nodes.size();act_light_index++){
			response_received=false;//initialize the flag for this act_light_index
			LightNode active_node_ref=Active_LightNodes.active_nodes.elementAt(act_light_index);

			if (!active_node_ref.slave_address_str.equals(this.slave_address_str)){
				//The logic here seems like more that would be necessary at first to handle a single click of user input, but there are
				//some interesting permutations that can happen when the user is allowed to manually add nodes and set addresses, shortcuts,
				//and the like,  this particular if statement helps to limit the processing to active light nodes whose address matches
				//the address of the light node who originally invoked the request
				continue;
			}

			//we assume the requested command gets through for the state of the lightnode
			doCorrectBlink(requested_color, active_node_ref);

			//Now we process the ack's and notify accordingly
			for (int rec_pool_index=0; rec_pool_index<t_final_pool.received_pool.size();rec_pool_index++){
				//Iterate through all of the entries in the received pool
				if (base.LightManager.COMM_DEBUG)System.out.println("LightNode:  comparing received_from_address "+
						t_final_pool.received_pool.elementAt(rec_pool_index).received_from_address 
						+" to "+"active_node_refs slave address "+ active_node_ref.slave_address_str);

				if (t_final_pool.received_pool.elementAt(rec_pool_index).received_from_address.equals(active_node_ref.slave_address_str)){
					//if the addresses match, we got a response for the active slave address
					//set the flag to indicate we received a response
					response_received=true;

					//Check if it was ACKED
					if (t_final_pool.received_pool.elementAt(rec_pool_index).acked==1){
						//if a response's received from address matches the slave address of this node AND
						//it received an ACK, change its color
						//doCorrectBlink(requested_color, active_node_ref);
						this.setACKfield(POSITIVE_RESPONSE);
					}

					//Check if it was NACKED						
					if (t_final_pool.received_pool.elementAt(rec_pool_index).acked==0){
						//if a response's received from address matches the slave address of this node AND
						//it received a NACK, color it darkGray
						//doCorrectBlink(Color.darkGray, active_node_ref);
						this.setACKfield(NEGATIVE_RESPONSE);
					}
				}//end if the addresses match
			} //end for rec_pool_index loop
			if (!response_received){
				//At this point, we've either found a matching address in the received_message_pool or iterated through
				//all of the addresses in received_message_pool unsuccessfully;
				//if we were unsuccessful, we need to indicate that we didn't get anything back
				//we are going to color the light node the same as if we received as NACK
				if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: we didn't get any responses so we are going to default to the error state");
				//doCorrectBlink(Color.darkGray, active_node_ref);
				this.setACKfield(NEGATIVE_RESPONSE);
			}


		}//end for act_light_index loop

		bf.requestFocusBackToBackground();
		bf.setCursorWait(false);
		if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: updateLightNode: done processing");
	}//end method updateLightNode
*/
	public void setACKfield(int state){
		switch (state){
		case PROCESSING:  
			acknowledgement_field.setText(" CMD SENT");
			acknowledgement_field.setFont(new Font("couriernew", Font.PLAIN, 10));
			acknowledgement_field.setBackground(Color.yellow);
			break;
		case POSITIVE_RESPONSE:  
			acknowledgement_field.setText(" RESPONSE");
			acknowledgement_field.setFont(new Font("couriernew", Font.PLAIN, 10));
			acknowledgement_field.setBackground(Color.GREEN);
			break;
		case NEGATIVE_RESPONSE:
			acknowledgement_field.setText(" RESPONSE");
			acknowledgement_field.setFont(new Font("couriernew", Font.PLAIN, 10));
			acknowledgement_field.setBackground(Color.RED);
			break;
		case INITIAL_VALUE: 
			acknowledgement_field.setText("  STATUS");
			acknowledgement_field.setFont(new Font("couriernew", Font.PLAIN, 10));
			acknowledgement_field.setBackground(Color.lightGray);
			break;
		}

	}
	/**This method determines whether or not to cause the light node to blink based on the
	 * requested_color passed in as a parameter.  It invokes the blinking sub-routines when
	 * the requested color is Yellow.
	 * 
	 * @param requested_color
	 * @param active_node_ref
	 */
	private void doCorrectBlink(Color requested_color, LightNode active_node_ref){
		if (requested_color.equals(Color.YELLOW)){
			//only blink if Color is YELLOW
			active_node_ref.blinkLight(requested_color, true);
		}else{
			active_node_ref.blinkLight(requested_color, false);
		}
	}
		
	
	public void blinkLight(Color light_color, Boolean active){
		if (active){
			//user has requested to start a light blinking
			if ((blink_job !=null)&&(!blink_job.isCancelled())&&(!blink_job.isDone())){
				if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: light was  blinking, ending the existing blinking job\n" +
						"before we start the next one.");
				blink_job.cancel(true);
			}
			if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: starting a blink job with color "+light_color);
			blink_job=new BlinkerJob(5,light_color,lightButton);
			blink_job.execute();
		}else{
			//user has requested to stop a light blinking
			if ((blink_job ==null)||(blink_job.isCancelled())||(blink_job.isDone())){
				//if the light node wasn't blinking already, just set the control color and the display color
				if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: done blinking, blink job was null, setting to "+light_color);
				lightButton.setBackground(light_color);//loop has ended reset to the static color set in the blinkLight method
				lightButton_control_color=light_color; //The control color is the authoritative source for the state of any light node
			}
			if (blink_job!=null){
				if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: done blinking, blink job was not null, setting to "+light_color);
				//if the light node was already blinking set the static_color indicator and stop the existing blinking job
				blink_job.static_color=light_color;
				blink_job.cancel(true);
			}
		}
 	}//end method blinkLight

	class BlinkerJob extends SwingWorker<Void,Void>{
		Color blinking_color;
		Color static_color=Color.white;//just an initialization, the color is set in the blinkLight method
		long delay;
		MyJButton local_lightButton;
		
		public BlinkerJob(int sec, Color blinking_color,MyJButton t_lightButton){
			delay = 100 * sec;
			this.blinking_color = blinking_color;
			local_lightButton=t_lightButton;
		}

		public Void doInBackground(){
			lightButton_control_color=blinking_color; //The control color is the authoritative source for the state of any light node
			while (!isCancelled()){
				//blink until the user comes back in the blinkLight method and says to cancel this thread
				try {
					local_lightButton.setBackground(blinking_color);
					Thread.sleep(delay);
					local_lightButton.setBackground(Color.white);
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
			}
			return null;
		}
		
		public void done(){
			//The thread is now finished
			//so set the background to whatever the user has specified 
			if (base.LightManager.COMM_DEBUG)System.out.println("LightNode: done blinking, setting to "+static_color);
			local_lightButton.setBackground(static_color);//loop has ended reset to the static color set in the blinkLight method
			lightButton_control_color=static_color; //The control color is the authoritative source for the state of any light node
		}
	}
	
	
	private void notifyUser(){
		JOptionPane.showMessageDialog(null, "Can't communicate with light node.", "Warning",
				JOptionPane.WARNING_MESSAGE,null);
	}
	
	public void setUpPopup() {
		JMenuItem delete_row= new JMenuItem("Delete Light Tower");
		delete_row.setActionCommand("delete tower");
		delete_row.addActionListener(this);

		JMenuItem rename_row= new JMenuItem("Rename Light Tower");
		rename_row.setActionCommand("rename tower");
		rename_row.addActionListener(this);

		JMenuItem shortcut_row= new JMenuItem("Set Shortcut");
		shortcut_row.setActionCommand("set shortcut");
		shortcut_row.addActionListener(this);

		JMenuItem address_row= new JMenuItem("Set Address");
		address_row.setActionCommand("set address");
		address_row.addActionListener(this);
		
		popup.add(delete_row);
		popup.add(rename_row);
		popup.add(shortcut_row);
		popup.add(address_row);
	}//end setUp_Table_Popup

	/** Convenience routine: Show a standard-form error dialog */
	private void errDialog(String message) {
		JOptionPane.showMessageDialog(null, message,
				"Error", JOptionPane.ERROR_MESSAGE);
		return;
	}
	
	private JPanel initializeJPanel(String title) {
		JPanel t_panel=new JPanel();
		t_panel.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(0,0,0,0)));
		t_panel.setLayout(new GridBagLayout());
		return t_panel;
	}

	public void enableComponents() {
		//don't add a mouse motion listener if the node is anchored already
		if (!anchor_box.isSelected())node_panel.addMouseMotionListener(mymouseadapter);
		node_panel.addMouseListener(mymouseadapter);
		anchor_box.addItemListener(this);
	}	

	class MyJButton extends JButton	{
		public MyJButton(String label) {
			super(label);
			setBackground(Color.lightGray);
			lightButton_control_color=Color.lightGray;
			// These statements enlarge the button so that it 
			// becomes a circle rather than an oval.
			Dimension size=new Dimension(50,50);
			size.width = size.height = Math.max(size.width, size.height);
			setPreferredSize(size);

			// This call causes the JButton not to paint the background.
			// This allows us to paint a round background.
			setContentAreaFilled(false);
		}

		// Paint the round background and label.
		protected void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillOval(0, 0, getSize().width-1, getSize().height-1);
			// This call will paint the label and the focus rectangle.
			super.paintComponent(g);
		}

		// Paint the border of the button using a simple stroke.
		protected void paintBorder(Graphics g) {
			g.setColor(getForeground());
			g.drawOval(0, 0, getSize().width-1, getSize().height-1);
		}
	} // end class MyJButton

	class MyMouseAdapter extends MouseInputAdapter{
		LightNode moving_node;
		public void mousePressed(MouseEvent e) {
			//if it is a left-click
			if (e.getButton()==MouseEvent.BUTTON1){
				moving_node=Active_LightNodes.getLightNodeAtMouseEvent(e);
			}
			//if it is a right-click
			if (e.getButton()>MouseEvent.BUTTON1) {
				initial_mouse_event=e;
				popup.show(e.getComponent(),e.getX(), e.getY());
			}
		}

		public void mouseDragged(MouseEvent e) {
			MouseEvent adjusted_e=SwingUtilities.convertMouseEvent((JComponent)e.getSource(), e, moving_node.background_img_panel);
			moving_node.node_panel.setLocation(adjusted_e.getX(),adjusted_e.getY());
			if(LightManager.DEBUG2)System.out.println("LightNode: getting loc "+moving_node.node_panel.getLocation());
		}
	}//end class MyMouseAdapter

	class LightButtonActionAdapter extends MouseInputAdapter{
		public void mousePressed(MouseEvent e) {
			//if it is a left-click
			if (e.getButton()==MouseEvent.BUTTON1){
				lightButtonAction(null);
			}
		}
	}//end class LightButtonActionAdapter
	
	public void actionPerformed(ActionEvent e) {
		if ((e.getActionCommand()).equals("delete tower")) {
			Active_LightNodes.removeLightNode(initial_mouse_event);
		}	
		if ((e.getActionCommand()).equals("rename tower")) {
			LightNode ln=Active_LightNodes.getLightNodeAtMouseEvent(initial_mouse_event);
			String title=getNodeTitle();
			ln.node_panel.setBorder( BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(title),
					BorderFactory.createEmptyBorder(0,0,0,0)));
			node_panel.setSize(node_panel.getPreferredSize());//reset the size to best accommodate the length of the title
			//the light node may get distorted otherwise
		}		
		if ((e.getActionCommand()).equals("set shortcut")) {
			//Allow the user to change the shortcuts and slave address of this node
			adjustShortcut();
		}
		if ((e.getActionCommand()).equals("set address")) {
			//Allow the user to change the shortcuts and slave address of this node
			adjustAddress();
		}

	}

	/**This method is invoked when the user is toggling the Anchored checkbox
	 * when it is enabled, and the node is locked, the mousemotionlistener is removed
	 * when it is disabled (and therefore, not anchored), the mousemotionlistner is added back in
	 * 
	 */
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
        	node_panel.removeMouseMotionListener(mymouseadapter);
        }
        if (e.getStateChange() == ItemEvent.DESELECTED) {
        	node_panel.addMouseMotionListener(mymouseadapter);
        }
	}

	private void adjustShortcut(){
        bf.enableBackgroundFrameKeyListener(false);
		new CustomDialogAsInternalFrame(local_desktop,bf,this,"node shortcut character");
		bf.enableBackgroundFrameKeyListener(true);
    }
	private void adjustAddress(){
		bf.enableBackgroundFrameKeyListener(false);
		new CustomDialogAsInternalFrame(local_desktop,bf, this,"node address");
        bf.enableBackgroundFrameKeyListener(true);
	}
		
	
}
