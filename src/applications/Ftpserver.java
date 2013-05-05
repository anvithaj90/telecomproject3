	package applications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import services.DatagramService;
import services.TTPServer;

// TODO: Auto-generated Javadoc
public class Ftpserver {
	
	private static TTPServer ts;
	private static String SOURCE_PORT = "4444";
	private static int FILE_NAME = 32;
	private static final int HEADER_SIZE = 9;
	private static final int ACK = 8;
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		if(args.length != 1) {
			printUsage();
		}
		
		System.out.println("Starting Server ...");
		
		Integer.parseInt(args[0]);
		ts = new TTPServer();
		run();
	}

	/**
	 * Run.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private static void run() throws IOException, ClassNotFoundException {
		byte[] received_byte_array = null;
		while(true) {
			received_byte_array = ts.receive_data(SOURCE_PORT);
			if(received_byte_array[8] == FILE_NAME)
			{
				int i;
				int j = 0;
				byte[] new_data = new byte[received_byte_array.length];
				/*
				 * converting the filename byte array into string
				 */
				for(i=HEADER_SIZE;i<received_byte_array.length;i++,j++)
				{
					new_data[j] = received_byte_array[i];
				}
				String received_file = new String(new_data);
			    if(received_byte_array[8] == FILE_NAME)
			    	send_file(received_file);			    
			}
			else if(received_byte_array[8] == ACK)
			{
				System.out.println("got final ack of open connection");
				int received_ack = byte_to_int(received_byte_array,7,6,5,4);
				Thread newThread = new Thread(new TTPSend(received_byte_array,ts,received_ack));
				newThread.start();
			}
			System.out.println("received data" + received_byte_array.toString());
		}
	}

	private static int byte_to_int(byte[] received_byte_array, int i, int j,
			int k, int l) {
		// TODO Auto-generated method stub
		int value = received_byte_array[i] << 24 | (received_byte_array[j] & 0xFF) << 16 | (received_byte_array[k] & 0xFF) << 8 | (received_byte_array[l] & 0xFF);
		return value;
	}

	/**
	 * Send_file.
	 *
	 * @param received_file the byte array to send to the client
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private static void send_file(String received_file) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		File new_file = new File(received_file);	
        byte[] data_to_send = new byte[(int) new_file.length()];
        try {
              FileInputStream fileInputStream = new FileInputStream(new_file);
              fileInputStream.read(data_to_send,0,(int) new_file.length());
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

	/**
	 * Prints the usage.
	 */
	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}

}

class TTPSend implements Runnable {
	private static final int HEADER_SIZE = 9;
	private static final byte ACK = 8;
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
			if(data.length == HEADER_SIZE)
			{
				if(data[8] == ACK)
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
