package modem_comms;


public class MasterCounter {
	public static Integer msg_counter;
	public static long start_time;

	final Integer max_msg_count=99999999; 
	public MasterCounter(){
		start_time=System.currentTimeMillis();
		msg_counter=new Integer(0);
	}
	
	public static void restartTimer(){
		start_time=System.currentTimeMillis();
	}
	
	public void incrementMsgCounter(){
		if (msg_counter>=max_msg_count){
			msg_counter=new Integer(0);
		}
		msg_counter++;
	}
	
	public Integer getCurrentMsgCount(){
		return(msg_counter);
	}
	

}
