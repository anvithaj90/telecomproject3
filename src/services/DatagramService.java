/*
 *  A Stub that provides datagram send and receive functionality
 *  
 *  Feel free to modify this file to simulate network errors such as packet
 *  drops, duplication, corruption etc. But for grading purposes we will
 *  replace this file with out own version. So DO NOT make any changes to the
 *  function prototypes
 */
package services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import datatypes.Datagram;

public class DatagramService {

	private int port;
	private int verbose;
	private DatagramSocket socket;
	private int count=0;

	public DatagramService(int port, int verbose) throws SocketException {
		super();
		this.port = port;
		this.verbose = verbose;

		socket = new DatagramSocket(port);
	}

	public void sendDatagram(Datagram datagram) throws IOException {
		count++;

		ByteArrayOutputStream bStream = new ByteArrayOutputStream(1500);
		ObjectOutputStream oStream = new ObjectOutputStream(bStream);
		oStream.writeObject(datagram);
		oStream.flush();

		byte[] data = bStream.toByteArray();
		InetAddress IPAddress = InetAddress.getByName(datagram.getDstaddr());
		DatagramPacket packet = new DatagramPacket(data, data.length,
				IPAddress, datagram.getDstport());
		/*if(count == 1)
		{
			//drop first syn packet this is not handled by our code so remove when testing
		}
		else*/ if(count%9==0) {
			// Delayed Packets
			int delay = 10;   //delay in milliseconds
			Delayed(packet,delay);
		}  else if(count%7==0) {
			// Duplicate Packets
			Duplicates(packet);
		} else if (count%11==0) {
			//Drop Packets
		} else {
			socket.send(packet);
		}
	}

	public Datagram receiveDatagram() throws IOException,
	ClassNotFoundException {

		byte[] buf = new byte[1500];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		socket.receive(packet);

		ByteArrayInputStream bStream = new ByteArrayInputStream(
				packet.getData());
		ObjectInputStream oStream = new ObjectInputStream(bStream);
		Datagram datagram = (Datagram) oStream.readObject();

		return datagram;
	}
	private void Delayed(DatagramPacket packet,int delay) throws IOException {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		//	e.printStackTrace();
		}
		socket.send(packet);
	}

	private void Duplicates(DatagramPacket packet) throws IOException {
		int number_of_duplicates = 5;
		for(int i = 0; i<number_of_duplicates; i++)
			socket.send(packet);
	}

}