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

import datatypes.Datagram;

// TODO: Auto-generated Javadoc
/**
 * The Class DatagramService.
 */
public class DatagramService {

	/** The port. */
	private int port;
	
	/** The verbose. */
	private int verbose;
	
	/** The socket. */
	private DatagramSocket socket;

	/**
	 * Instantiates a new datagram service.
	 *
	 * @param port the port
	 * @param verbose the verbose
	 * @throws SocketException the socket exception
	 */
	public DatagramService(int port, int verbose) throws SocketException {
		super();
		this.port = port;
		this.verbose = verbose;

		socket = new DatagramSocket(port);
	}

	/**
	 * Send datagram.
	 *
	 * @param datagram the datagram
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void sendDatagram(Datagram datagram) throws IOException {

		ByteArrayOutputStream bStream = new ByteArrayOutputStream(1500);
		ObjectOutputStream oStream = new ObjectOutputStream(bStream);
		oStream.writeObject(datagram);
		oStream.flush();

		// Create Datagram Packet
		byte[] data = bStream.toByteArray();
		InetAddress IPAddress = InetAddress.getByName(datagram.getDstaddr());
		DatagramPacket packet = new DatagramPacket(data, data.length,
				IPAddress, datagram.getDstport());

		// Send packet
		socket.send(packet);
	}

	/**
	 * Receive datagram.
	 *
	 * @return the datagram
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
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
}
