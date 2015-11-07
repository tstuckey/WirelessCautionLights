package modem_comms;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import base.BackgroundFrame;
import base.The_Desktop;


/**
 * This application controls the Satel brand wireless modems
 * 
 * @author Satel Modem adaptation by Tom Stuckey
 */
public class ModemActions {
	The_Desktop parent_desktop;

	ProgressMonitor progressMonitor;

	Object the_selected_comm_port;

	/** The javax.com.CommPort object in use */
	private SerialPort thePort=null;
	protected HashMap portsIDmap = new HashMap();

	Long discovery_and_group_sendDelay;	/* milliseconds to wait before programmatically sending consecutive messages during group messages*/
	Long fullcourse_sendDelay;	/* milliseconds to wait before programmatically sending consecutive messages during full course caution*/

	Long receiveTimeOut;	/* milliseconds to wait before timeout in recv */
	
	/* Protocol characters used */
	char start_char= (char)2; //STX in ASCII
	char end_char=(char)3;    //ETX in ASCII
	char ack_char='6';        //ACK in ASCII
	char nack_char='F';       //SATEL's NACK character

	/** The input and output streams */
	private InputStream serialInput;
	private OutputStream serialOutput;

	protected InputStream inStream=null;
	protected OutputStream outStream=null;

	/** The state for disconnected and connected */
	static int S_DISCONNECTED = 0, S_CONNECTED = 1;
	/** The state, either disconnected or connected */
	public int state = S_DISCONNECTED;

	String slave_address;	/*Address of the master modem--for sandbox testing only*/
	String master_address;	/*Address of the master modem*/
	int start_address; 		/*Start Address*/
	int stop_address; 		/*Stop Address*/
	int progress_interval;


	/** Construct a TheModem */
	public ModemActions(The_Desktop desktop_class) {
		parent_desktop=desktop_class;
		setInitValues();
	}

	private void setInitValues(){
		ResourceBundle labels = ResourceBundle.getBundle("Connection_Props");
		master_address=labels.getString("MASTER_ADDRESS");
		start_address=Integer.valueOf(labels.getString("start_slave_range"));
		stop_address=Integer.valueOf(labels.getString("stop_slave_range"));
		receiveTimeOut=new Long(labels.getString("receive_timeout"));
		discovery_and_group_sendDelay=new Long(labels.getString("discovery_and_group_sendDelay"));
		fullcourse_sendDelay=new Long(labels.getString("fullcourse_sendDelay"));
		progress_interval=Integer.valueOf(labels.getString("progress_increment"));
	}

	private void setStreams(InputStream is, OutputStream os, PrintWriter errs) {
		inStream = is;
		outStream = os;
	}

