package applications;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import services.TTPclient;

public class Ftpclient {
	private static TTPclient ts;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, NumberFormatException, NoSuchAlgorithmException {
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
		System.out.println("Received at client" + received_byte_array + "Flag=" + received_byte_array[4]);
		String receivedfile = ts.send_file_name(filename,String.valueOf((short)Integer.parseInt(args[1])), String.valueOf((short)port), "127.0.0.1", "127.0.0.1");
		System.out.println("received at client->" + receivedfile);
		byte dataToWrite[] = receivedfile.getBytes();
		String client_file_path = new String("clientfiles/" + filename);
		FileOutputStream out = new FileOutputStream(client_file_path);
		out.write(dataToWrite);
		out.close();
	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
