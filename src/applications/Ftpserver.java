	package applications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
		ts = new TTPServer();
		run();
	}

	private static void run() throws IOException, ClassNotFoundException {
		byte[] received_byte_array = null;
		while(true) {
			received_byte_array = ts.receive_data("4444");
			if(received_byte_array[8]==32)
			{
				int i;
				int j = 0;
				byte[] new_data = new byte[received_byte_array.length];
				/*
				 * converting the filename byte array into string
				 */
				for(i=9;i<received_byte_array.length;i++,j++)
				{
					new_data[j] = received_byte_array[i];
				}
				String received_file = new String(new_data);
			    System.out.println("received file name:" + received_file);
			    if(received_byte_array[8] == 32)
			    	send_file(received_file);
			    /*
			     * 
			     * Write code here to read a file with file name = received_file 
			     * and store it in a byte array titled data_to_send
			     * 
			     */
			    
			}
			else if(received_byte_array[8] == 8)
			{
				
				Thread newThread = new Thread(new TTPSend(received_byte_array,ts,received_byte_array[3]<<8 | received_byte_array[2]));
				newThread.start();
			}
			System.out.println("received data" + received_byte_array.toString());
		}
	}

	private static void send_file(String received_file) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("Received filename at the server" + received_file);
		File new_file = new File(received_file);	
        byte[] data_to_send = new byte[(int) new_file.length()];
        try {
              FileInputStream fileInputStream = new FileInputStream(new_file);
              fileInputStream.read(data_to_send);
              fileInputStream.close();
         } catch (FileNotFoundException e) {
                     System.out.println("File Not Found.");
                     e.printStackTrace();
         }
         catch (IOException e1) {
                  System.out.println("Error Reading The File.");
                   e1.printStackTrace();
         }
        Thread newThread = new Thread(new TTPSend(data_to_send,ts,0));
		newThread.start();	
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}

}

class TTPSend implements Runnable {
	private TTPServer server;
	private byte[] data;
	private int seq_num;
	public TTPSend(byte[] data_to_send, TTPServer ts, int seq_num) {
		// TODO Auto-generated constructor stub
		this.server = ts;
		this.data = data_to_send;
		this.seq_num = seq_num;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			if(data.length == 9)
			{
				if(data[8] == 8)
				server.send_file(null, seq_num);
			}
			else
			{
				server.send_file(data, seq_num);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
