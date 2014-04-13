package base;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import db_info.DB_Calls;

public class Active_Backgrounds{
	public static Vector<BackgroundFrame> backgrounds;
		public Active_Backgrounds() {
			backgrounds=new Vector<BackgroundFrame>();

	}//end class Active_Work_GUIss

	public static void addBackground(BackgroundFrame background_frame){
		backgrounds.add(background_frame);
	}


	public static void removeBackground(BackgroundFrame background_frame) {
		int i=0;
		while (i<backgrounds.size()){
			BackgroundFrame this_background=backgrounds.elementAt(i);
			if (this_background==background_frame){
				backgrounds.removeElementAt(i);
				break;
			}
			i++;
		}
	}


	public static void disposeActiveBackgrounds(JDesktopPane local_desktop ){
		int i=0;
		while (i<backgrounds.size()){
			backgrounds.elementAt(i).img_panel.picture=null;//this deallocates the existing picture
			local_desktop.remove(backgrounds.elementAt(i));
			local_desktop.getDesktopManager().closeFrame(backgrounds.elementAt(i).local_frame);
			backgrounds.elementAt(i).dispose();
			backgrounds.removeElementAt(i);
			i++;
		}
	}
	
}

