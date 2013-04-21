/*
 * A sample server that uses DatagramService
 */
package applications;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import services.DatagramService;
import services.TTPServer;
import services.TTPService;
import datatypes.Datagram;

public class server {

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

	//	Datagram datagram;

		byte[] received_byte_array = null;
		while(true) {
			received_byte_array = ts.receive_data("4444");
		//	received_byte_array = ts.receive_data();
			System.out.println("received data" + received_byte_array.toString());
		//	System.out.println("received" + received_byte_array);
		//	System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData().toString());
		//	reassemble.add(datagram.getData().toString());
		/*	Datagram ack = new Datagram();
			ack.setSrcaddr(datagram.getDstaddr());
			
			ack.setSrcport(datagram.getDstport());
			ack.setDstaddr(datagram.getSrcaddr());
			ack.setDstport(datagram.getSrcport());
			ack.setData("ACK");
			ds.sendDatagram(ack);*/
		}
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}
}
