/*
 * A sample client that uses DatagramService
 */

package applications;

import java.io.IOException;
import java.net.SocketException;

import services.DatagramService;
import services.TTPService;
import services.TTPclient;
import datatypes.Datagram;

public class client {

	private static DatagramService ds;
	private static TTPclient ts;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if(args.length != 2) {
			printUsage();
		}
		
		System.out.println("Starting client ...");
		
		int port = Integer.parseInt(args[0]);
	//	ds = new DatagramService(port, 10);
		ts = new TTPclient();
		
		Datagram datagram = new Datagram();
		//datagram.setData("Hello World!");
		datagram.setSrcaddr("127.0.0.1");
		datagram.setDstaddr("127.0.0.1");
		datagram.setDstport((short)Integer.parseInt(args[1]));
		datagram.setSrcport((short)port);
		
		String sourceip;
		String destinationip;
		short source_p;
		short destination_p;
		Object data;
		short checksum;
		
		sourceip = datagram.getSrcaddr();
		destinationip = datagram.getDstaddr();
		source_p = datagram.getSrcport();
		destination_p = datagram.getDstport();
		data = datagram.getData();
		String destination_p1 = String.valueOf(destination_p);
		String source_p1 = String.valueOf(source_p);
//		checksum = TTPService.checksum(sourceip, destinationip, source_p, destination_p, data);
		
	//	ts.send_data("Hello World!",destination_p1, source_p1, sourceip, destinationip);
		ts.connection_open(destination_p1, source_p1, sourceip, destinationip);
		//ds.sendDatagram(datagram);
		//System.out.println("Sent datagram");
		byte[] received_byte_array = ts.receive_data(source_p1.toString());
		
		
		//System.out.println("Received " + datagram.getData());
		System.out.println("Received " + received_byte_array);
//		System.out.println("Received " + received_byte_array.toString());
	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
