/*
 * Authors: 
 * Anvitha Jaishankar (anvithaj@cmu.edu)
 * Ruchir Patwa (rpatwa@cmu.edu)
 */
package services;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.swing.Timer;


import datatypes.Datagram;

// TODO: Auto-generated Javadoc
public class TTPclient {
	private int isn_client;
	private int last_ack;
	private int open=0;
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
	private static int EOF = 2;
	private static int FIN_CLOSE = 4;
	private static int FIN_ACK = 5;
	private static int FLAG_BYTE = 8;
	private static int MAX_PAYLOAD = 1285;
	private static DatagramService ds;
	private int timer_value = 8000;
	public TTPclient(int timer_val){
		timer_value = timer_val*1000;
		System.out.println("timer value is :" + timer_value);
		timer.setDelay(timer_value);
	}

	ActionListener resendData = new ActionListener(){
		public void actionPerformed(ActionEvent event){
			byte[] header = new byte[HEADER_SIZE];
			header = create_header(isn_client, last_ack, 'A'); 		
			try {
				send_data(header, dest_port, src_port, src_ip, dest_ip);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			System.out.println("TTP CLient resends the data after timeout!");
		}
	};

	Timer timer = new Timer(timer_value,resendData);
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
		byte[] header = new byte[HEADER_SIZE];
		byte[] filename_byte_array = filename.getBytes();
		byte[] combined_filename = new byte[header.length + filename_byte_array.length];
		header = create_header(isn_client,0,'E');
		System.arraycopy(header, 0, combined_filename, 0, header.length);
		System.arraycopy(filename_byte_array, 0, combined_filename, header.length, filename_byte_array.length);	

		send_data(combined_filename, dest_port, src_port, src_ip, dest_ip);

		timer.start();
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
		byte[] data = (byte[])datagram.getData();	

		dest_ip = datagram.getSrcaddr();
		src_ip = datagram.getDstaddr();
		dest_port = String.valueOf(datagram.getSrcport());
		src_port = String.valueOf(datagram.getDstport());
		short received_checksum = datagram.getChecksum();
		short cal_checksum = calculate_checksum(datagram);
		byte[] header = new byte[HEADER_SIZE];

		if(received_checksum != cal_checksum)
		{
			header = create_header(isn_client, last_ack, 'A'); 
			send_data(header, dest_port, src_port, src_ip, dest_ip);
		}

		if(data[FLAG_BYTE] == SYN_ACK)
		{	 
			int received_seq_num = byte_to_int(header, 3, 2, 1, 0);
			isn_client++;
			header = create_header(isn_client+1, received_seq_num + 1, 'A'); 			
			send_data(header, dest_port, src_port, src_ip, dest_ip);	
			value = true;
		}
		if(data[FLAG_BYTE] == DATA)
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
			isn_client++;
		
			int received_seq_num = byte_to_int(data, 3, 2, 1, 0);
			System.out.println("Sequence number recieved from server is : "+ received_seq_num);
			header = create_header(isn_client, received_seq_num, 'A');
			send_data(header, dest_port, src_port, src_ip, dest_ip);
		}
		if(data[FLAG_BYTE] == EOF)
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
			System.out.println("Sequence number recieved from server is : "+ received_seq_num);
			header = create_header(isn_client, received_seq_num, 'A');
			send_data(header, dest_port, src_port, src_ip, dest_ip);
			return data;
		}
		if(data[FLAG_BYTE] == FILENAME)
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
		else if(data[FLAG_BYTE] == FIN_CLOSE)
		{
			System.out.println("Client received fin");
			byte[] received_data = (byte[]) datagram.getData();
			byte[] data_header = new byte[HEADER_SIZE];
			int received_seq_num = byte_to_int(received_data, 3, 2, 1, 0);
			data_header = create_header(isn_client, received_seq_num + 1, 'V');
			send_data(data_header, dest_port, src_port, src_ip, dest_ip);
		}
		else if(data[FLAG_BYTE] == FIN_ACK)
		{

			System.out.println("Client received FIN_ACK, connection closed at client");
			while(data[FLAG_BYTE] != FIN_CLOSE)
			{
				data=receive_data(src_port);
			}
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

		int received_seq =byte_to_int(temp, 3, 2, 1, 0);
		last_ack = received_seq;
		while(temp[FLAG_BYTE] != EOF && temp[FLAG_BYTE] != MD5)
		{
			received_seq =byte_to_int(temp, 3, 2, 1, 0);
			if(temp[FLAG_BYTE]==DATA && received_seq == last_ack)
			{
				timer.restart();
				for(int l = HEADER_SIZE; l<temp.length;l++)
				{
					reassembled_file.add(temp[l]);
				}
				last_ack++;
			}
			temp = receive_data(src_port);
		}

		received_seq =byte_to_int(temp, 3, 2, 1, 0);
		if(temp[FLAG_BYTE]==EOF && received_seq == last_ack)
		{
			int l=0;
			timer.restart();
			for(l = HEADER_SIZE; l<temp.length;l++)
			{
				reassembled_file.add(temp[l]);
			}
			temp = receive_data(src_port);
			last_ack++;
		}
		while(temp[FLAG_BYTE]!=MD5)
		{
			temp = receive_data(src_port);
		}
		if(temp[FLAG_BYTE] == MD5)
		{
			int j = 0;
			timer.stop();
			for(i=HEADER_SIZE;i<temp.length;i++,j++)
			{
				md5_value[j] = temp[i];
			}
		}

		i=0;
		byte[] reassembled_file_byte_array = new byte[reassembled_file.size()];
		for(Byte current : reassembled_file)
		{
			reassembled_file_byte_array[i] = current;
			i++;
		}
		String reassembled_string = new String(reassembled_file_byte_array);
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest_c = md.digest(reassembled_file_byte_array);
		if(Arrays.equals(md5_value, thedigest_c))
		{
			System.out.println("Yes the MD5 values are the are same :)");
		}
		else
		{
			System.out.println("No the MD5 values are not same :( Please try again!!!");
		}
		last_ack++;
		return reassembled_string;
	}

	/**
	 * Connection_close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 */
	public void connection_close() throws IOException, ClassNotFoundException, InterruptedException { 
		byte[] header = new byte[HEADER_SIZE]; //byte array to store the flag
		System.out.println("Client sends FIN");
		header = create_header(isn_client, last_ack, 'C'); // create a header with only ack set
		send_data(header, dest_port, src_port, src_ip, dest_ip);
		byte[] temp = new byte[MAX_PAYLOAD+HEADER_SIZE];
		temp =receive_data(src_port);
		while(temp[FLAG_BYTE]!=FIN_ACK)
		{
			temp = receive_data(src_port);
		}

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
		case 'S': header[FLAG_BYTE] = 0x01; /* SYN Flag */	
		break;
		case 'D': header[FLAG_BYTE] = 0x10; /* DATA Flag */	
		break;
		case 'A': header[FLAG_BYTE] = 0x08; /*ACK Flag */	
		break;
		case 'B': header[FLAG_BYTE] = 0x09; /*SYN_ACK Flag */
		break;
		case 'F': header[FLAG_BYTE] = 0x02; /*EOF Flag*/
		break;
		case 'E': header[FLAG_BYTE] = 0x20; /*FILENAME Flag */
		break;
		case 'C': header[FLAG_BYTE] = 0x04; /*FIN_CLOSE Flag */
		break;
		case 'V': header[FLAG_BYTE] = 0x05; /*FIN_ACK Flag */
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
