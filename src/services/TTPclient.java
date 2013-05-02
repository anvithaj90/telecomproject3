package services;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import datatypes.Datagram;

public class TTPclient {
	private int isn_client;
	private int last_ack;
	public List<Byte> reassembled_file = new ArrayList<Byte>();
	public byte [] md5_value = new byte[16];
	public TTPclient(){

	}
	boolean value = false;
	int seq_num = 0;
	int ack = 0;
	char flag = 'S';
	int open=0;
	
	private static DatagramService ds;
	
	public void connection_open(String dest_port, String src_port, String src_ip, String dest_ip) throws IOException {
		ds = new DatagramService(Integer.parseInt(src_port), 10);
		Random r = new Random();
		isn_client = r.nextInt(40000);
		byte[] header = new byte[9];
		header = create_header(isn_client,0,'S');	
		Datagram datagram = new Datagram();
		datagram.setData(header);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
//		System.out.println(datagram.toString());
		ds.sendDatagram(datagram);		
	}
	public String send_file_name(String filename, String dest_port, String src_port, String src_ip, String dest_ip) throws IOException, ClassNotFoundException, InterruptedException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		/*
		 * Set the D flag to indicate that client is sending the filename
		 */
		byte[] header = new byte[9];
		byte[] filename_byte_array = filename.getBytes();
		byte[] combined_filename = new byte[header.length + filename_byte_array.length];
		header = create_header(isn_client,0,'E');
		System.arraycopy(header, 0, combined_filename, 0, header.length);
		System.arraycopy(filename_byte_array, 0, combined_filename, header.length, filename_byte_array.length);	
		
		//sends the filename
		send_data(combined_filename, dest_port, src_port, src_ip, dest_ip);
	//	System.out.println("sent file name");
		
