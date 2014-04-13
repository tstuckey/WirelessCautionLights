package modem_comms;

public class ReceivedMessage {
	public String whole_msg;
	public String received_from_address;
	public int acked; 
	
	public ReceivedMessage(){
		whole_msg="";
		received_from_address="";
		acked=-1;//1=acked 0=nacked -1=neither
	}
}
