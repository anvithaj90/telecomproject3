package applications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.Scanner;

import services.DatagramService;
import services.TTPServer;

public class Ftpserver {
	private static DatagramService ds;
	private static TTPServer ts;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		if(args.length != 1) {
			printUsage();
		}
		
		System.out.println("Starting Server ...");
		
		int port = Integer.parseInt(args[0]);
	//	ds = new DatagramService(port, 10);
		ts = new TTPServer();
		run();
	}

	private static void run() throws IOException, ClassNotFoundException {
		byte[] received_byte_array = null;
		while(true) {
			received_byte_array = ts.receive_data("4444");
			if(received_byte_array[4]==32)
			{
				int i;
				int j = 0;
				byte[] new_data = new byte[received_byte_array.length];
				/*
				 * converting the filename byte array into string
				 */
				for(i=5;i<received_byte_array.length;i++,j++)
				{
					new_data[j] = received_byte_array[i];
				}
				String received_file = new String(new_data);
			    System.out.println("received file name:" + received_file);
			    if(received_byte_array[4] == 32)
			    	send_file(received_file);
			    /*
			     * 
			     * Write code here to read a file with file name = received_file 
			     * and store it in a byte array titled data_to_send
			     * 
			     */
			    
			}
			System.out.println("received data" + received_byte_array.toString());
		}
	}

	private static void send_file(String received_file) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("Received filename at the server" + received_file);
		File new_file = new File(received_file);	
	//	File file = new File("c:/EventItemBroker.java");

        byte[] data_to_send = new byte[(int) new_file.length()];
        try {
              FileInputStream fileInputStream = new FileInputStream(new_file);
              fileInputStream.read(data_to_send);
              fileInputStream.close();
       /*   for (int i = 0; i < data_to_send.length; i++) {
                          System.out.print((char)data_to_send[i]);
               }*/
         } catch (FileNotFoundException e) {
                     System.out.println("File Not Found.");
                     e.printStackTrace();
         }
         catch (IOException e1) {
                  System.out.println("Error Reading The File.");
                   e1.printStackTrace();
         }
		ts.send_file(data_to_send);
		
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}

}
