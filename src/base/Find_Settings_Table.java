package base;

import javax.swing.event.*;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import javax.swing.*;

import utilities.MyTableModel;

import modem_comms.ModemActions;

import db_info.DB_Calls;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Vector;
import java.sql.*;

public class Find_Settings_Table implements ActionListener,
ListSelectionListener, PopupMenuListener, TableModelListener {
	JInternalFrame p_frame;
	static JDesktopPane desktop;
	ModemActions theModel;
	JTable class_table;
	MyTableModel model;

	JPanel search_panel;

	JPanel number_area;
	JTextField field_for_numbers;
	JPanel number_buttons_panel;
	JPanel results_panel;
	JPanel commands_panel;

	GridBagConstraints class_c;

	JButton new_selection;
	JButton open_selection;
	JButton clone_selection;
	JButton delete_selection;

	Integer number_of_results_buttons;//the total number of results buttons to handle the query
	static Integer number_results_buttons_per_panel=5;//the number of results buttons per results panel
	Integer current_results_button;   //the active results button
	Integer current_results_panel;    //the active results panel

	public Find_Settings_Table(JInternalFrame parent_internal_frame,
			JDesktopPane parent_desktop,ModemActions theModel, JPanel parent_panel,
			GridBagConstraints parent_c) {
		p_frame = parent_internal_frame;
		desktop = parent_desktop;
		this.theModel=theModel;
		initializeClassVariables();
		setupResultsArea();
		setUpSelectionButton();
		getTracks(current_results_button);// retrieve all data to start off with
		parent_panel.add(search_panel, parent_c);
		disablePanelComponents();
	}// end constructor

	private void initializeClassVariables() {
		search_panel = initializeJPanel("");
		results_panel = initializeJPanel("");
		commands_panel = initializeJPanel("Actions:");
		number_area=initializeJPanel("");
		field_for_numbers=new JTextField(10);
		field_for_numbers.setEditable(false);
		number_buttons_panel=initializeJPanel("");

		class_c = new GridBagConstraints();
		class_c.anchor = GridBagConstraints.CENTER;
		class_c.weightx = 1.0;
		class_c.gridwidth = GridBagConstraints.REMAINDER;

		new_selection= new JButton("New Track");
		open_selection = new JButton("Open Track");
		clone_selection = new JButton("Clone Track");
		delete_selection = new JButton("Delete Track");

		current_results_button=1; //initialize the current results page to 1
		current_results_panel=0; //initialize the current results panel to 0
		number_of_results_buttons=1;	
	}

	public JPanel initializeJPanel(String title) {
		JPanel t_panel = new JPanel();
		t_panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title), 
				BorderFactory.createEmptyBorder(0,0,0,0)));
		t_panel.setLayout(new GridBagLayout());
		return t_panel;
	}


	public void setupResultsArea() {
		model=new MyTableModel();//create an instance of our special model called MyTableModel
		class_table = new JTable(model);
		
		//ListSelection is used so the rows in the JTable can be selected and events can be fired from
		ListSelectionModel selection_model = class_table.getSelectionModel();
		selection_model.addListSelectionListener(this);

		JScrollPane areaScrollPane = new JScrollPane(class_table);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(800, 250));
		areaScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Results"), 
						BorderFactory.createEmptyBorder(0, 0, 0, 0)), 
						areaScrollPane.getBorder()));

		createNumbersArea();
		results_panel.add(number_area,class_c);
		results_panel.add(areaScrollPane,class_c);
		search_panel.add(results_panel,class_c);
	}
	private void createNumbersArea() {
		GridBagConstraints c=new GridBagConstraints();
		c.insets=new Insets(0,0,5,0);
		number_area.add(new JLabel("Total Matching Settings:"),c);
		c.insets=new Insets(0,0,0,0);
		c.gridwidth=1;
		number_area.add(field_for_numbers, c);
		number_area.add(number_buttons_panel,c);
	}    

	private void clearTable() {
		Vector<String> data_vect = new Vector<String>();
		Vector<String> col_id_vect = new Vector<String>();

		model.setDataVector(data_vect, col_id_vect);
		model.fireTableDataChanged();
	}

	public void getTracks(Integer page) {
		ResultSet rs = null;
		clearTable();
		getNumberTracks();//get the number of tracks and build the results button panel

		rs = DB_Calls.findTracks(page);

		try {
			if (rs.first()) {
				Vector<String> col_names = new Vector<String>();
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 0; i < rsmd.getColumnCount(); i++) {
					//System.out.println("column "+(i+1)+" is |"+rsmd.getColumnLabel(i+1)+"|");
					col_names.add(rsmd.getColumnLabel(i + 1));// get to the list of the columns to request
					model.addColumn(rsmd.getColumnLabel(i + 1));// add to the list of display columns
				}

				do {
					// Get the data from the resultSet
					Vector<String> data = new Vector<String>();
					for (int i = 0; i < col_names.size(); i++) {
						data.add(rs.getString(col_names.elementAt(i)));
					}
					model.addRow(data); // add this result to the table
				} while (rs.next());

				// resize the columns each time we get new results
				for (int i = 0; i < class_table.getColumnCount(); i++) {
					TableColumn this_col = class_table.getColumnModel().getColumn(i);
					this_col.sizeWidthToFit();
				}
				model.addTableModelListener(this);//Listen for if the user changes the editable cells
			}
		} catch (SQLException e) {
			System.err.println("Find_Settings_Table: Problem retrieving data.");
			e.printStackTrace();
		}
	}

	private int determineNumberResultsButtons(Integer total_number, Integer records_per_page){
		Integer t_number_of_results_buttons=total_number / records_per_page;
		if(total_number % records_per_page >0){
			t_number_of_results_buttons++;//if there is a partial page, add one more page
		}
		return t_number_of_results_buttons;
	}

	private void rebuildResultsPanel(){
		//remove any existing results buttons first
		Component[] panel_components=number_buttons_panel.getComponents();
		int max=panel_components.length;
		for(int i=0;i<max;i++) {
			number_buttons_panel.remove(panel_components[i]);
		}
		p_frame.updateUI();

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1.0;
		c.insets = new Insets(0,0,5,5);
		JLabel label=new JLabel("Result Pages ");
		number_buttons_panel.add(label,c);

		//if the current results panel is non-zero, then put in a previous button
		if (current_results_panel>0){
			JButton button=new JButton("<<");
			button.addActionListener(this);
			button.setActionCommand("prev set");
			number_buttons_panel.add(button,c);
		}
		int base=current_results_panel*number_results_buttons_per_panel;
		for (int i=base+1; i<=number_of_results_buttons; i++){
			if (i<=(base+number_results_buttons_per_panel)){
				JButton button=new JButton(i+"");
				button.addActionListener(this);
				button.setActionCommand(i+"");
				number_buttons_panel.add(button,c);
			}
			//if we are over the number of buttons for the panel, then put in a next button
			if (i>(base+number_results_buttons_per_panel)){
				JButton button=new JButton(">>");
				button.addActionListener(this);
				button.setActionCommand("next set");
				number_buttons_panel.add(button,c);
				break;//only go to the limit per panel
			}
		}
		p_frame.updateUI();			
	}

	public void getNumberTracks() {
		ResultSet rs_numbers = null;
		rs_numbers = DB_Calls.getNumberOfTracks();
		try {

			if (rs_numbers.first()) {
				Integer total_number=rs_numbers.getInt("total");
				Integer records_per_page=rs_numbers.getInt("records per page");
				field_for_numbers.setText("     "+total_number);
				number_of_results_buttons=determineNumberResultsButtons(total_number,records_per_page);
				rebuildResultsPanel();
			}
		} catch (SQLException e) {
			System.err.println("Find_Settings_Table: Problem retrieving data.");
			e.printStackTrace();
		}
	}

	public void setUpSelectionButton() {
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.insets = new Insets(10, 35, 10, 35);

		new_selection.setActionCommand("New Track");
		new_selection.addActionListener(this);
		
		open_selection.setActionCommand("Open Track");
		open_selection.addActionListener(this);
		open_selection.setEnabled(false);// Disable this button except when
		// one row is selected

		clone_selection.setActionCommand("Clone Track");
		clone_selection.addActionListener(this);
		clone_selection.setEnabled(false);// Disable this button except when
		// one row is selected

		delete_selection.setActionCommand("Delete Tracks");
		delete_selection.addActionListener(this);
		delete_selection.setEnabled(false);// Disable this button except when
		// one or more rows are selected

		commands_panel.add(new_selection, c);
		commands_panel.add(open_selection, c);
		commands_panel.add(clone_selection, c);
		commands_panel.add(delete_selection, c);

		search_panel.add(commands_panel, class_c);
	}

	public void openSelectedSettings() {
		// get the rows the user has selected
		int[] selected_rows = class_table.getSelectedRows();

		// only clone if only one setting is selected
		// if more than one is selected just return out of this method
		if (selected_rows.length != 1)
			return;		

		// for each select row, get the setting id which is in column 0
		// get the work order id which is in the 0th column
		Integer track_id = new Integer((String) model.getValueAt(
				selected_rows[0], 0));

		p_frame.setVisible(false);

		BackgroundFrame bf=new BackgroundFrame(desktop,theModel,track_id);
		Active_Backgrounds.addBackground(bf);
		Active_LightNodes.setBackgroundVariables(bf, track_id);
		
		// to minimize problems, remove the selection from the user's table
		// after they have opened a window with the
		// setting
		for (int i = selected_rows.length - 1; i >= 0; i--) {
			model.removeRow(selected_rows[i]);
		}
		p_frame.dispose();// close the find_settings frame
	}

	public void deleteSelectedSettings() {
		Boolean delete_all=false;
		String response=null;
		
		// get the rows the user has selected
		int[] selected_rows = class_table.getSelectedRows();


		// for each select row, get the setting id which is in column 0
		// open a new window for each setting
		for (int i = 0; i < selected_rows.length; i++) {
			// get the work order id which is in the 0th column
			Integer track_id = new Integer((String) model.getValueAt(
					selected_rows[i], 0));

			if (delete_all){
				//don't prompt the user, just go ahead and delete the work order
				DB_Calls.deleteTrack(track_id);
			}

			//Double check with the user that the want to delete these settings
			if (!delete_all){
				response=verifyWorkOrderDelete(track_id);
				if (response.equals("single yes")){
					//only delete the active work_order in the loop
					DB_Calls.deleteTrack(track_id);	
				}
				if (response.equals("all yes")){
					//delete the active work order in the loop and
					//flip the flag so the user won't by prompted any more
					DB_Calls.deleteTrack(track_id);
					delete_all=true; 
				}
				if (response.equals("cancel")){
					return;
				}
			}
		}		
		
		// to minimize problems, remove the selection from the user's table
		// after they have opened a window with the
		// setting
		for (int i = selected_rows.length - 1; i >= 0; i--) {
			model.removeRow(selected_rows[i]);
		}
	}

    private String verifyWorkOrderDelete(int work_order) {
		String response="cancel";
    	Object usr_options[] = { "Yes","Yes to All","Cancel" };
		Integer usr_response=JOptionPane.showOptionDialog(null, null, "Delete Setting "+work_order,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, usr_options,
				usr_options[0]);
		
		if (usr_response==0){
			response="single yes";
		}
		
		if (usr_response==1){
			response="all yes";		
		}

		if (usr_response==2){
			response="cancel";
		}

		return response;
    } 
	
    
	public void cloneSetting() {
		// get the rows the user has selected
		int[] selected_rows = class_table.getSelectedRows();

		// only clone if only one setting is selected
		// if more than one is selected just return out of this method
		if (selected_rows.length != 1)
			return;

		// for each select row, get the setting id which is in column 0
		// get the work order id which is in the 0th column
		Integer track_id = new Integer((String) model.getValueAt(
				selected_rows[0], 0));
		// System.out.println("Cloning work order "+work_order);
		DB_Calls.cloneTrack(track_id);
		// the zero is a flag indicating
		// we do not want to clone everything
		// customer comments, tech support comments, payment info, and shipping
		// info will not be cloned with this flag turned off
		getTracks(current_results_button);
	}

	public void disablePanelComponents() {
		// make the table cells uneditable
		//class_table.setDefaultEditor(Object.class, null);
	}

	public void valueChanged(ListSelectionEvent e) {
		// This method is invoked twice when a user selects or deselects a row
		// in the table
		// It only allows one setting to be cloned at a time by only enabling
		// the clone button when
		// only one row is selected
		
		if ((class_table.getSelectedRows()).length == 0){
			new_selection.setEnabled(true);
			open_selection.setEnabled(false);
			clone_selection.setEnabled(false);
			delete_selection.setEnabled(false);
		}
		
		if ((class_table.getSelectedRows()).length == 1){
			new_selection.setEnabled(true);
			open_selection.setEnabled(true);
			clone_selection.setEnabled(true);
			delete_selection.setEnabled(true);
		}

		if ((class_table.getSelectedRows()).length > 1){
			new_selection.setEnabled(true);
			open_selection.setEnabled(false);
			clone_selection.setEnabled(false);
			delete_selection.setEnabled(true);
		}
	}

	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Get the Work Orders based on the criteria the user has selected every
		// time one of the criteria
		// combo_boxes popup menu is closed after being opened
		current_results_button=1; //user has selected a new criteria so go back to the first selection
		current_results_panel=0; //user has selected a new criteria so go back to the first selection
		number_of_results_buttons=1;//user has selected a new criteria so go back to the first selection	
		getTracks(current_results_button);
	}

	public void actionPerformed(ActionEvent e) {
		The_Desktop.setCursorWait(true);
		model.removeTableModelListener(this);

		String action_command=e.getActionCommand();

		try{
			Integer int_result=Integer.parseInt(action_command);
			if (int_result>0) {
				current_results_button=int_result;//set the current selected page
				getTracks(current_results_button);
			}
		}catch (NumberFormatException exc) {
		}

		if (action_command.equals("prev set")) {
			current_results_panel--;
			rebuildResultsPanel();
		}
		if (action_command.equals("next set")) {
			current_results_panel++;
			rebuildResultsPanel();
		}

		if (action_command.equals("New Track")) {
			p_frame.dispose();
			The_Desktop.newTrack();
		}		
		if (action_command.equals("Open Track")) {
			openSelectedSettings();
		}
		if (action_command.equals("Delete Tracks")) {
			deleteSelectedSettings();
		}
		if (action_command.equals("Clone Track")) {
			cloneSetting();
		}
		model.addTableModelListener(this);
		The_Desktop.setCursorWait(false);
	}//end method actionPerformed

	
	
	public void tableChanged(TableModelEvent e) {
		//For the correct balance between user interaction and db interaction,
		//this event handling was embedded in the MyTableModel class that
		//extends the DefaultTableModel
	}

}//end class Find_Settings_Table

