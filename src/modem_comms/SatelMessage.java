package modem_comms;

import java.io.IOException;
import base.The_Desktop;

public class SatelMessage {
	char start_char= (char)2; //STX in ASCII
	char end_char=(char)3;    //ETX in ASCII
	char ack_char='6';    //ACK in ASCII
	char nack_char='F';    //SATEL's NACK character

	public TheCRC crc_instance;
	static Boolean regular_invocation=true;

	public SatelMessage() {
		crc_instance=new TheCRC();
	}

	/** A main program, for direct invocation and testing purposes. */
	public static void main(String[] argv) throws 
	IOException, InterruptedException {
		SatelMessage cm=new SatelMessage();
		//some basic testing data
		regular_invocation=false;
		System.out.println("SMD smg to 01 from 00 with outputs to 0\t"+cm.makeSMDcommand("01", "00", "0"));
		System.out.println("SMD smg to 01 from 00 with outputs to 1\t"+cm.makeSMDcommand("01", "00", "1"));
		System.out.println("SMD smg to 01 from 00 with outputs to 2\t"+cm.makeSMDcommand("01", "00", "2"));
		System.out.println("SMD smg to 01 from 00 with outputs to 3\t"+cm.makeSMDcommand("01", "00", "3"));
		System.out.println("GMD smg to 4C from 12\t"+cm.makeGMDcommand("4C","12"));
		System.out.println("ACK smg to 00 from 01\t"+cm.makeACKcommand("00","01"));
		System.out.println("NACK smg to 4C from 12\t"+cm.makeNACKcommand("4C","12"));
		System.out.println("SMD smg to FF from 00 with outputs to 1\t"+cm.makeSMDcommand("FF", "00", "1"));
		System.out.println("Formatted Hex String for "+"255"+ " is "+cm.getFormattedHexString("255"));


		System.exit(0);
	}
	
	 /*The General Message structure is:
		 *	   
		 *
		 * The checksum is calculated from the DATA-message only (include
		 * receiver and sender addresses, message length and message)
	 */
				
	//The General Message Structure is composed of four parts
	//|STX		 |DATA    | CRC    | ETX      |
	//1.Start character, STX, ASCII code 2
	//2.Data
	//3.CRC, as implemented in TheCRC class
	//4.End character, ETX, ASCII code 3
	//
	// The checksum is calculated from the DATA-message only (include
	// receiver and sender addresses, message length and message)


	private String getHexLength(String input){
		String unformatted_result=Long.toHexString(input.length()).toUpperCase();
		String formatted_result;
		if (unformatted_result.length()==1){
			formatted_result=String.format("0%s", unformatted_result);//add a leading zero if one digit only	
		}else{
			formatted_result=String.format("%s", unformatted_result);
		}
		
		return(formatted_result);
			}

	public String getFormattedHexString(String input){
		String unformatted_result=null;
		String formatted_result=null;
		
		try{
			Integer int_val=Integer.valueOf(input);
			unformatted_result=Integer.toHexString(int_val).toUpperCase();
			}catch (NumberFormatException nf){
			if (base.LightManager.COMM_DEBUG)System.out.println("SatelMessage: Had a problem with Integer.valueOf(input)");
			return null;
		}

		if (unformatted_result.length()==1){
			formatted_result=String.format("0%s", unformatted_result);//add a leading zero if one digit only	
		}else{
			formatted_result=String.format("%s", unformatted_result);
		}
		return(formatted_result);	
	}
	
	public String convertAndFormatHexStringToDecString(String hex_string){
		String unformatted_result=null;
		String formatted_result=null;
		
		try{
			unformatted_result=Integer.parseInt(hex_string,16)+"";
			}catch (NumberFormatException nf){
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\t\tSatelMessage: ERROR: Had a problem with Integer.parseInt(hex_string,16)");
			return "-1";
		}
			if (unformatted_result.length()==1){
				formatted_result=String.format("0%s", unformatted_result);//add a leading zero if one digit only	
			}else{
				formatted_result=String.format("%s", unformatted_result);
			}

		return(formatted_result);	
	}

