package base;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import modem_comms.ModemActions;

import java.awt.*;

/* Used by Setup_Desktop.java. */
public class Find_Settings_Frame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 40, yOffset = 40;
    
    Find_Settings_Table arrival_info;
    
    public Find_Settings_Frame(JDesktopPane desktop, ModemActions theModel) {
               super( "",   //blank title
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable
        
        JPanel t_panel = new JPanel();
        t_panel.setBorder( BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(0,0,0,0)));
        
        t_panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx=1.0;
        
        //put the items on the arrival_panel
        arrival_info = new Find_Settings_Table(this,desktop,theModel,t_panel,c);
        
        JScrollPane areaScrollPane=new JScrollPane(t_panel);
        areaScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        this.add(areaScrollPane);
        this.pack();
        this.setTitle("Search Track Setups");
        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }
}
