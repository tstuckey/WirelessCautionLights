package modem_comms;

public class TheCRC {

	public TheCRC(){
	}
/*The checksum to be used is a 16 bit CRC checksum
 *The General Message structure is:
 *	   |STX		 |DATA    | CRC    | ETX      |
 *
 * The checksum is calculated from the DATA-message only (include
 * receiver and sender addresses, message length and message)
 */
	
	
/*	Below is the C implementation of the SATEL CRC check
 * public short CRC_16 (unsigned char length, unsigned char *data){ 
					unsigned short crc_table[16] = 
					{ 
				0x0000, 0x1081, 0x2102, 0x3183, 0x4204, 0x5285, 0x6306, 0x7387, 
				0x8408, 0x9489, 0xA50A, 0xB58B, 0xC60C, 0xD68D, 0xE70E, 0xF78F 
		}; 

		unsigned short crc = 0xFFFF; 
		unsigned char tmp, index, i; 

		for (i = 0; i < length; i++) 
		{ 
			tmp = data[i]; 
			index = ((crc ^ tmp) & 0x000F); 
			crc = ((crc >> 4) & 0x0FFF) ^ crc_table[index]; 
			tmp >>= 4; 
			index = ((crc ^ tmp) & 0x000F); 
			crc = ((crc >> 4) & 0x0FFF) ^ crc_table[index]; 
		} 

		return (~crc); 
	} */

	public String CRC_16 (int length, String data){
	 //The data is the DATA portion of the message
	 //The CRC is calculated off of the DATA portion only
		char crc_table[] ={ 
							0x0000, 0x1081, 0x2102, 0x3183, 
							0x4204, 0x5285, 0x6306, 0x7387, 
							0x8408, 0x9489, 0xA50A, 0xB58B, 
							0xC60C, 0xD68D, 0xE70E, 0xF78F 
							}; 

		char crc = 0xFFFF; 
		char tmp, index, i; 
		
		for (i = 0; i < length; i++) 
		{ 
			tmp = data.charAt(i); 
			index = (char)((crc ^ tmp) & 0x000F); 
			crc = (char) (((crc >> 4) & 0x0FFF) ^ crc_table[index]); 
			tmp >>= 4; 
			index = (char) ((crc ^ tmp) & 0x000F); 
			crc = (char) (((crc >> 4) & 0x0FFF) ^ crc_table[index]); 
		} 	 
		
	//return the Hex value of the crc in UPPERCASE 
	//System.out.println("\tTheCRC:  Integer.toHexString( 0x10000 | (~crc)).substring(4).toUpperCase())"+ Integer.toHexString( 0x10000 | (~crc)).substring(4).toUpperCase()); 

	//truncate the first 5 digits of the hex output
	return (Integer.toHexString( 0x10000 | (~crc)).substring(4).toUpperCase());
	}
}//end class CRC
