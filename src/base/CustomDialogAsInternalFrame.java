package base;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;


public	class CustomDialogAsInternalFrame extends JInternalFrame 
                                          implements ActionListener, PropertyChangeListener,
                                          InternalFrameListener{
	JDesktopPane local_desktop;
	JInternalFrame local_frame;
	BackgroundFrame bf;
	
	ComponentListener myComponentListener;
	String prompt;
	LightNode node_ref;
	
	private JOptionPane optionPane;
	private String typedText = null;
	private JTextField textField;

	private String enterString = "Enter";
	private String cancelString = "Cancel";

	/** Creates the reusable dialog. */
	public CustomDialogAsInternalFrame(JDesktopPane desktop,BackgroundFrame bf,LightNode node_ref, String prompt) {
		this.local_desktop=desktop;
		this.bf=bf;
		this.node_ref=node_ref;
		this.prompt=prompt;
		initializeClassVariables();

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		local_frame.add(optionPane);
		local_frame.addComponentListener(new myComponentListener());
		local_frame.pack();
		local_frame.setVisible(true);
		local_desktop.add(local_frame);
		local_desktop.getDesktopManager().activateFrame(local_frame);//make this the frame at the front
		textField.requestFocusInWindow();
	}

	private void initializeClassVariables(){
		local_frame=new JInternalFrame("",true,true,true,true);
		setupTextFields();
		setupOptionPane();
	}

    private void setupTextFields() {
		textField = new JTextField(5);
		if (prompt.contains("shortcut"))textField.setText(node_ref.keyboard_shortcut+""); //shortcuts can only be one character
		if (prompt.contains("address"))textField.setText(node_ref.slave_address_str); //addresses must be two characters
		
		textField.selectAll();//highlight the old shortcut or address
		textField.addActionListener(this);
    }	
	
    private void setupOptionPane() {
		//Create an array of the text and components to be displayed.
    	Object[] array = {"Enter "+prompt+":", textField};

		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = {enterString, cancelString};

		//Create the JOptionPane.
		optionPane = new JOptionPane(array,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				options,
				options[0]);

		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
    }
    
	public void actionPerformed(ActionEvent e) {
		optionPane.setValue(enterString);
	}

	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		typedText=textField.getText();
		
		if (local_frame.isVisible()
				&& (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) ||
						JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}

			//Reset the JOptionPane's value.
			//If you don't do this, then if the user
			//presses the same button next time, no
			//property change event will be fired.
			optionPane.setValue(
					JOptionPane.UNINITIALIZED_VALUE);

			if (value.equals(enterString)) {
				//user clicked the "Enter" button
				if (checkValues()) {
					//we're done; clear and dismiss the dialog
					changeAttributeAndHidePane();
				} else {
					//text was invalid
					handleInvalidInput();
				}
			} else { 
				//user closed dialog or clicked the "Cancel" button
				typedText = null;//nullify any typedText since it has not been validated
				changeAttributeAndHidePane();
			}
		}
	}
	
	/**Check the string t_typedText that is passed in as a parameter
	 * It simply checks the length of the string against the permitted
	 * input_length value
	 * 
	 * @param t_typedText
	 * @return boolean as to whether the string is valid
	 */
	private Boolean checkValues(){
		//evaluate the length of the text typed against the permitted length
		
		if (prompt.contains("node shortcut")){
			if (typedText.length()==1){
				//for a node shortcut, the length has to be 1
				return true;
			}
		}
		if (prompt.contains("node address")){
			if (typedText.length()==1){
				//if the length of the address is 1, we need to
				//prepend a zero
				typedText="0"+typedText;
				return tryToGetIntegerValue(typedText);
			}
			if (typedText.length()==2){
				return tryToGetIntegerValue(typedText);
			}
		}
		return false;
	}
	
	/**Attempts to get the integer value of the string; if it is successful
	 * the method returns true, if it is unsuccessful, it returns false
	 * 
	 * @param candidate_string
	 * @return boolean result of trying to get the Integer value
	 */
	private Boolean tryToGetIntegerValue(String candidate_string){
		try{
			//see if we can make an integer value out of it
			Integer.valueOf(candidate_string);
			return true;
		}catch(NumberFormatException nf){
			return false;
		}
		
	}
	
	
	/**Handle the invalid input by popping up an error window
	 * and setting the typedText value to null
	 */
	private void handleInvalidInput(){
		//text was invalid
		textField.selectAll();
		JOptionPane.showMessageDialog(
				CustomDialogAsInternalFrame.this,
				"Sorry, \"" + typedText + "\" "
				+ "isn't a valid "+prompt+".\n"
				+ "Please enter a valid "+prompt+".",
				"Try again",
				JOptionPane.ERROR_MESSAGE);
		typedText = null;
		textField.requestFocusInWindow();
	}
	
	/**Sets the shortcut or slave address depending on how the class was invoked
	  */
	public void changeAttributeAndHidePane() {
		if (typedText != null){
			//non-null typedText means we have a new value to be set
			if (prompt.contains("node shortcut")){
				node_ref.keyboard_shortcut=typedText; //shortcuts can only be one character
				node_ref.keyboard_shortcut_display.setText(node_ref.keyboard_shortcut+"");
			}
			if (prompt.contains("node address")){
				node_ref.slave_address_str=typedText;
				//System.out.println("CustomDialogAsInternalFrame: slave address has been set to: "+typedText );
				String computed_shorcut=Active_LightNodes.determineKeyBoardShortcut(null, typedText);
				node_ref.keyboard_shortcut=computed_shorcut;
				node_ref.keyboard_shortcut_display.setText(computed_shorcut+"");
			}
		}
		
		local_frame.setVisible(false);
		local_desktop.remove(local_frame);
		local_desktop.repaint();
		bf.singleAutoClickToActiveFrame();//this makes sure the background is the active frame so keyboard input is responded to in as the user expects
	}
	
	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void internalFrameClosed(InternalFrameEvent e) {
		/*
		 * Instead of directly closing the window,
		 * we're going to change the JOptionPane's
		 * value property.
		 */
		optionPane.setValue(new Integer(
				JOptionPane.CLOSED_OPTION));
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
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

	class myComponentListener extends ComponentAdapter{
		public void componentShown(ComponentEvent e) {
			//Ensure the JTextField gets the first focus.
			if (e.getComponent().getClass().getName().contains("JTextField")){
				e.getComponent().requestFocusInWindow();	
			}
		}
	}

}

