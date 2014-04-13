package db_info;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import modem_comms.ModemActions;

public class DB_UserCredentials
implements ActionListener, InternalFrameListener, FocusListener {
	DB_Connection local_db_connection;
	JDesktopPane local_desktop;   
	ModemActions theModel;
	public JInternalFrame local_frame;	
	JPanel Main_Panel;
	GridBagConstraints class_c;

	JComboBox portsComboBox;
	JTextField hostname;
	JTextField username;
	JPasswordField password;
	JButton okButton;
	JButton cancelButton;	
	UserFields user_fields;

	public DB_UserCredentials(DB_Connection db_connection,ModemActions theModel,JDesktopPane desktop) {
		local_db_connection=db_connection;
		local_desktop=desktop;
		this.theModel=theModel;
		initializeClassVariables();
		doDialog(Main_Panel);
		local_frame.add(Main_Panel);
		local_frame.pack();
		local_frame.setVisible(true);
		local_frame.getRootPane().setDefaultButton(okButton);//make the okButton the default button

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = local_frame.getSize();
		local_frame.setLocation((int)screenSize.width/2 - frameSize.width/2,
				(int)screenSize.height/2 - frameSize.height/2);
		local_frame.getRootPane().setDefaultButton( okButton );
		local_desktop.add(local_frame);
	}

	private void initializeClassVariables() {
		local_frame=new JInternalFrame("",true,true,true,true);
		local_frame.addInternalFrameListener(this);
		Main_Panel = initializeJPanel("Credentials");
		class_c = new GridBagConstraints();
		class_c.weightx = 1.0;

		class_c.anchor=GridBagConstraints.NORTHWEST;
		class_c.gridwidth=GridBagConstraints.REMAINDER;
		
		portsComboBox=new JComboBox();
		hostname = new JTextField(getHost(),20);
		hostname.addFocusListener(this);
		username = new JTextField(getUsr(),20);
		password = new JPasswordField(getPasswd(),20);
		user_fields = new UserFields(null,null, null);
	}
	private String getHost(){
		ResourceBundle lables = ResourceBundle.getBundle("Connection_Props");
		String result = lables.getString("DB_HOST");
		return result;
	}
	private String getUsr(){
		ResourceBundle lables = ResourceBundle.getBundle("Connection_Props");
		String result = lables.getString("DB_USR");
		return result;
	}
	private String getPasswd(){
		ResourceBundle lables = ResourceBundle.getBundle("Connection_Props");
		String result = lables.getString("DB_PASSWD");
		return result;
	}
	
	public JPanel initializeJPanel(String title) {
		JPanel t_panel=new JPanel();
		t_panel.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(0,0,0,0)));
		t_panel.setLayout(new GridBagLayout());
		return t_panel;
	}

	class UserFields {
		public String the_hostname;
		public String the_username;
		public String the_password;

		public UserFields(String h, String u, String p) {
			the_username = h;
			the_username = u;
			the_password = p;
		}
	}

	public void doDialog(JPanel parent_panel) {
		JPanel p1=initializeJPanel("");
		JPanel p2=initializeJPanel("");
		addComboBox(p1, "Port:",portsComboBox,class_c,"end row");
		addTextField(p1," Hostname:",hostname,class_c,"end row");
		addTextField(p1,"User name:",username,class_c,"end row");
		addPasswordField(p1,"  Password:",password,class_c,"end row");
		class_c.anchor=GridBagConstraints.SOUTHWEST;
		
		class_c.anchor=GridBagConstraints.CENTER;
		okButton = addButton(p2, "Ok");
		cancelButton = addButton(p2, "Cancel");

		class_c.gridwidth=GridBagConstraints.REMAINDER;
		parent_panel.add(p1,class_c);
		parent_panel.add(p2,class_c);
		okButton.setSelected(true);
	}


	private JButton addButton(Container c, String name) {
		JButton button = new JButton(name);
		button.addActionListener(this);
		button.addFocusListener(this);
		c.add(button);
		return button;
	}

	public void addComboBox(JPanel t_panel, String t_label, JComboBox t_combobox,
			GridBagConstraints c, String end_row) {
		JPanel local_panel=new JPanel();
		c.anchor=GridBagConstraints.NORTHWEST;
		c.insets=new Insets(10,35,10,10);
		local_panel.add(new JLabel(t_label),c);

		c.insets=new Insets(10,35,10,35);
		if (end_row.equals("end row")) {
			c.gridwidth=GridBagConstraints.REMAINDER;
		}

		local_panel.add(t_combobox, c);
		Vector<String> port_list=theModel.populateComboBox();
		for (int i=0;i<port_list.size();i++){
			portsComboBox.addItem(port_list.elementAt(i));
		}
		portsComboBox.setSelectedIndex(port_list.size()-1); //set the selection in the combobox
		
		t_panel.add(local_panel,c);
		c.gridwidth=1; //reset gridwidth
	}  	
	
	
	public void addTextField(JPanel t_panel, String t_label, JTextField t_field,
			GridBagConstraints c, String end_row) {
		JPanel local_panel=new JPanel();
		c.insets=new Insets(10,35,10,10);
		local_panel.add(new JLabel(t_label),c);

		c.insets=new Insets(10,0,10,35);
		if (end_row.equals("end row")) {
			c.gridwidth=GridBagConstraints.REMAINDER;
		}

		local_panel.add(t_field, c);
		t_panel.add(local_panel,c);
		c.gridwidth=1; //reset gridwidth
	}   

	public void addPasswordField(JPanel t_panel, String t_label, JPasswordField t_area,
			GridBagConstraints c, String end_row) {
		JPanel local_panel=new JPanel();

		c.insets=new Insets(10,35,10,10);
		local_panel.add(new JLabel(t_label),c);

		c.insets=new Insets(10,0,10,35);
		if (end_row.equals("end row")) {
			c.gridwidth=GridBagConstraints.REMAINDER;
		}

		local_panel.add(t_area, c);
		t_panel.add(local_panel,c);
		c.gridwidth=1; //reset gridwidth
	}   

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == okButton) {
			user_fields.the_hostname=hostname.getText();
			user_fields.the_username = username.getText();
			user_fields.the_password = new String(password.getPassword());		
			local_frame.setVisible(false);
			local_frame.dispose();
			if(local_db_connection.tryConnect()){//try to connect with these credentials
				theModel.connectOrDisconnect(portsComboBox.getSelectedItem());//only connect to the COM port if we successfully
				//connected to the DB
			}
			}	
	}

	public void internalFrameClosing(InternalFrameEvent e) {
	}

	public void internalFrameClosed(InternalFrameEvent e) {
	}

	public void internalFrameOpened(InternalFrameEvent e) {
	}

	public void internalFrameIconified(InternalFrameEvent e) {
	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	public void internalFrameActivated(InternalFrameEvent e) {
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
	}

	public void mouseDragged(MouseEvent arg0) {
	}

	public void mouseMoved(MouseEvent e) {
	}




}