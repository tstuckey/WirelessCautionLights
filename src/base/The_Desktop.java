package base;

import javax.swing.JFrame;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import modem_comms.MasterCounter;
import modem_comms.ModemActions;
import db_info.DB_Connection;
import utilities.FileChooser;
import utilities.Help_Frame;
import java.awt.event.*;
import java.awt.*;

public class The_Desktop extends JFrame implements ActionListener {
    static JDesktopPane desktop;
    public DB_Connection active_db_connection;
    public Find_Settings_Frame find_frame;
    public JMenuBar menuBar;
    
    public static MasterCounter appCounter;
    public static ModemActions theModel=null;

    public The_Desktop() {
    	connectedToDatabase(false);
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);
        
        //Set up the GUI
        desktop = new JDesktopPane(); //a specialized layered pane
        setContentPane(desktop);
        setJMenuBar(createMenuBar());
        
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
		
		//Make the text for the comboxes black even when they are disabled
		UIManager.put("ComboBox.disabledForeground", Color.black);
        
		new Active_Backgrounds();
		new Active_LightNodes();
		appCounter=new MasterCounter();
		
		theModel=new ModemActions(this);
		active_db_connection=new DB_Connection(this,theModel,desktop);
    }

    public void connectedToDatabase(Boolean connected){
       	if (connected){
       		this.setTitle("Light Manager--DB CONNECTED");
       		//findTracks();//invoke the find interface as soon as the user is connected
       	}
    	else this.setTitle("Light Manager--DB DISCONNECTED");
    }

    public void connectedToPort(Boolean com_port_connected){
   		String current_frame_title=this.getTitle();
   		
    	if (com_port_connected){
    		if (current_frame_title.contains("Light Manager--DB CONNECTED")){
    			this.setTitle("Light Manager--DB CONNECTED; COM Port--CONNECTED");
    			}else{
    				this.setTitle("Light Manager--DB DISCONNECTED; COM Port--CONNECTED");
    			}
       	}
    	else {
    		if (current_frame_title.contains("Light Manager--DB DISCONNECTED")){
    			this.setTitle("Light Manager--DB DISCONNECTED; COM Port--CONNECTED");
    			}else{
    				this.setTitle("Light Manager--DB CONNECTED; COM Port--DISCONNECTED");
    			}
    	}
    }    
    
    private void resetModem() {
		Object stringArray2[] = { "Reset Modem Connection","Cancel" };
		Integer clear_result=JOptionPane.showOptionDialog(null, null, "Are You Sure?",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, stringArray2,
				stringArray2[0]);
		
		if (clear_result==0){
			//theModel.bounceConnection();
		}
		
		if (clear_result==1){
		    return;//exit out of this method			
		}
    } 
    
    public void closeIntroFrames(){
    	if ((find_frame!=null)&&(find_frame.isVisible())){
    		find_frame.dispose();
    	}
    	
    	if ((active_db_connection!=null)&&(active_db_connection.the_user_info.local_frame.isVisible())){
    		active_db_connection.the_user_info.local_frame.dispose();
    	}
    	
    }  
    
    protected JMenuBar createMenuBar() {
        menuBar = new JMenuBar();
        
        //Set up the File menu.
        JMenu file_menu=addToMenuBar(menuBar, "File");
        //addToMenu(file_menu, "New Track", "new track");
        addToMenu(file_menu, "Quit", "quit");

        //Set up the Find menu.
        JMenu work_order_menu=addToMenuBar(menuBar, "Track Library");
        addToMenu(work_order_menu, "Search Track Setups", "search tracks");        

        //Set up the Help menu.
        JMenu help_menu=addToMenuBar(menuBar, "Help");
        addToMenu(help_menu, "Version", "version");
        //addToMenu(help_menu,"Reset Modem Connection","reset"); //problematic on LINUX leave out for now
        return menuBar;
    }
    
    private JMenu addToMenuBar(JMenuBar menuBar, String title) {
        JMenu t_menu = new JMenu(title);
        menuBar.add(t_menu);
        return t_menu;
    }
    
    private void addToMenu(JMenu t_menu, String title, String act_cmd) {
        //This method creates a menuItem with "title" adds it to the t_menu and 
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setActionCommand(act_cmd);
        menuItem.addActionListener(this);
        t_menu.add(menuItem);
    }
    
    //React to menu selections.
    public void actionPerformed(ActionEvent e) {
        String cmd=e.getActionCommand();
        if (cmd.equals("search tracks")) {
            findTracks();
        }
        
        if (cmd.equals("new track")) {
            newTrack();
        }
        if (cmd.equals("quit")) {
        	if (theModel!=null)theModel.connectOrDisconnect(null);//close the COM Port
        	System.exit(0);
        }
        if (cmd.equals("version")) {
        	helpFrame();
        }
        if (cmd.equals("reset")) {
        	resetModem();
        }
    }
    
    //Create a new internal frame.
    public static void newTrack() {
    	setCursorWait(true); 
    	FileChooser fc = new FileChooser(desktop, theModel);
        setCursorWait(false); 
    }

    protected void helpFrame() {
        //create a new Settings_Frame with a newly generated work_order_id
        Help_Frame help_frame = new Help_Frame(desktop);
    	help_frame.setVisible(true);
        desktop.add(help_frame);
        try {
        	help_frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}
    }    

    public  void findTracks() {
    	setCursorWait(true); 
    	closeIntroFrames();
    	if ((find_frame!=null)&&(find_frame.isVisible())){
    		find_frame.dispose();
    	}
    	find_frame = new Find_Settings_Frame(desktop, theModel);
        find_frame.setVisible(true);
        desktop.add(find_frame);
        
        try {
            find_frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}
        setCursorWait(false); 
    }
    
    
    public static void setCursorWait(Boolean state){
    	if (state){
    		desktop.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	}else{
    		desktop.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
        	}
    }

    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        The_Desktop setupDesktop = new The_Desktop();
        setupDesktop.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupDesktop.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                //Close out any settings frames that were open
            	if (theModel!=null)theModel.connectOrDisconnect(null);
            	System.exit(0); 
            }
        });  
        
        //Display the window.
        setupDesktop.setVisible(true);
    }
    
    public static void drawPage() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