	private String formatCounter(Integer input_counter){
		String formatted_result=input_counter.toString();

		while (formatted_result.length()<8){
			formatted_result="0"+formatted_result;
		}
		return(formatted_result);
			}
	
	public String makeSMDcommand(String receiver_address, String sender_address,
								String digital_ports){
		//SMD-message Set Mini-Link Data
		//With SMD-message the MASTER sets the states for the slave.  After a sent message, the Master waits
		//for the acknowledgment.  If everything was OK, the slave answers by sending an ACK-message.  If
		//the message was received but not understood (i.e. fail in the CRC), the slave sends a NACK-
		//message.  The slave sends SMD messages only by request.  The Master can send SMD-messages
		//any time.
		//
		//Example SMD-message: <STX>01000CSMD0000000001842<ETX>
		//bytes| 1		 | 2	 | 2	  | 2 	  |	3	   | 1		 | 8	   | 4	 	  | 1        |
		//     |---------|-------|--------|-------|--------|---------|---------|----------|----------|
		//	   |STX		 |01	 |00	  |0C	  |	SMD	   | 0		 | 0000	   | 1842	  | ETX      |
		//	   |		 |		 |		  |		  |		   |	     | 0000    |		  |          |
		//	   |		 |		 |		  |		  |		   |	     |         |		  |          |
		//	   |Start	 |Rec	 |Send    |Length |	Static | Digital | Counter | CRC	  |	End      |
		//	   |Character|Address|Address |		  | SMD	   | Ports	 |	       | checksum | Character|
		//The MESSAGE includes all commands to the unit.  In the example it is SMD 000 000 000
		//The structure is as follows: 
		//    |SMD       | 0               | 0000 0000   |
		//    |Command   | Setting the     | Setting the |
		//    |          | Digital Outputs | counter     |
		//The Length refers to the length of the command to the unit and the arguments
		//in the example above, the length is "SMD+0+0000+0000".length()
		//0x0C =12Dec
		//
		//Setting the Digital Outputs
		//Value
		//  0		All Ports OFF
		//  1		Port 1    ON
		//	2		Port 2    ON
		//  3		Port 1,2  ON
		//
		//The CRC checksum is a HEX value
		// calculated from the DATA-message only:
		// receiver address, sender addresses, message length, and message
				
		String command="SMD";
		//String counter="00000000"; /*for testing, we simplify the message and leave the counter at zero*/
		String formatted_counter=0+"";
		if (regular_invocation){
			The_Desktop.appCounter.incrementMsgCounter();/*increment the msg counter each time the SMD call is made*/
			formatted_counter=formatCounter(The_Desktop.appCounter.getCurrentMsgCount());	
		}
		
		//get the length of the concatenation of the message command, the digital ports to be set,
		//and the counter
		String message=command+digital_ports+formatted_counter;
		String message_length=getHexLength(message);
		//System.out.println("SMD: hex length was "+message_length);
			    
		String data=receiver_address+sender_address+message_length+message;
		
		//System.out.println("SMD: Getting the crc_value with parameters "+data.length()+"  "+data);
		String crc_val=crc_instance.CRC_16(data.length(), data);
		//System.out.println("SMD: TheCRC="+(crc_val)+" in hex");
		//System.out.println("SMD: Whole thing looks like: "+start_char+data+crc_val+end_char);
		
		return (start_char+data+crc_val+end_char);
	}
	
