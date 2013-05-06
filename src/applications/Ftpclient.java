/*
 * Authors: 
 * Anvitha Jaishankar (anvithaj@cmu.edu)
 * Ruchir Patwa (rpatwa@cmu.edu)
 */
package applications;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import services.TTPclient;

// TODO: Auto-generated Javadoc
public class Ftpclient {
	private static TTPclient ts;
	private static String source_ip = "127.0.0.1";
	private static String destination_ip = "127.0.0.1";
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 * @throws InterruptedException the interrupted exception
	 * @throws NumberFormatException the number format exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, NumberFormatException, NoSuchAlgorithmException {
		if(args.length != 2) {
			printUsage();
		}
		
		System.out.println("Starting FTP client ...");
		int port = Integer.parseInt(args[0]);
		System.out.println("Enter the file name to be fetched : ");
		String filename;
	    Scanner scanIn = new Scanner(System.in);
	    filename = scanIn.nextLine();
	   /* System.out.println("Enter the timer value : ");
		int timer=5000;
	    timer = scanIn.nextInt();
	    scanIn.close();      */      
	    
		ts = new TTPclient();
		ts.connection_open(String.valueOf((short)Integer.parseInt(args[1])), String.valueOf((short)port), source_ip, destination_ip);
		byte[] received_byte_array = ts.receive_data(String.valueOf((short)port));
		String receivedfile = ts.send_file_name(filename,String.valueOf((short)Integer.parseInt(args[1])), String.valueOf((short)port), source_ip, destination_ip);
	
		byte dataToWrite[] = receivedfile.getBytes();
		String client_file_path = new String("clientfiles/" + filename);
		FileOutputStream out = new FileOutputStream(client_file_path);
		BufferedOutputStream bs = new BufferedOutputStream(out);
		bs.write(dataToWrite);
		bs.close();
		bs=null;
		ts.connection_close();
		
	}
	
	/**
	 * Prints the usage.
	 */
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