	/** Load the list of Serial Ports into the chooser.
	 */
	public Vector<String> populateComboBox() {
		Vector<String> port_list=new Vector<String>();
		// get list of ports available on this particular computer,
		// by calling static method in CommPortIdentifier.
		Enumeration pList = null;
		try{
            pList = CommPortIdentifier.getPortIdentifiers();
        }catch (Exception e){
            System.err.println("No ports available");
        }

		// Process the list of ports, putting serial ports into ComboBox
		while ((pList!=null)&&(pList.hasMoreElements())) {
			CommPortIdentifier cpi = (CommPortIdentifier)pList.nextElement();
			if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				port_list.add(cpi.getName());
				portsIDmap.put(cpi.getName(), cpi);
			}
		}
		return port_list;
	}

	public void connectOrDisconnect(Object the_selected_comm_port){
		if (the_selected_comm_port==null){
			disconnect();
			return;
		}
		this.the_selected_comm_port=the_selected_comm_port;
		if (state == S_CONNECTED) {
			disconnect();
		} else {
			connect();
		}	
	}

	/** Connect to the chosen serial port, and set parameters. */
	void connect() {
		//if the modem instance is null; create a new instantiation before proceeding 
		setStreams(serialInput, serialOutput, new PrintWriter(System.out));

		try {
			// Open the specified serial port
			CommPortIdentifier cpi = (CommPortIdentifier)portsIDmap.get(the_selected_comm_port);
			thePort = (SerialPort)cpi.open("JModem", 15*1000);//wait 15000 milliseconds to block waiting for
			//port to open
			// Set the serial port parameters.
			thePort.setSerialPortParams(
					9600,					// baud
					SerialPort.DATABITS_8,	//bits per byte (for text 7 is an option)
					SerialPort.STOPBITS_1,	// stop bits
					SerialPort.PARITY_NONE);// parity; no parity since we are relying on CRC checksums

			thePort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN & SerialPort.FLOWCONTROL_RTSCTS_OUT);
		} catch (PortInUseException pue) {
			errDialog("Port in use: close other app, or use different port.");
			return;
		} catch (UnsupportedCommOperationException uoe) {
			errDialog("Unsupported options error: try different settings");
			return;
		}

		// Similar to "raw" mode: return when 1 or more chars available.
		try {
			thePort.enableReceiveThreshold(1);
			if (!thePort.isReceiveThresholdEnabled()) {
				errDialog("Could not set receive threshold");
				disconnect();
				return;
			}
			//thePort.setInputBufferSize(buf.length);
		} catch (UnsupportedCommOperationException ev) {
			errDialog("Unable to set receive threshold in Comm API; port unusable.");
			disconnect();
			return;
		}

		// Get the streams
		try {
			inStream = thePort.getInputStream();
		} catch (IOException e) {
			errDialog("Error getting input stream:\n" + e.toString());
			return;
		}
		try {
			outStream = thePort.getOutputStream();
		} catch (IOException e) {
			errDialog("Error getting output stream:\n" + e.toString());
			return;
		}
		// Finally, tell rest of program, and user, that we're online.
		state = S_CONNECTED;
		if (base.LightManager.COMM_DEBUG) System.out.println("Starting thread");
		if (parent_desktop!=null){
			parent_desktop.connectedToPort(true);
		}
	}	

	/** Break our connection to the serial port. */
	void disconnect() {
		// Tell java.io we are done with the input and output
		if (thePort==null)return; //nothing has been instantiated, so nothing has to be closed

		try {
			inStream.close();
			outStream.close();
			if (base.LightManager.COMM_DEBUG) System.out.println("Stopping thread");
		} catch (IOException e) {
			errDialog("IO Exception closing port:\n");
		} catch (NullPointerException gen_exception) {
			errDialog("General Exception closing port:\n");
		}
		// Tell javax.comm we are done with the port.
		try {
			thePort.removeEventListener();
			thePort.close();
		} catch (Exception e) {
			System.out.println("Had a problem closing the port.");
			//e.printStackTrace();
		}

		// Tell rest of program we are no longer online.
		state = S_DISCONNECTED;

		serialInput=null;
		serialOutput=null;

		inStream=null;
		outStream=null;

		if (parent_desktop!=null){
			parent_desktop.connectedToPort(false);
		}
	}

	protected void resetStreams(){
		try {
			while(inStream.available()>0){
				getchar();//if anything is waiting in the queue; purge it one character at a time
			}
		outStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("\t\t\t\tModemActions: reset failed!");
			e.printStackTrace();
		}
	}

	protected char getchar() throws IOException {
		return (char)inStream.read();
	}

	protected void putchar(int c) throws IOException {
		outStream.write(c);
	}

	
	private Integer formatAsInteger(Double in_double){
		DecimalFormat df = new DecimalFormat("###");
		Integer out_integer=new Integer(df.format(in_double));	
		return out_integer;	
	}	
	
	/**This method instantiates the ProgressMonitor
	 * 
	 * @param parent_frame
	 */
	public void setupProgressMonitor(JInternalFrame parent_frame, String overall_msg, int max_val){
		UIManager.put("ProgressMonitor.progressText", overall_msg);
		progressMonitor = new ProgressMonitor(parent_frame,"","", 0, max_val);
		progressMonitor.setMillisToDecideToPopup(10);
		progressMonitor.setMillisToPopup(10);
		progressMonitor.setProgress(0);
	}

	
	/**This method updates the ProgressMonitor; if the progress monitor has been cancelled, it returns false
	 */
	public Boolean updateProgressMonitor(int increment, String note){
		if (progressMonitor.isCanceled()){
			progressMonitor.setProgress(0);
			return false;
		}
		progressMonitor.setProgress(increment);//increment the progress bar
		progressMonitor.setNote(note);
		return true;
	}