	public String makeGMDcommand(String receiver_address, String sender_address){
		//GMD-message 
		//With GET-message the Master can ask the status from the slave. GET-message is always 
		//answered by ACK-message followed by SMD-message. If the slave received a message, but it 
		//was not clear (i.e. fail in CRC), the slave answers with a NACK-message. 
		//
		//Example: Slave address is 4C (0x4C = 76 Dec) and Master address is 12 12 (0x12=18 Dec)
		//Example GMD-message: <STX>4C1203GMD2A34<ETX>
		//	   |---------|-------|--------|-------|--------|---------|----------|
		//	   |STX		 |4C	 |12	  |03	  |	GMD	   | 2A34	 | ETX      |
		//	   |		 |		 |		  |		  |		   |	     |          |
		//	   |Start	 |Rec	 |Send    |Length |	Message| CRC     | End      |
		//	   |Character|Address|Address |		  |    	   | checksum| Character|
		//The Length refers to the length of the command to the unit and the arguments
		//in the example above, the length is "GMD".length()
		//0x03 =3Dec
		
		String command="GMD";

		//get the length of the concatenation of the message command
		String message=command;
		String message_length=getHexLength(message);
				//System.out.println("GMD: hex length was "+message_length);
				
		String data=receiver_address+sender_address+message_length+message;
		
		//System.out.println("GMD: Getting the crc_value with parameters "+data.length()+"  "+data);
		String crc_val=crc_instance.CRC_16(data.length(), data);
		//System.out.println("GMD: TheCRC="+(crc_val)+" in hex");
		//System.out.println("GMD: Whole thing looks like: "+start_char+data+crc_val+end_char);
		
		return (start_char+data+crc_val+end_char);
	}
	
	public String makeACKcommand(String receiver_address, String sender_address){
		//ACK-message
		//The equipment that has received a GMG or SMD request will answer back with an ACK-message
		//
		//Example: Receiver address is 4C (0x4C = 76 Dec) and Sender address is 12 (0x12=18 Dec)
		//ACK-message: <STX>4C126<ETX>
		//	   |---------|-------|--------|---------------|----------|
		//	   |STX		 |4C	 |12	  |'6'	          | ETX      |
		//	   |         |       |        |               |          |
		//	   |Start	 |Rec	 |Send    | ACK-          | End      |
		//	   |Character|Address|Address |	Character	  | Character|		


		//get the length of the concatenation of the message command
		String data=receiver_address+sender_address+ack_char;
		
		//System.out.println("ACK: Whole thing looks like: "+start_char+data+end_char);
		
		return (start_char+data+end_char);
	}

	public String makeNACKcommand(String receiver_address, String sender_address){
		//NACK-message
		//The slave sends NACK-message, if it has got a GMD or SMD request, but has not been able to
		//decode the request (for example, error in CRC-check)
		//
		//Example: Receiver address is 4C (0x4C = 76 Dec) and Sender address is 12 (0x12=18 Dec)
		//ACK-message: <STX>4C12F<ETX>
		//	   |---------|-------|--------|---------------|----------|
		//	   |STX		 |4C	 |12	  |'F'	          | ETX      |
		//	   |         |       |        |               |          |
		//	   |Start	 |Rec	 |Send    | NACK-         | End      |
		//	   |Character|Address|Address |	Character	  | Character|		
		
		String data=receiver_address+sender_address+nack_char;
		
		//System.out.println("NACK: Whole thing looks like: "+start_char+data+end_char);
		
		return (start_char+data+end_char);
	}	
	
