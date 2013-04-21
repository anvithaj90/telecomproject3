package services;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import datatypes.Datagram;

public class TTPService {
	private int isn1;
	private int isn2;
	public TTPService(){

	}
	String payload= "Hello World!";
	int seq_num = 0;
	int ack = 0;
	char flag = 'S';
	
	private static DatagramService ds;
	
	public void connection_open(String dest_port, String src_port, String src_ip, String dest_ip) throws IOException {
		ds = new DatagramService(Integer.parseInt(src_port), 10);
		Random r = new Random();
		isn1 = r.nextInt(65535);
		byte[] header = new byte[5];
	
		int ack = 0;
		header = create_header(isn1,ack,'S');
				
		Datagram datagram = new Datagram();
		datagram.setData(header);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		System.out.println(datagram.toString());
		ds.sendDatagram(datagram);
		
	}
	public void send_data(String received_data, String dest_port, String src_port, String src_ip, String dest_ip) throws IOException	{
		if(ds==null){
			ds = new DatagramService(Integer.parseInt(src_port), 10);
		}
		byte[] datas;
		//int i = 0;
		int j = 0;
	//	byte[] header = new byte[6];
	//	byte[] payload = new byte[50];
//		byte[] data = new byte[header.length + payload.length];
//		String data1 = "Hello World!";
//		for(int i=0;i<data1.length();i+=2){
	//		if(data1.length()>2){
			//	header = create_header(seq_num,ack,flag);
		//		payload = create_payload(data1.substring(i, i+2));
		//		payload = create_payload(received_data);
	//		}
	//	System.arraycopy(header, 0, data, 0, header.length);
	//	System.arraycopy(payload, 0, data, header.length, payload.length);
		//String datastring = new Arrays.toString(data);		
		Datagram datagram = new Datagram();
		datagram.setData(received_data);
		datagram.setSrcaddr(src_ip);
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		System.out.println(datagram.toString());
		ds.sendDatagram(datagram);
		}
//	}
	public byte[] receive_data() throws ClassNotFoundException, IOException {
		Datagram datagram = ds.receiveDatagram();
		String temp = null;
 		temp = (String) datagram.getData();
		byte[] data = temp.getBytes();	//data from the datagram
		byte[] data1 = new byte[50]; //byte array for data
		int i = 4;
		int j = 0;
		
		
		if(data1[4]==0x01)
		{
			/* Generate a random number for the other side and set the 
			 * flag to SYN and ACK i.e. B. Get the source and the destination IP
			 * and port addresses and call send_data to send the SYN-ACK packet.
			 *  Increment the syn value of the prev  and 
			 */
			Random r = new Random();
			isn2 = r.nextInt(65535) + 0;
			
			byte[] header = new byte[5]; //byte array to store the flag
			
			header = create_header(isn2, isn1+1, 'B'); // create a header with both syn and ack set
			
			String dest_port;
			String src_port;
			String src_ip;
			String dest_ip;
			dest_ip = datagram.getSrcaddr();
			src_ip = datagram.getDstaddr();
			dest_port = String.valueOf(datagram.getSrcport());
			src_port = String.valueOf(datagram.getDstport());
			send_data(header.toString(), dest_port, src_port, src_ip, dest_ip);
			
		}
		else if(data[4] == 0x09)
		{
			byte[] header = new byte[5]; //byte array to store the flag
			header = create_header(isn1+1, isn2+1, 'A'); // create a header with only ack set
			String dest_port;
			String src_port;
			String src_ip;
			String dest_ip;
			dest_ip = datagram.getSrcaddr();
			src_ip = datagram.getDstaddr();
			dest_port = String.valueOf(datagram.getSrcport());
			src_port = String.valueOf(datagram.getDstport());
			send_data(header.toString(), dest_port, src_port, src_ip, dest_ip);
			
		}
		else if(data[4] == 0x08)
		{
			System.out.println("Connection is established, start sending data");
		}
		
		System.out.println(data1.toString());
		return data1;
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
