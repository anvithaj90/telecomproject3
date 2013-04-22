package services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import datatypes.Datagram;

public class TTPclient {
	private int isn_client;
	public TTPclient(){

	}
	
	int seq_num = 0;
	int ack = 0;
	char flag = 'S';
	
	private static DatagramService ds;
	
	public void connection_open(String dest_port, String src_port, String src_ip, String dest_ip) throws IOException {
		ds = new DatagramService(Integer.parseInt(src_port), 10);
		Random r = new Random();
		isn_client = r.nextInt(65535);
		byte[] header = new byte[5];
		header = create_header(isn_client,0,'S');	
		Datagram datagram = new Datagram();
		datagram.setData(header);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		System.out.println(datagram.toString());
		ds.sendDatagram(datagram);
		
	}
	public void send_data(byte[] received_data, String dest_port, String src_port, String src_ip, String dest_ip) throws IOException	{

		Datagram datagram = new Datagram();
		datagram.setData(received_data);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		short checksum = calculate_checksum(datagram);
		datagram.setChecksum(checksum);
		System.out.println("sent"+checksum);
		System.out.println(datagram.toString());
		ds.sendDatagram(datagram);
		}
	
	private short calculate_checksum(Datagram datagram) {
		// TODO Auto-generated method stub
		Checksum checksum = new CRC32();  
		byte[] d = (byte [])datagram.getData();
		checksum.update(d,0,(int)d.length);   
		short value = (short) checksum.getValue(); //this is the real checksum
		return value;		
	}
	public byte[] receive_data(String port) throws ClassNotFoundException, IOException, InterruptedException {
		
		Datagram datagram = ds.receiveDatagram();

		byte[] data = (byte[])datagram.getData();	//data from the datagram
		byte[] data1 = new byte[50]; //byte array for data

		short checksum;
		checksum = datagram.getChecksum();
		System.out.println("recieved "+checksum);
		if(data[4] == 9)
		{
			byte[] header = new byte[5]; //byte array to store the flag
			int received_seq_num = header[0] << 8 | header[1];
			header = create_header(isn_client, received_seq_num + 1, 'A'); // create a header with only ack set
			String dest_port;
			String src_port;
			String src_ip;
			String dest_ip;
			dest_ip = datagram.getSrcaddr();
			src_ip = datagram.getDstaddr();
			dest_port = String.valueOf(datagram.getSrcport());
			src_port = String.valueOf(datagram.getDstport());
			send_data(header, dest_port, src_port, src_ip, dest_ip);	
			receive_file(src_port);
		}
	
		System.out.println(data.toString());
		return data;
	}
	
	private void receive_file(String src_port) throws ClassNotFoundException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		byte[] temp = receive_data(src_port);
		List<Byte> reassembled_file = new ArrayList<Byte>();
		int i = 0;
		/*
		 * Loop till the Finish flag is set
		 */
		while(temp[4] != 2)
		{
			if(temp[4]==16)
			{
				reassembled_file.add(temp[5]);
				reassembled_file.add(temp[6]);
			}
			temp = receive_data(src_port);
		}
		/*
		 * Add the byte that has Finish flag set
		 */
		if(temp[4]==2)
		{
			reassembled_file.add(temp[5]);
			reassembled_file.add(temp[6]);
		}
		/*
		 * Convert the List to a Byte array 
		 */
		byte[] reassembled_file_byte_array = new byte[reassembled_file.size()];
		for(Byte current : reassembled_file)
		{
			reassembled_file_byte_array[i] = current;
			i++;
		}
		
		/*
		 * Print the byte array 
		 */
		for(i=0;i<reassembled_file_byte_array.length;i+=2)
		{
			System.out.print(Character.toString((char)reassembled_file_byte_array[i]) + Character.toString((char)reassembled_file_byte_array[i+1]));
		}

	}
	
	public void connection_close() { 
		
	}
	
	public byte[] create_header(int seq_num, int ack, char flag) {
		byte[] header = new byte[5];
		header[0] = (byte)(seq_num & 0xFF);
		header[1] = (byte)((seq_num >> 8) & 0xFF);
		header[2] = (byte)(ack & 0xFF);
		header[3] = (byte)((ack >> 8) & 0xFF);
	//S == SYN
		if(flag == 'S')
			header[4] = 0x01;
		//start of file
		if(flag == 'D')
			header[4] = 0x10;
		// F == FIN end of file
		if(flag == 'A')
			header[4] = 0x08;
		if(flag == 'B')
			header[4] = 0x09;
		else if(flag == 'F')
			header[4] = 0x02;
		//C == close connection
		else if(flag == 'C')
			header[4] = 0x04;
		return header;
		
	}
	
	public byte[] create_payload(String payload) {
		byte[] payload_byte = payload.getBytes(); 	    	
		return payload_byte;
	}
	
}
