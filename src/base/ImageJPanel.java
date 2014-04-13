package base;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ImageJPanel extends JPanel {
	public ImageIcon picture; // image to be displayed
	public byte[] picture_bytes;
	public JDesktopPane local_desktop;
	
	int bottombuttonWidth;
	int bottombuttonHeight;
	int height_buffer=50;
	public ImageJPanel(byte[] imageFile, JDesktopPane desktop, int width, int height)
	{
		local_desktop=desktop;
		this.bottombuttonWidth=width;
		this.bottombuttonHeight=height+height_buffer;
		setLayout(null);//so LightNodes can be added in various places
		//based on x,y coordinates
		try {
			if (imageFile!=null){
				picture = new ImageIcon(imageFile);
				picture_bytes=new byte[imageFile.length];
				picture_bytes=imageFile;
			}
			else{
				JOptionPane.showMessageDialog(null, "No Image in DB", "Warning",
						JOptionPane.WARNING_MESSAGE,null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public ImageJPanel(File imageFileName, JDesktopPane desktop, int width, int height)
	{
		local_desktop=desktop;
		this.bottombuttonWidth=width;
		this.bottombuttonHeight=height+height_buffer;
		setLayout(null);//so LightNodes can be added in various places
		//based on x,y coordinates
		try {
			picture = new ImageIcon(imageFileName.toURI().toURL());
			picture_bytes=convertFileToByte(imageFileName.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public byte[] convertFileToByte(String file){
		byte[] data =null;
		try {
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();
			data = new byte[(int)fc.size()];
			ByteBuffer bb = ByteBuffer.wrap(data);
			fc.read(bb);
		} catch (FileNotFoundException e) {
			System.err.println("ImageJPanel: unable to locate file");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ImageJPanel: problem converting to bytes");
			e.printStackTrace();
		}

		return data;
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent( g );
		if (picture !=null){
			picture.paintIcon(this, g, 0, 0); // display icon
		}
	}

	public Dimension getPreferredSize()
	{
		int final_w, final_h=0;
		int icon_w=picture.getIconWidth();
		int icon_h=picture.getIconHeight();
		
		Rectangle the_bounds=local_desktop.getBounds();
		int desktop_w=the_bounds.width;
		int desktop_h=the_bounds.height;

		//if the icon width is greater than the desktop,
		//then set the preferred size to the desktop width
		//else use the icon width
		if (icon_w+bottombuttonWidth>desktop_w){
			if (LightManager.DEBUG2)
				System.out.println("ImageJpanel: width greater than desktop_w"); 
			final_w=desktop_w-bottombuttonWidth;
			//final_w=100;
		}else{
			final_w=icon_w;
		}
		//if the icon height is greater than the desktop,
		//then set the preferred size to the desktop height
		//else use the icon height
		if (icon_h+bottombuttonHeight>desktop_h){
			if (LightManager.DEBUG2)
				System.out.println("ImageJpanel: height greater than desktop_h"); 
			final_h=desktop_h-bottombuttonHeight;
			//final_h=100;
		}else{
			final_h=icon_h;
		}
		return new Dimension(final_w, final_h);
	}



}
