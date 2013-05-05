package services;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import datatypes.Datagram;

// TODO: Auto-generated Javadoc

public class TTPclient {
	private int isn_client;
	private int last_ack;
	private int seq_num = 0;
	private int ack = 0;
	private int open=0;
	private char flag = 'S';
	public List<Byte> reassembled_file = new ArrayList<Byte>();
	public List<Byte> reassembled_file_name = new ArrayList<Byte>();
	public byte [] md5_value = new byte[16];
	boolean value = false;
	String dest_port = "4444";
	String src_port = "2222";
	String src_ip = "127.0.0.1";
	String dest_ip = "127.0.0.1";
	private static int MAX_RANDOM_NUMBER = 40000;
	private static int HEADER_SIZE = 9;
	private static int SYN_ACK = 9;
	private static int MD5 = 64;
	private static int FILENAME = 32;
	private static int DATA = 16;
	private static int LAST_PACKET = 2;
	public TTPclient(){

	}

	private static DatagramService ds;

	/**
	 * Connection_open.
	 *
	 * @param dest_port the dest_port
	 * @param src_port the src_port
	 * @param src_ip the src_ip
	 * @param dest_ip the dest_ip
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void connection_open(String dest_port, String src_port, String src_ip, String dest_ip) throws IOException {
		ds = new DatagramService(Integer.parseInt(src_port), 10);
		Random r = new Random();
		isn_client = r.nextInt(MAX_RANDOM_NUMBER);
		byte[] header = new byte[HEADER_SIZE];
		header = create_header(isn_client,0,'S');	
		Datagram datagram = new Datagram();
		datagram.setData(header);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		ds.sendDatagram(datagram);		
	}

	/**
	 * Send_file_name.
	 *
	 * @param filename the filename
	 * @param dest_port the dest_port
	 * @param src_port the src_port
	 * @param src_ip the src_ip
	 * @param dest_ip the dest_ip
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 * @throws InterruptedException the interrupted exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public String send_file_name(String filename, String dest_port, String src_port, String src_ip, String dest_ip) throws IOException, ClassNotFoundException, InterruptedException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		/*
		 * Set the D flag to indicate that client is sending the filename
		 */
		byte[] header = new byte[HEADER_SIZE];
		byte[] filename_byte_array = filename.getBytes();
		byte[] combined_filename = new byte[header.length + filename_byte_array.length];
		header = create_header(isn_client,0,'E');
		System.arraycopy(header, 0, combined_filename, 0, header.length);
		System.arraycopy(filename_byte_array, 0, combined_filename, header.length, filename_byte_array.length);	

