package applications;

import java.io.IOException;
import services.TTPclient;

public class Ftpclient {
	private static TTPclient ts;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		if(args.length != 2) {
			printUsage();
		}
		
		System.out.println("Starting FTP client ...");
		int port = Integer.parseInt(args[0]);
		String filename = "hi.txt";
		ts = new TTPclient();
		ts.connection_open(String.valueOf((short)Integer.parseInt(args[1])), String.valueOf((short)port), "127.0.0.1", "127.0.0.1");
	//	Thread.sleep(1000);
		byte[] received_byte_array = ts.receive_data(String.valueOf((short)port));
		System.out.println("Received " + received_byte_array + "Flag=" + received_byte_array[4]);
		ts.send_file_name(filename,String.valueOf((short)Integer.parseInt(args[1])), String.valueOf((short)port), "127.0.0.1", "127.0.0.1");
		
	//	byte[] received_byte_array = ts.receive_data(String.valueOf((short)port));

	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