	/**Parse the ACK message for the address who sent the ACK message as well as the 
	 * specific ACK or NACK character.
	 * 
	 * @param whole_msg
	 * @return
	 */
	public ReceivedMessage ParseACK (String whole_msg){
		//This method uses the end_char as the point of reference
		//That is if we have the end_char we can figure some other things out about the message received
		//If we don't have an end_char, then we just return an empty Received Message
		//
		//Example: Receiver address is 00 (0x00 = 00 Dec) and Sender address is 12 (0x12=18 Dec)
		//ACK-message: <STX>00126<ETX>
		//	   |---------|-------|--------|---------------|----------|
		//	   |STX		 |00	 |12	  |'6'	          | ETX      |
		//	   |         |       |        |               |          |
		//	   |Start	 |Rec	 |Send    | ACK-          | End      |
		//	   |Character|Address|Address |	Character	  | Character|		

		
		if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: ParseAck: whole message was: "+whole_msg);
		
		ReceivedMessage result=new ReceivedMessage();
		
		if (whole_msg.length()<4){
			//we need at least four characters to work with, so if we have less than four, return the empty msg
			return result;
		}
		
		if (whole_msg.charAt(0)==start_char){
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: ParseAck Start Character was valid.");
		}else
		{
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: ParseAck Start Character was not valid.");
		}
			
		if (whole_msg.charAt(whole_msg.length()-1)==end_char){
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: ParseAck End Character was valid.");
		}else
		{
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: ParseAck End Character was not valid.");
			//since the end character was not valid, go ahead and return the empty msg
			return result;
		}

		result.whole_msg=whole_msg;//we are passed the point where we might return an empty msg
		                           //so go ahead and assign the input parameter to the appropriate field in the 
								   //returning class instance
		
		//Get the receiving and sending addresses which are Hexadecimal encoded strings
		//we are assuming the receiving address is the master which is set to 00
		//sending address should be the slave address
		String send_hex_address=whole_msg.substring(whole_msg.length()-4,whole_msg.length()-2);

		//Conversions from Hex Strings to Decimals Strings
		if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: received_from_address raw Hex "+send_hex_address);
		if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: received_from_address formatted as Dec "+convertAndFormatHexStringToDecString(send_hex_address));
		result.received_from_address=convertAndFormatHexStringToDecString(send_hex_address);
		
		//Segregated the ACK or NACK character		
		char t_ack_char=(whole_msg.substring(whole_msg.length()-2, whole_msg.length()-1)).charAt(0);
		if (base.LightManager.COMM_DEBUG)System.out.println("ack char was "+t_ack_char);
		
		if (t_ack_char==ack_char){
			//System.out.println("ACK received");
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: was ack'd ");
			result.acked=1;
		}else if (t_ack_char==nack_char){
			//System.out.println("NACK received");
			if (base.LightManager.COMM_DEBUG)System.out.println("\t\t\tSatelMessage: was nack'd ");
			result.acked=0;
		}else
			result.acked=-1;
				
		return result;
	}

	
	//The calculated CRC is not agreeing with the computed CRC; so we've blocked it off for now
	/*	public String makeVRScomand(String receiver_address, String sender_address){
		//VER-message
		//This message retrieves the SW-Verions of the C-LINK100
		//
		//Example: Receiver address is 4C (0x4C = 76 Dec) and Sender address is 12 (0x12=18 Dec)
		//VER-message: <STX>4C12FFF03VRSV1.0A6AE4<ETX>
		//     |---------|-------|--------|----------|--------|---------|---------|----------|----------|
		//	   |STX		 |4C	 |12	  |FFF       |	03 	  | VRS     | V1.0A   | 6AE4	 | ETX      |
		//	   |		 |		 |		  |		     |		  |	        |         |		     |          |
		//	   |Start	 |Rec	 |Send    |Extensions|Length  | Message | SW-     | CRC	     |	End     |
		//	   |Character|Address|Address |		     | 	      |    	    | Version | checksum | Character|
		
		String extensions="FFF";
		String command="VRS";
		String version="V1.0A";
		
		//get the length of the concatenation of the message command
		String message=command;
		String message_length=getHexLength(message);
				System.out.println("VRS: hex length was "+message_length);
				
		String data=receiver_address+sender_address+extensions+message_length+message+version;
		
		System.out.println("VRS: Getting the crc_value with parameters "+data.length()+"  "+data);
		String crc_val=crc_instance.CRC_16(data.length(), data);
		System.out.println("VRS: TheCRC="+(crc_val)+" in hex");
		System.out.println("VRS: Whole thing looks like: "+start_char+data+crc_val+end_char);
		
		return (start_char+data+crc_val+end_char);
	}*/
	

}