		//sends the filename
		send_data(combined_filename, dest_port, src_port, src_ip, dest_ip);
		// call receive file to receive the file
		String sendingfile = receive_file(src_port);
		return sendingfile;
	}

	/**
	 * Send_data.
	 *
	 * @param received_data the received_data
	 * @param dest_port the dest_port
	 * @param src_port the src_port
	 * @param src_ip the src_ip
	 * @param dest_ip the dest_ip
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void send_data(byte[] received_data, String dest_port, String src_port, String src_ip, String dest_ip) throws IOException	{

		Datagram datagram = new Datagram();
		datagram.setData(received_data);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		short checksum = calculate_checksum(datagram);
		datagram.setChecksum(checksum);
		ds.sendDatagram(datagram);
	}

	/**
	 * Calculate_checksum.
	 *
	 * @param datagram the datagram
	 * @return the short
	 */
	private short calculate_checksum(Datagram datagram) {

		// TODO Auto-generated method stub
		byte[] buffer = (byte[])datagram.getData();
		int length = buffer.length;
		int i = 0;
		int sum = 0;
		while (length > 0) {
			sum += (buffer[i++]&0xff) << 8;
			if ((--length)==0) break;
			sum += (buffer[i++]&0xff);
			--length;
		}
		return (short) ((~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF);
	}

	/**
	 * Receive_data.
	 *
	 * @param port the port
	 * @return the byte[]
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public byte[] receive_data(String port) throws ClassNotFoundException, IOException, InterruptedException {

		Datagram datagram = ds.receiveDatagram();
		byte[] data = (byte[])datagram.getData();	//data from the datagram

		dest_ip = datagram.getSrcaddr();
		src_ip = datagram.getDstaddr();
		dest_port = String.valueOf(datagram.getSrcport());
		src_port = String.valueOf(datagram.getDstport());
		short received_checksum = datagram.getChecksum();
		short cal_checksum = calculate_checksum(datagram);
		byte[] header = new byte[HEADER_SIZE];
		
		if(received_checksum != cal_checksum)
		{
			//byte array to store the flag
			header = create_header(isn_client, last_ack, 'A'); // create a header with both syn and ack set
			send_data(header, dest_port, src_port, src_ip, dest_ip);
		}

		if(data[8] == SYN_ACK)
		{	 //byte array to store the flag
			int received_seq_num = byte_to_int(header, 3, 2, 1, 0);
			isn_client++;
			header = create_header(isn_client+1, received_seq_num + 1, 'A'); // create a header with only ack set			
			send_data(header, dest_port, src_port, src_ip, dest_ip);	
			value = true;
		}
		if(data[8] == DATA)
		{
			int i;
			int j = 0;
			byte[] new_data = new byte[(data.length)-8];
			/*
			 * converting the filename byte array into string
			 */
			for(i=HEADER_SIZE;i<data.length;i++,j++)
			{
				reassembled_file.add(data[i]);
				new_data[j] = data[i];
			}
			/*
			 * send ack for the received packet
			 * create a header with only ack set
			 */
			isn_client++;
			//byte array to store the flag
			int received_seq_num = byte_to_int(data, 3, 2, 1, 0);
			System.out.println("&*&*&*&*&*&seq number recieved from server is : "+ received_seq_num);
			header = create_header(isn_client, received_seq_num, 'A');
			send_data(header, dest_port, src_port, src_ip, dest_ip);
		}
		if(data[8] == LAST_PACKET)
		{
			int i;
			int j = 0;
			byte[] new_data = new byte[(data.length)-8];

			for(i=HEADER_SIZE;i<data.length;i++,j++)
			{
				new_data[j] = data[i];
				reassembled_file.add(data[i]);
			}
			isn_client++;
	//byte array to store the flag
			int received_seq_num = byte_to_int(data, 3, 2, 1, 0);
			System.out.println("&*&*&*&*&*&seq number recieved from server is : "+ received_seq_num);
			header = create_header(isn_client, received_seq_num, 'A');
			send_data(header, dest_port, src_port, src_ip, dest_ip);
			return data;
		}
		if(data[8] == FILENAME)
		{
			int i;
			int j = 0;
			byte[] new_data = new byte[(data.length)-8];
			/*
			 * converting the filename byte array into string
			 */
			for(i=HEADER_SIZE;i<data.length;i++,j++)
			{
				new_data[j] = data[i];
				reassembled_file_name.add(data[i]);
			}
			System.out.println(data.toString());
			return data;
		}
		else if(data[8] == MD5)
		{
			System.out.println("reached client fin++++++++++++++++++++++++++");
			byte[] received_data = (byte[]) datagram.getData();
			byte[] data_header = new byte[HEADER_SIZE];
			int received_seq_num = byte_to_int(received_data, 3, 2, 1, 0);
			data_header = create_header(isn_client, received_seq_num + 1, 'V');
			send_data(data_header, dest_port, src_port, src_ip, dest_ip);
		}
		else if(data[8] == 5)
		{
			System.out.println("connection closed at client");
		}
		System.out.println(data.toString());
		return data;
	}

	private int byte_to_int(byte[] header, int i, int j, int k, int l) {
		// TODO Auto-generated method stub
		int value = header[i] << 24 | (header[j] & 0xFF) << 16 | (header[k] & 0xFF) << 8 | (header[l] & 0xFF);
		return value;
	}

	/**
	 * Receive_file.
	 *
	 * @param src_port the src_port
	 * @return the string
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	private String receive_file(String src_port) throws ClassNotFoundException, IOException, InterruptedException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		byte[] temp = receive_data(src_port);
		List<Byte> reassembled_file = new ArrayList<Byte>();
		int i = 0;
		/*
		 * Loop till the Finish flag is set
		 */
		int received_seq =byte_to_int(temp, 3, 2, 1, 0);
		last_ack = received_seq;
		while(temp[8] != LAST_PACKET && temp[8] != MD5)
		{
			received_seq =byte_to_int(temp, 3, 2, 1, 0);
			System.out.println("I got the data with seq number :" + received_seq);
			if(temp[8]==DATA && received_seq == last_ack)
			{
				for(int l = HEADER_SIZE; l<temp.length;l++)
				{
					reassembled_file.add(temp[l]);
				}
				last_ack++;
			}

			//listening for the next data packet
			temp = receive_data(src_port);
		}
		/*
		 * Add the byte that has Finish flag set
		 */
		received_seq =byte_to_int(temp, 3, 2, 1, 0);
		if(temp[8]==LAST_PACKET && received_seq == last_ack)
		{
			int l=0;
			for(l = HEADER_SIZE; l<temp.length;l++)
			{
				reassembled_file.add(temp[l]);
			}
			temp = receive_data(src_port);
			last_ack++;
		}
		while(temp[8]!=MD5)
		{
			temp = receive_data(src_port);
		}
		if(temp[8] == MD5)
		{
			int j = 0;
			for(i=HEADER_SIZE;i<temp.length;i++,j++)
			{
				md5_value[j] = temp[i];
			}
			//	System.out.println("*********************Recieved MD5 = "+md5_value.toString());
		}
		/*
		 * Convert the List to a Byte array 
		 */
		i=0;
		byte[] reassembled_file_byte_array = new byte[reassembled_file.size()];
		for(Byte current : reassembled_file)
		{
			reassembled_file_byte_array[i] = current;
			i++;
		}
		String reassembled_string = new String(reassembled_file_byte_array);
		//	System.out.println("$$$$$$$$$$$received file: "+ reassembled_string);
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest_c = md.digest(reassembled_file_byte_array);
		if(Arrays.equals(md5_value, thedigest_c))
		{
			System.out.println("Yes they are same");

		}
		else
		{
			System.out.println("No they are not same");
		}
		//		System.out.println("Reassembled string -> " + reassembled_string);
		return reassembled_string;
	}

	/**
	 * Connection_close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void connection_close() throws IOException { 
		byte[] header = new byte[9]; //byte array to store the flag
		header = create_header(isn_client, last_ack, 'C'); // create a header with only ack set
		send_data(header, dest_port, src_port, src_ip, dest_ip);
	}

	/**
	 * Create_header.
	 *
	 * @param seq_num the seq_num
	 * @param ack the ack
	 * @param flag the flag
	 * @return the byte[]
	 */
	public byte[] create_header(int seq_num, int ack, char flag) {
		byte[] header = new byte[HEADER_SIZE];
		header[0] = (byte)(seq_num & 0xFF);
		header[1] = (byte)((seq_num >> 8) & 0xFF);
		header[2] = (byte)((seq_num >> 16) & 0xFF);
		header[3] = (byte)((seq_num >> 24) & 0xFF);
		header[4] = (byte)(ack & 0xFF);
		header[5] = (byte)((ack >> 8) & 0xFF);
		header[6] = (byte)((ack >> 16) & 0xFF);
		header[7] = (byte)((ack >> 24) & 0xFF);
		switch(flag)
		{
		case 'S': header[8] = 0x01; 	
		break;
		case 'D': header[8] = 0x10;
		break;
		case 'A': header[8] = 0x08;
		break;
		case 'B': header[8] = 0x09;
		break;
		case 'F': header[8] = 0x02;
		break;
		case 'E': header[8] = 0x20;
		break;
		case 'C': header[8] = 0x04;
		break;
		case 'V': header[8] = 0x05;
		break;
		default: break;
		}	
		return header;
	}

	/**
	 * Give_open_var.
	 *
	 * @return the int
	 */
	public int give_open_var()
	{
		return open;
	}

	/**
	 * Create_payload.
	 *
	 * @param payload the payload
	 * @return the byte[]
	 */
	public byte[] create_payload(String payload) {
		byte[] payload_byte = payload.getBytes(); 	    	
		return payload_byte;
	}

}
