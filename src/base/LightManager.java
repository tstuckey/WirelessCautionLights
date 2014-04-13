package base;

import java.io.*;

import utilities.SplashScreen;


public class LightManager {
	public static boolean DEBUG=false;
	public static boolean DEBUG2=false;
	public static boolean COMM_DEBUG=false;

	public static void main(String args[])throws IOException {
		//pos 0 is the first arg passed into the application
		if (args.length>0){
			if (args[0].equalsIgnoreCase("comm_debug")){
				COMM_DEBUG=true; //if the user wants an in depth view of the msg traffic
			}
			else{
				System.out.println("To debug messages, invoke as java -jar Lights_Manager comm_debug");
			}
			
		}
		SplashScreen splashScreen=new SplashScreen("/img/logo.jpg");
		splashScreen.open (1500);
		//when the splash screen timer expires, the SplashScreen class invokes the The_Deskop
	}

}//end class Suspension_Notebook
