package applications;

import java.io.IOException;

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
			    System.out.println(received_file);
			}
			System.out.println("received data" + received_byte_array.toString());

		}
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}

}