		// call receive file to receive the file
		String sendingfile = receive_file(src_port);
		return sendingfile;
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
//		System.out.println("sent checksum = "+checksum+"flag = "+received_data[4]);
//		System.out.println(datagram.toString());
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
		short checksum;
		checksum = datagram.getChecksum();
//		System.out.println("recieved "+checksum + "flag = "+data[4]);
		if(data[8] == 9)
		{
			byte[] header = new byte[8]; //byte array to store the flag
			int received_seq_num = header[3] << 24 | (header[2] & 0xFF) << 16 | (header[1] & 0xFF) << 8 | (header[0] & 0xFF);
			System.out.println("&*&**&*&*&*seq number at client is: "+ isn_client);
			System.out.println("&*&*&*&*&*&seq number recieved from server is : "+ received_seq_num);
			isn_client++;
			//last_ack = data[7] << 24 | (data[6] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[4] & 0xFF);
			header = create_header(isn_client+1, received_seq_num + 1, 'A'); // create a header with only ack set
		//	open+=1;
			String dest_port;
			String src_port;
			String src_ip;
			String dest_ip;
			dest_ip = datagram.getSrcaddr();
			src_ip = datagram.getDstaddr();
			dest_port = String.valueOf(datagram.getSrcport());
			src_port = String.valueOf(datagram.getDstport());
			send_data(header, dest_port, src_port, src_ip, dest_ip);	
			value = true;
		}
		if(data[8] == 16)
		{
			String dest_port;
			String src_port;
			String src_ip;
			String dest_ip;
			dest_ip = datagram.getSrcaddr();
			src_ip = datagram.getDstaddr();
			dest_port = String.valueOf(datagram.getSrcport());
			src_port = String.valueOf(datagram.getDstport());
			int i;
			int j = 0;
			byte[] new_data = new byte[(data.length)-8];
			/*
			 * converting the filename byte array into string
			 */
			for(i=9;i<data.length;i++,j++)
			{
				reassembled_file.add(data[i]);
				new_data[j] = data[i];
			}
			String received_file = new String(new_data);
	//	    System.out.println("received file data:" + received_file);
			/*
			 * send ack for the received packet
			 * create a header with only ack set
			 */
		    isn_client++;
		    byte[] header = new byte[9]; //byte array to store the flag
			int received_seq_num = data[3] << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
			//last_ack = data[7] << 24 | (data[6] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[4] & 0xFF);
			System.out.println("&*&**&*&*&*seq number at client is: "+ isn_client);
			System.out.println("&*&*&*&*&*&seq number recieved from server is : "+ received_seq_num);
			System.out.println("isn client value is: "+ isn_client);
			header = create_header(isn_client, received_seq_num, 'A');
	//		isn_client++;
			send_data(header, dest_port, src_port, src_ip, dest_ip);
		}
		if(data[8] == 4)
		{
			int i;
			int j = 0;
			byte[] new_data = new byte[(data.length)-8];
			/*
			 * converting the filename byte array into string
			 */
			for(i=9;i<data.length;i++,j++)
			{
				new_data[j] = data[i];
				reassembled_file.add(data[i]);
			}
			return data;
		}
		if(data[8] == 32)
		{
			int i;
			int j = 0;
			byte[] new_data = new byte[(data.length)-8];
			/*
			 * converting the filename byte array into string
			 */
			for(i=9;i<data.length;i++,j++)
			{
				new_data[j] = data[i];
				reassembled_file.add(data[i]);
			}
	//		System.out.println(data.toString());
			return data;
		}
		
	
	//	System.out.println(data.toString());
		return data;
	}
	
	private String receive_file(String src_port) throws ClassNotFoundException, IOException, InterruptedException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		byte[] temp = receive_data(src_port);
		List<Byte> reassembled_file = new ArrayList<Byte>();
		int i = 0;
		/*
		 * Loop till the Finish flag is set
		 */
		int received_seq =temp[3] << 24 | (temp[2] & 0xFF) << 16 | (temp[1] & 0xFF) << 8 | (temp[0] & 0xFF);
		last_ack = received_seq;
		while(temp[8] != 2 && temp[8] != 64)
		{
			received_seq =temp[3] << 24 | (temp[2] & 0xFF) << 16 | (temp[1] & 0xFF) << 8 | (temp[0] & 0xFF);
			
			if(temp[8]==16 && received_seq == last_ack)
			{
				int l=0;
				
				for(l = 9; l<temp.length;l++)
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
		received_seq =temp[3] << 24 | (temp[2] & 0xFF) << 16 | (temp[1] & 0xFF) << 8 | (temp[0] & 0xFF);
		if(temp[8]==2 && received_seq == last_ack)
		{
			int l=0;
			for(l = 9; l<temp.length;l++)
			{
				reassembled_file.add(temp[l]);
			}
			temp = receive_data(src_port);
			last_ack++;
		}
		
	
		while(temp[8]!=64)
		{
			temp = receive_data(src_port);
		}
		if(temp[8] == 64)
		{
			int j = 0;
			for(i=9;i<temp.length;i++,j++)
			{
				md5_value[j] = temp[i];
			}
			System.out.println("*********************Recieved MD5 = "+md5_value.toString());
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
		System.out.println("$$$$$$$$$$$received file: "+ reassembled_string);
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(reassembled_file_byte_array);
		if(Arrays.equals(md5_value, thedigest))
		{
			System.out.println("Yes they are same");
			
		}
		else
		{
			System.out.println("No they are not and Java sucks");
		}
//		System.out.println("Reassembled string -> " + reassembled_string);
		return reassembled_string;
	}

	public void connection_close() { 
		
	}
	
	public byte[] create_header(int seq_num, int ack, char flag) {
		byte[] header = new byte[9];
		header[0] = (byte)(seq_num & 0xFF);
		header[1] = (byte)((seq_num >> 8) & 0xFF);
		header[2] = (byte)((seq_num >> 16) & 0xFF);
		header[3] = (byte)((seq_num >> 24) & 0xFF);
		header[4] = (byte)(ack & 0xFF);
		header[5] = (byte)((ack >> 8) & 0xFF);
		header[6] = (byte)((ack >> 16) & 0xFF);
		header[7] = (byte)((ack >> 24) & 0xFF);
	//S == SYN
		if(flag == 'S')
			header[8] = 0x01;
		//start of file
		if(flag == 'D')
			header[8] = 0x10;
		// F == FIN end of file
		if(flag == 'A')
			header[8] = 0x08;
		if(flag == 'B')
			header[8] = 0x09;
		else if(flag == 'F')
			header[8] = 0x02;
		//file name
		else if(flag == 'E')
			header[8] = 0x20;
		//C == close connection
		else if(flag == 'C')
			header[8] = 0x04;
		return header;
		
	}
	public int give_open_var()
	{
		return open;
	}
	public byte[] create_payload(String payload) {
		byte[] payload_byte = payload.getBytes(); 	    	
		return payload_byte;
	}
	
}
