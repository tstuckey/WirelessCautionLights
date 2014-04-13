package utilities;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import modem_comms.ModemActions;

import base.Active_Backgrounds;
import base.Active_LightNodes;
import base.BackgroundFrame;

public class FileChooser{
	JDesktopPane local_desktop;
	ModemActions theModel;
	GridBagConstraints class_c;

	JButton openButton, saveButton;
	JFileChooser fc;
	public FileChooser(JDesktopPane desktop, ModemActions theModel) {
		local_desktop = desktop;
		this.theModel=theModel;
		initializeClassVariables();
		int returnVal=fc.showOpenDialog(local_desktop);
		handleFCchoices(returnVal);
	}

	private void initializeClassVariables() {
		class_c = new GridBagConstraints();
		class_c.anchor = GridBagConstraints.CENTER;
		class_c.gridwidth = GridBagConstraints.REMAINDER;
		setUpFileDialog();
	}
	private void setUpFileDialog() {
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
	    //Only permit photos by default
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "Picture Files", "jpg","gif","png");
	    fc.setFileFilter(filter);
	}


	private void handleFCchoices(int returnVal) {
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				//Load the file as the background
				BackgroundFrame bf=new BackgroundFrame(local_desktop,theModel,file);
				Active_Backgrounds.addBackground(bf);
				Active_LightNodes.setBackgroundVariables(bf);
				bf.saveState(false);
			} else {
				//operation canceled by user
			}
	}

}