/**This method receives the ReceivedMessage Pool and the type of acknowledgment we are looking for.
 * Based on the acknowledgment String, this method looks for those type of messages and puts them in a valid_address
 * vector which is then returned. 
 * 
 * @param t_pool
 * @param acknowledgment
 * @return vector of the addresses whose return type was passed in as the acknowledgment parameter
 */
	private Vector<String> determineAddresses(ReceivedMessagedPool t_pool, String acknowledgment){
		Vector<String> valid_addresses=new Vector<String>();
		int ack_value_to_check_for=0;
		
		if (acknowledgment.equals("ACK")){
			ack_value_to_check_for=1;
		}else if(acknowledgment.equals("NACK")){
			ack_value_to_check_for=0;
		}else if (acknowledgment.equals("failure")){
			ack_value_to_check_for=-1;
		}

		if (base.LightManager.COMM_DEBUG)System.out.println("determineAddresses: there were "+t_pool.received_pool.size()+" entries in the buffer");
		for(int i=0;i<t_pool.received_pool.size();i++){
			if (t_pool.received_pool.elementAt(i).acked==ack_value_to_check_for){
				valid_addresses.add(t_pool.received_pool.elementAt(i).received_from_address);
			}
		}
		return valid_addresses;
	}	

	private ReceivedMessagedPool parsePool(ReceivedMessagedPool t_pool){
		ReceivedMessagedPool result_pool=new ReceivedMessagedPool();//create a new instance for return
		SatelMessage satelMsg=new SatelMessage();
		String a_raw_msg;
		ReceivedMessage processed_msg;

		if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: parsePool: there were "+t_pool.received_pool.size()+" entries in the buffer");
		for(int i=0;i<t_pool.received_pool.size();i++){
			a_raw_msg=t_pool.received_pool.elementAt(i).whole_msg;
			processed_msg=satelMsg.ParseACK(a_raw_msg); //invoke the parsing method and store the formatted msg				
			result_pool.received_pool.add(processed_msg);
		}
		return result_pool;
	}	
	
	/**This inner class sends out a turn-off command to a specified range of slave addresses. This class receives 
	 * the reference to the instance of the Receiving Loop class. When the thread is done sending out the addresses,
	 * the "done" method waits for the receiver thread in the Receiving Loop class to complete; when it has completed, the
	 * message pool (a.k.a. buffer ) is processed, and the nodes that acknowledged the command are placed onto the active background
	 * as valid light nodes. 
	 *
	 */
	class DiscoveryJob extends SwingWorker<Void, Void>{
		ReceivingLoop the_receiver=null;
		JInternalFrame jif;
		BackgroundFrame bf;
		Boolean do_progress=true; //yes we want a progres monitor during discovery
		
		public DiscoveryJob(JInternalFrame parent_frame, BackgroundFrame bf){
			this.jif=parent_frame;
			this.bf=bf;
		}
		
		/**This method is the executed to start the thread to execute the discovery run.
   	     */
		public Void doInBackground() {
			String potential_address;
			String digital_output="0";//  0		All Ports OFF
			SatelMessage satel_msgs=new SatelMessage();
			setupProgressMonitor(jif, "Discovering...", stop_address+progress_interval);

			try {

				the_receiver=new ReceivingLoop();
				the_receiver.execute(); //start the thread to listen for responses
				
				if (base.LightManager.COMM_DEBUG)System.out.println("\n\nModemActions: scanRange: starting discovery iterations.");
				master_address=satel_msgs.getFormattedHexString(master_address);
				for (int index=start_address; index<=stop_address; index++ ){
					potential_address=satel_msgs.getFormattedHexString(index+"");

					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: scanRange: sending a message to "+potential_address);
					//ping message will be a command to turn off the digital ports
					//E.g. makeSMDcomand(potential_address, master_address, "0");
					String cmd_string=satel_msgs.makeSMDcommand(potential_address, master_address, digital_output);
					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: sending message "+cmd_string+" to "+potential_address);
					for (int char_index = 0; char_index < cmd_string.length(); char_index++) {
						putchar(cmd_string.charAt(char_index));
					}

					Boolean progress_alive=updateProgressMonitor(index, "Initializing..."+formatAsInteger((double)index/stop_address * 100) +"%");
					if (!progress_alive){
						//The progress monitor has been canceled
						//so kill the receiving loop and try to backout gracefully
						bf.setCursorWait(false);
						the_receiver.cancel(true);
						this.cancel(true);
					}
					try {
						Thread.sleep(discovery_and_group_sendDelay);  //we wait for just a few milliseconds so we don't overwhelm our communications
						                          //infrastructure with simultaneous send and receive traffic
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
					
				}//end address loop
				if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: scanRange: ending discovery iterations.");
				
				//Initialize and start the receive timer AFTER all the messages have been sent out
				IOTimer the_timer=new IOTimer(receiveTimeOut, the_receiver, do_progress);
				the_timer.execute();

				if (the_receiver == null)return null; //don't wait if the receiver is null
				while (!the_receiver.isDone()){
					//loop until the receiving thread has completed
				}
			} catch (IOException io_exception) {
			}	
			return null;
		}

		public void done() {
			//The thread is now finished, and the pool is no longer open to receive new messages
			//now we need to update the Background Image with the new nodes
			ReceivedMessagedPool final_pool=parsePool(the_receiver.msg_pool);//create a final instance of the pool
			Vector<String> valid_addresses=determineAddresses(final_pool, "ACK");
			bf.updateWithDiscoveredLightNodes(valid_addresses);

		}//end method done
	}//end class DiscoveryJob

	class EverythingOffJob extends SwingWorker<Void, Void>{
		ReceivingLoop the_receiver=null;
		JInternalFrame jif;
		BackgroundFrame bf;
		Boolean do_progress=false; //no we want a progress monitor during the turn off iterations
		
		public EverythingOffJob(JInternalFrame parent_frame, BackgroundFrame bf){
			this.jif=parent_frame;
			this.bf=bf;
		}
		
		/**This method is the executed to start the thread to execute the discovery run.
   	     */
		public Void doInBackground() {
			String potential_address;
			String digital_output="0";//  0		All Ports OFF
			SatelMessage satel_msgs=new SatelMessage();
			
			try {

				the_receiver=new ReceivingLoop();
				the_receiver.execute(); //start the thread to listen for responses
				
				if (base.LightManager.COMM_DEBUG)System.out.println("\n\nModemActions: turnoff: starting turnoff iterations.");
				master_address=satel_msgs.getFormattedHexString(master_address);
				for (int index=start_address; index<=stop_address; index++ ){
					potential_address=satel_msgs.getFormattedHexString(index+"");

					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: turnoff: sending a message to "+potential_address);
					//ping message will be a command to turn off the digital ports
					//E.g. makeSMDcomand(potential_address, master_address, "0");
					String cmd_string=satel_msgs.makeSMDcommand(potential_address, master_address, digital_output);
					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: turnoff: sending message "+cmd_string+" to "+potential_address);
					for (int char_index = 0; char_index < cmd_string.length(); char_index++) {
						putchar(cmd_string.charAt(char_index));
					}

					try {
						Thread.sleep(0);  //we wait for just a few milliseconds so we don't overwhelm our communications
						                          //infrastructure with simultaneous send and receive traffic
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
					
				}//end address loop
				if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: turnoff: ending turnoff iterations.");
				
				//Initialize and start the receive timer AFTER all the messages have been sent out
				IOTimer the_timer=new IOTimer(receiveTimeOut, the_receiver, do_progress);
				the_timer.execute();

				if (the_receiver == null)return null; //don't wait if the receiver is null
				while (!the_receiver.isDone()){
					//loop until the receiving thread has completed
				}
			} catch (IOException io_exception) {
			}	
			return null;
		}

		public void done() {
			//The thread is now finished, and the pool is no longer open to receive new messages
			//now we need to update the Background Image with the new nodes
			ReceivedMessagedPool final_pool=parsePool(the_receiver.msg_pool);//create a final instance of the pool
			Vector<String> valid_addresses=determineAddresses(final_pool, "ACK");//Since we are turning everything off,
			//we aren't doing anything with these information; it is do only for programmatic parallelism 
			
			bf.updateEverythingOff();

		}//end method done
	}//end class EverythingOffJob


	
	
	/**This inner class is the timer for receiving input.  This class receives the timeout period, in milliseconds,
	 * and the reference to the instance of the Receiving Loop class.  When the thread contained is invoked, it loops until the
	 * timeout period expires, and then cancels (with interrupt priority) the receiving thread within the Receiving Loop class.
	 */
	class IOTimer extends SwingWorker<Void, Void>{
		long milliseconds;
		ReceivingLoop receiving_class_ref;
		Boolean do_progress;

		/** Construct an IO Timer */
		public IOTimer(Long receive_timeout, ReceivingLoop t_receiving_class_ref, Boolean do_progress) {
			milliseconds = receive_timeout;
			receiving_class_ref=t_receiving_class_ref;
			this.do_progress=do_progress;

		}
		public Void doInBackground(){
			try{
				long short_sleep=milliseconds/progress_interval;
				if (base.LightManager.COMM_DEBUG)System.out.println("\t\tModemActions: IOTimer: starting timer ");
				for (int i=1; i<=progress_interval; i++){
					Thread.sleep(short_sleep);
					if (do_progress)updateProgressMonitor(i, "Processing... "+formatAsInteger((double)i/progress_interval * 100) +"%");
				}
				if (base.LightManager.COMM_DEBUG)System.out.println("\t\tModemActions: IOTimer: stopping timer ");
				receiving_class_ref.cancel(true);//the time period has expired, so send a request for the thread to exit

			}catch (InterruptedException e){
				System.out.println("ModemActions; IOtimer: this thread was interrupted, but shouldn't have been.");
			}
			return null;
		}
		
		public void done(){
			if (do_progress) progressMonitor.close();// we're done timing so close down the progress monitor if it is still open
		}
	}

/**This inner class instantiates the Receiving Message Pool and listens until the timer thread
 * cancels this thread.  It accepts input characters one at a time and concatenates the characters
 * into a strings (an end_char is the signifying value to break into another string);
 *  the strings are then submitted into the pool (a.k.a buffer) for later processing.
 */
	class ReceivingLoop extends SwingWorker<Void, Void>{
		ReceivedMessagedPool msg_pool;

		public Void doInBackground(){
			msg_pool=new ReceivedMessagedPool();//instantiate the message pool
			char character;
			String whole_msg="";
			ReceivedMessage received_msg=new ReceivedMessage();

			try{
				if (base.LightManager.COMM_DEBUG)System.out.println("\t\tModemActions: RECEIVINGLOOP: listening for first set of chars... ");
				while(!isCancelled()){
					//loop infinitely until this thread is interrupted my an instance of the IOTimer thread
					character = getchar();
					if (whole_msg.length()<1)whole_msg=character+"";
					else whole_msg=whole_msg+character;

					if (character==end_char){
						//we received an end character, so add this string to the buffer
						received_msg.whole_msg=whole_msg; //put the whole_msg in the ReceivedMessage class instance
						msg_pool.received_pool.add(received_msg);
						if (base.LightManager.COMM_DEBUG)System.out.println("\t\tModemActions: RECEIVINGLOOP: msg recedived was: "+received_msg.whole_msg);
						whole_msg="";//reset whole message
						received_msg=new ReceivedMessage();
					}
				}
				

			}catch (IOException e){
				if (base.LightManager.COMM_DEBUG)System.out.println("\t\tModemActions: RECEIVINGLOOP: Problem reading characters from ");
			}
			return null;
		}
		
		public void done(){
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\tModemActions: RECEIVINGLOOP: DONE listening for responses ");
		}
	}	

	
	/**This inner class sends out a turn-off command to a specified range of slave addresses. This class receives 
	 * the reference to the instance of the Receiving Loop class. When the thread is done sending out the addresses,
	 * the "done" method waits for the receiver thread in the Receiving Loop class to complete; when it has completed, the
	 * message pool (a.k.a. buffer ) is processed, and the nodes that acknowledged the command are placed onto the active background
	 * as valid light nodes. 
	 *
	 */
	class FullCourseJob extends SwingWorker<Void, Void>{
		ReceivingLoop the_receiver=null;
		Vector<String> valid_slave_addresses;
		JInternalFrame jif;
		BackgroundFrame bf;
		Boolean do_progress=false; //no progress for full course actions
		
		Boolean turn_on;
		Color light_color=null;

		public FullCourseJob(JInternalFrame jif, BackgroundFrame bf, Boolean turn_on, Color light_color, Vector<String> valid_slave_addresses){
			this.jif=jif;
			this.bf=bf;
			this.turn_on=turn_on;
			this.light_color=light_color;
			this.valid_slave_addresses=valid_slave_addresses;
		}
		
		/**This method is the executed to start the thread to execute the full course job.
   	     */
		public Void doInBackground() {
			String slave_address;
			String digital_output;
			SatelMessage satel_msgs=new SatelMessage();
			
			if (turn_on){
				//command was to turn on the full course caution lights; this is a red light or port 1
				if (light_color==Color.RED){
					digital_output="1";	
				}else
					//default to making the light yellow 
					digital_output="2";
			}else{
				//command was to turn off the full course caution lights
				digital_output="0";
			}
			
			try {
				//FOR TESTING ONLY!!! REMOVE FOR PRODUCTION
			/*	valid_slave_addresses=new Vector<String>();
				for (int i=1; i<=15; i++){
					if (i<=9){
						valid_slave_addresses.add("0"+i);
					}
					else{
						valid_slave_addresses.add(i+"");
					}
				 System.out.println("Slave address is "+valid_slave_addresses.elementAt(i-1));
				}*/
				//FOR TESTING ONLY!!! REMOVE FOR PRODUCTION
				
				
				the_receiver=new ReceivingLoop();
				the_receiver.execute(); //start the thread to listen for responses
				//updateProgressMonitor(1, "Initializing...");
				
				if (base.LightManager.COMM_DEBUG)System.out.println("\n\nModemActions: FullCourseJob_asIndividuals: starting full course iterations; there are "+
																		valid_slave_addresses.size()+" addresses to send to." );
				master_address=satel_msgs.getFormattedHexString(master_address);

				for (int index=0; index<valid_slave_addresses.size(); index++ ){
					slave_address=satel_msgs.getFormattedHexString(valid_slave_addresses.elementAt(index)+"");
					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: FullCourseJob_asIndividuals: sending a message to "+slave_address);
					//ping message will be a command to turn off the digital ports
					//E.g. makeSMDcomand(potential_address, master_address, "0");
					String cmd_string=satel_msgs.makeSMDcommand(slave_address, master_address, digital_output);
					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: FullCourseJob_asIndividuals: sending message "+cmd_string+" to "+slave_address);
					for (int char_index = 0; char_index < cmd_string.length(); char_index++) {
						putchar(cmd_string.charAt(char_index));
					}
					//updateProgressMonitor(index, "Initializing..."+formatAsInteger((double)index/valid_slave_addresses.size() * 100) +"%");
					try {
						Thread.sleep(fullcourse_sendDelay);  //we wait for just a few milliseconds so we don't overwhelm our communications
						                          //infrastructure with simultaneous send and receive traffic
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
					
				}//end address loop
				if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: FullCourseJob_asIndividuals: ending full course iterations.");
				
				//Initialize and start the receive timer AFTER all the messages have been sent out
				IOTimer the_timer=new IOTimer(receiveTimeOut, the_receiver,do_progress);
				the_timer.execute();

				if (the_receiver == null)return null; //don't wait if the receiver is null
				while (!the_receiver.isDone()){
					//loop until the receiving thread has completed
				}
				
			} catch (IOException io_exception) {
			}	
			return null;
		}

		public void done() {
			//The thread is now finished, and the pool is no longer open to receive new messages
			//now we need to update the Background Image with the new nodes
			if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: FullCourseJob_asIndividuals: FullCourseJobThread is done with primary execution.");
			ReceivedMessagedPool final_pool=parsePool(the_receiver.msg_pool);//create a final instance of the pool
			bf.updateFullCourse_asIndividuals(final_pool, turn_on,light_color);

		}//end method done
	}//end class FullCourseJob_asIndividuals	


	
	/**This inner class sends out a turn-off command to a specified range of slave addresses. This class receives 
	 * the reference to the instance of the Receiving Loop class. When the thread is done sending out the addresses,
	 * the "done" method waits for the receiver thread in the Receiving Loop class to complete; when it has completed, the
	 * message pool (a.k.a. buffer ) is processed, and the nodes that acknowledged the command are placed onto the active background
	 * as valid light nodes. 
	 *
	 */
	class MultipleAddressJob extends SwingWorker<Void, Void>{
		ReceivingLoop the_receiver=null;
		Vector<String> valid_slave_addresses;
		JInternalFrame jif;
		BackgroundFrame bf;
		Boolean do_progress=false; //no progress for full course actions
		
		Boolean turn_on;
		Color light_color=null;

		public MultipleAddressJob(JInternalFrame jif, BackgroundFrame bf, Boolean turn_on, Color light_color, Vector<String> valid_slave_addresses){
			this.jif=jif;
			this.bf=bf;
			this.turn_on=turn_on;
			this.light_color=light_color;
			this.valid_slave_addresses=valid_slave_addresses;
		}
		
		/**This method is the executed to start the thread to execute the full course job.
   	     */
		public Void doInBackground() {
			String slave_address;
			String digital_output;
			SatelMessage satel_msgs=new SatelMessage();
			
			if (turn_on){
				//command was to turn on the full course caution lights; this is a red light or port 1
				if (light_color==Color.RED){
					digital_output="1";	
				}else
					//default to making the light yellow 
					digital_output="2";
			}else{
				//command was to turn off the full course caution lights
				digital_output="0";
			}
			
			try {
				the_receiver=new ReceivingLoop();
				the_receiver.execute(); //start the thread to listen for responses
				//updateProgressMonitor(1, "Initializing...");
				
				if (base.LightManager.COMM_DEBUG)System.out.println("\n\nModemActions: MultipleAddressJob: starting full course iterations; there are "+
																		valid_slave_addresses.size()+" addresses to send to." );
				master_address=satel_msgs.getFormattedHexString(master_address);

				for (int index=0; index<valid_slave_addresses.size(); index++ ){
					slave_address=satel_msgs.getFormattedHexString(valid_slave_addresses.elementAt(index)+"");
					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: MultipleAddressJob: sending a message to "+slave_address);
					//ping message will be a command to turn off the digital ports
					//E.g. makeSMDcomand(potential_address, master_address, "0");
					String cmd_string=satel_msgs.makeSMDcommand(slave_address, master_address, digital_output);
					if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: MultipleAddressJob: sending message "+cmd_string+" to "+slave_address);
					for (int char_index = 0; char_index < cmd_string.length(); char_index++) {
						putchar(cmd_string.charAt(char_index));
					}

					try {
						Thread.sleep(discovery_and_group_sendDelay);  //we wait for just a few milliseconds so we don't overwhelm our communications
						                          //infrastructure with simultaneous send and receive traffic
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
					
				}//end address loop
				if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: MultipleAddressJob: ending course iterations.");

				//Initialize and start the receive timer AFTER all the messages have been sent out
				IOTimer the_timer=new IOTimer(receiveTimeOut, the_receiver,do_progress);
				the_timer.execute();

				if (the_receiver == null)return null; //don't wait if the receiver is null
				while (!the_receiver.isDone()){
					//loop until the receiving thread has completed
				}
				
			} catch (IOException io_exception) {
			}	
			return null;
		}

		public void done() {
			//The thread is now finished, and the pool is no longer open to receive new messages
			//now we need to update the Background Image with the new nodes
			if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: MultipleAddressJob: MultipleAddressJobThread is done with primary execution.");
			ReceivedMessagedPool final_pool=parsePool(the_receiver.msg_pool);//create a final instance of the pool
			bf.updateSubsetCourse(valid_slave_addresses,final_pool, turn_on,light_color);

		}//end method done
	}//end class MultipleAddressJob	
	
	

	/**This inner class sends out a turn-off command to a specified range of slave addresses. This class receives 
	 * the reference to the instance of the Receiving Loop class. When the thread is done sending out the addresses,
	 * the "done" method waits for the receiver thread in the Receiving Loop class to complete; when it has completed, the
	 * message pool (a.k.a. buffer ) is processed, and the nodes that acknowledged the command are placed onto the active background
	 * as valid light nodes. 
	 *
	 */
/*	class SingleAddressJob extends SwingWorker<Void, Void>{
		ReceivingLoop the_receiver=null;
		JInternalFrame jif;
		LightNode node;

		String valid_slave_address;
		Boolean turn_on;

		public SingleAddressJob(JInternalFrame jif, LightNode node, Boolean turn_on, String valid_slave_address){
			this.jif=jif;
			this.node=node;
			this.turn_on=turn_on;
			this.valid_slave_address=valid_slave_address;
		}

		*//**This method is the executed to start the thread to execute the full course job.
		 *//*
		public Void doInBackground() {
			String slave_address;
			String digital_output;
			SatelMessage satel_msgs=new SatelMessage();
			Boolean do_progress=false;

			if (turn_on){
				//command was to turn on the full course caution lights; this is a yellow light or port 2
				digital_output="2";
			}else{
				//command was to turn off the full course caution lights
				digital_output="0";
			}

			try {
				the_receiver=new ReceivingLoop();
				the_receiver.execute(); //start the thread to listen for responses

				master_address=satel_msgs.getFormattedHexString(master_address);
				slave_address=satel_msgs.getFormattedHexString(valid_slave_address);

				if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: SingleJob: sending a message to "+slave_address);
				//ping message will be a command to turn off the digital ports
				//E.g. makeSMDcomand(potential_address, master_address, "0");
				String cmd_string=satel_msgs.makeSMDcommand(slave_address, master_address, digital_output);
				if (base.LightManager.COMM_DEBUG)System.out.println("ModemActions: SingleJob: sending message "+cmd_string+" to "+slave_address);
				for (int char_index = 0; char_index < cmd_string.length(); char_index++) {
					putchar(cmd_string.charAt(char_index));
				}

				//initialize and start the receive timer AFTER the message has been sent out
				IOTimer the_timer=new IOTimer(individual_receiveTimeOut, the_receiver, do_progress);
				the_timer.execute();

				if (the_receiver == null)return null; //don't wait if the receiver is null
				while (!the_receiver.isDone()){
					//loop until the receiving thread has completed
				}

			} catch (IOException io_exception) {
			}	
			return null;
		}

		public void done() {
			//The thread is now finished, and the pool is no longer open to receive new messages
			//now we need to update the Background Image with the new nodes

			ReceivedMessagedPool final_pool=parsePool(the_receiver.msg_pool);//create a final instance of the pool
			node.updateLightNode(final_pool, turn_on);
		}//end method done
	}//end class SingleJob	
*/
		
	/**This method is the central control point for discovering a range of values.
	 * 
	 * @param parent_frame
	 * @param calling_bf
	 */
	public void discoverNodes(JInternalFrame parent_frame, BackgroundFrame calling_bf){
		resetStreams();
		DiscoveryJob discoveryJob=new DiscoveryJob(parent_frame,calling_bf);//now actually start discovery
		discoveryJob.execute();//now actually start discovery
	}
	
	public void fullCourseCaution(JInternalFrame parent_frame,Boolean turn_on, Vector<String> valid_slave_addresses, BackgroundFrame calling_bf){
		resetStreams();
		FullCourseJob fullCourseJob=new FullCourseJob(parent_frame,calling_bf,turn_on,Color.RED, valid_slave_addresses);
		fullCourseJob.execute();//now actually invoke the full course command 
	}

	public void everythingOff(JInternalFrame parent_frame, BackgroundFrame calling_bf){
		resetStreams();
		EverythingOffJob everythingOffJob=new EverythingOffJob(parent_frame,calling_bf);//turn everything off
		everythingOffJob.execute();//now actually start discovery
	}
		
	
	public void sendIndividualCommandToMultipleAddresses(JInternalFrame parent_frame,Boolean turn_on, Vector<String> valid_slave_addresses, BackgroundFrame calling_bf){
		resetStreams();
		MultipleAddressJob multipleJob=new MultipleAddressJob(parent_frame,calling_bf,turn_on,Color.YELLOW, valid_slave_addresses);
		multipleJob.execute();//now actually invoke the full course command 
	}
	
/*	public void sendIndividualCommand(JInternalFrame parent_frame,Boolean turn_on,String valid_slave_address, LightNode node){
		resetStreams();
		SingleAddressJob singleJob=new SingleAddressJob(parent_frame,node, turn_on, valid_slave_address);
		singleJob.execute();//now actually invoke the single command
	}*/


	/** Convenience routine: Show a standard-form error dialog */
	private void errDialog(String message) {
		JOptionPane.showMessageDialog(null, message,
				"Communications Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

}

