package utilities;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import base.LightManager;

import java.awt.*;


/* Used by Setup_Desktop.java. */
public class Help_Frame extends JInternalFrame {
	static final int xOffset = 30, yOffset = 30;
	static int openFrameCount = 0;
	JDesktopPane local_desktop;   	
	JInternalFrame local_frame;
	JScrollPane areaScrollPane;
	JPanel Main_Panel;
	GridBagConstraints class_c;


	public Help_Frame(JDesktopPane desktop) {
		local_desktop=desktop;
		initializeClassVariables();
		loadImage();
		local_frame.add(areaScrollPane);
		local_frame.pack();
		local_frame.setVisible(true);

		local_frame.setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
		local_desktop.add(local_frame);
		openFrameCount++;
	}

	private void initializeClassVariables() {
		local_frame=new JInternalFrame("",true,true,true,true);
		Main_Panel = initializeJPanel("Information");
		class_c = new GridBagConstraints();
		class_c.anchor=GridBagConstraints.CENTER;
		class_c.gridwidth=GridBagConstraints.REMAINDER;


		areaScrollPane=new JScrollPane(Main_Panel);
	}

	public JPanel initializeJPanel(String title) {
		JPanel t_panel=new JPanel();
		t_panel.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title),
				BorderFactory.createEmptyBorder(0,0,0,0)));
		t_panel.setLayout(new GridBagLayout());
		return t_panel;
	}

	private void loadImage(){
		ImageIcon logo_icon = new ImageIcon(LightManager.class.getResource("/img/logo.jpg"));
		JLabel a_label = new JLabel("", logo_icon, SwingConstants.CENTER);
		Main_Panel.add(a_label,class_c);
	}

	public void doPopup(){
		JOptionPane.showMessageDialog(null, "Suspension Notebook v 1.0", "Information",
				JOptionPane.INFORMATION_MESSAGE,null);
	}

}
