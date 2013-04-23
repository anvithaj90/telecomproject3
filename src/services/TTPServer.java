package services;

import java.io.IOException;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import datatypes.Datagram;

public class TTPServer {
	private int isn_server;
	private int isn_data;
	public TTPServer(){
	}
//	String payload= "Hello World!";
	String payload = null;
	byte[] payload_byte_array = new byte[50]; 
	byte[] data_header = new byte[5];

	int seq_num = 0;
	int ack = 0;
	char flag = 'S';
	
	private static DatagramService ds;

	public void send_data(byte[] data, String dest_port, String src_port, String src_ip, String dest_ip) throws IOException	{
			
		Datagram datagram = new Datagram();
		datagram.setData(data);
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
		byte[] d = (byte[])datagram.getData();
		checksum.update(d,0,(int)d.length);   
		short value = (short) checksum.getValue(); //this is the real checksum
		return value;		
	}
	public byte[] receive_data(String port) throws ClassNotFoundException, IOException {
		if(ds==null){
			ds = new DatagramService(Short.parseShort(port), 10);
		}
		Datagram datagram = new Datagram();
		datagram = ds.receiveDatagram();
		
		byte[] data = (byte[]) datagram.getData();	//data from the datagram
		byte[] data1 = new byte[50]; //byte array for data
		short checksum;
		checksum = datagram.getChecksum();	
		System.out.println("received checksum = "+checksum+"flag = "+data[4]);
		if(data[4] == 1)
		{
			/* Generate a random number for the other side and set the 
			 * flag to SYN and ACK i.e. B. Get the source and the destination IP
			 * and port addresses and call send_data to send the SYN-ACK packet.
			 *  Increment the syn value of the prev  and 
			 */
			Random r = new Random();
			isn_server = r.nextInt(65535);
			byte[] header = new byte[5]; //byte array to store the flag
			int received_seq_num = header[0] << 8 | header[1];
			header = create_header(isn_server, received_seq_num + 1, 'B'); // create a header with both syn and ack set
			
			String dest_port;
			String src_port;
			String src_ip;
			String dest_ip;
			dest_ip = datagram.getSrcaddr();
			src_ip = datagram.getDstaddr();
			dest_port = String.valueOf(datagram.getSrcport());
			src_port = String.valueOf(datagram.getDstport());
			send_data(header, dest_port, src_port, src_ip, dest_ip);
		}

		else if(data[4] == 8)
		{
			String src_port;
			src_port = String.valueOf(datagram.getDstport());
			System.out.println("Connection is established, start sending data");
			byte[] data2=data;
		/*	while(true)
			{
				 data2 = receive_data(src_port);
				 System.out.println(data2[5]);
			}*/
		}
		else if(data[4] == 32)
		{
			/*int i;
			int j = 0;
			byte[] new_data = new byte[data.length];
			
			for(i=5;i<data.length;i++,j++)
			{
				new_data[j] = data[i];
			}
			String received_file = new String(new_data);
		    System.out.println(received_file);*/
	//	    byte[] data_to_send = fs.getfile_from_ftp(received_file);
		    //payload_byte_array = data_to_send;
		    System.out.println("came here");
			return data;
		   // String temp = new String(data_to_send);
		   // payload = temp;
		}
		else if(data[4]==16)
		{
			String dest_port;
			String src_port;
			String src_ip;
			String dest_ip;
			dest_ip = datagram.getSrcaddr();
			src_ip = datagram.getDstaddr();
			dest_port = String.valueOf(datagram.getSrcport());
			src_port = String.valueOf(datagram.getDstport());
			/*
			 * start sending data along with 'C' flag set to indicate the start 
			 * of data. Generate a new sequence number called isn_data for the 
			 * data.
			 */
			Random r = new Random();
			isn_data = r.nextInt(65535);
			byte[] combined_data = new byte[data_header.length + payload_byte_array.length];
			for(int i=0;i<payload.length()-2;i+=2){
				  if(payload.length()>2){
					  data_header = create_header(isn_data, 0 , 'D');
					  payload_byte_array = create_payload(payload.substring(i, i+2));
				  }
				  System.arraycopy(data_header, 0, combined_data, 0, data_header.length);
				  System.arraycopy(payload_byte_array, 0, combined_data, data_header.length, payload_byte_array.length);			
				  send_data(combined_data, dest_port, src_port, src_ip, dest_ip);
			}
			
			/*
			 * For the last packet add the FIN flag
			 */
			 data_header = create_header(isn_data, 0 , 'F');
			 payload_byte_array = create_payload(payload.substring(payload.length()-2, payload.length()));
			 System.arraycopy(data_header, 0, combined_data, 0, data_header.length);
			 System.arraycopy(payload_byte_array, 0, combined_data, data_header.length, payload_byte_array.length);			
			 send_data(combined_data, dest_port, src_port, src_ip, dest_ip);
		}
		System.out.println(data1.toString());
		return data;
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
