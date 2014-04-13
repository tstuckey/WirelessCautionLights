package utilities;

import java.net.URL;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.Timer;
import javax.swing.JWindow;

import base.LightManager;
import base.The_Desktop;

public class SplashScreen extends JWindow
{
  private Image  image_;
  private int    x_, y_, width_, height_;

  /**
   * Create a new splash screen object of the specified image.
   * The image file is located and referred to through the deployment, not
   */
  public SplashScreen (String imageFileName)
  {
    super (new Frame());

    try {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      
      URL imageUrl = LightManager.class.getResource(imageFileName);
      image_ = toolkit.getImage (imageUrl);

      MediaTracker mediaTracker = new MediaTracker (this);
      mediaTracker.addImage (image_, 0);
      mediaTracker.waitForID (0);

      width_  = image_.getWidth (this);
      height_ = image_.getHeight (this);

      Dimension screenSize = toolkit.getScreenSize();
      
      x_ = (screenSize.width  - width_)  / 2;
      y_ = (screenSize.height - height_) / 2;
    }
    catch (Exception exception) {
      exception.printStackTrace();
      image_ = null;
    }
  }


  /**
   * Open the splash screen and keep it open for the specified duration
   * or until close() is called explicitly.
   */
  public void open (int nMilliseconds)
  {
    if (image_ == null) return;
    
    Timer timer = new Timer (Integer.MAX_VALUE, new ActionListener() {
        public void actionPerformed (ActionEvent event) {
          ((Timer) event.getSource()).stop();
          close();
        };
      });
    
    timer.setInitialDelay (nMilliseconds);
    timer.start();

    setBounds(x_, y_, width_, height_);
    setVisible(true);
    this.toFront();
  }

  /**
   * Close the splash screen.
   */
  public void close()
  {
    The_Desktop.drawPage();//invoke the GUI start as the splash intro closes
	setVisible (false);
    dispose();
  }
  
  public void paint (Graphics graphics) 
  {
    if (image_ == null) return;
    graphics.drawImage (image_, 0, 0, width_, height_, this);
  }
}
