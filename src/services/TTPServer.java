package services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import datatypes.Datagram;

public class TTPServer {
	private int isn_server;
	private int isn_data;
	private HashMap<Integer, byte[]> file_map = new HashMap<Integer, byte[]>();
	public TTPServer(){
	}
	//	String payload= "Hello World!";
	String payload = null;
	byte[] payload_byte_array = new byte[1296]; 
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
		byte[] data1 = new byte[1296]; //byte array for data
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
		/*	byte[] received_data = (byte[]) datagram.getData();
			int received_seq_num = received_data[0] << 8 | received_data[1];
			System.out.println(received_seq_num);*/
		}
		else if(data[4] == 32)
		{

			System.out.println("came here where flag is 32 in ttp server");
			return data;

		}

		System.out.println(data1.toString());
		return data;
	}
	public void connection_close() { 

	}


	public void send_file(byte[] data_full) throws IOException, ClassNotFoundException { 
		//byte[] combined = new byte[data_header.length + data_full.length];
		byte[] combined = new byte[1300];
		String broken_payload = new String(data_full);
		int rem = broken_payload.length()%1295;
		if(rem == 0)
			rem+=1295;
		int i;
		isn_server++;
		for(i=0;i<data_full.length-1295;i+=1295){
			if(broken_payload.length()>1295){
				data_header = create_header(isn_server, 0 , 'D');
				payload_byte_array = create_payload(broken_payload.substring(i, i+1295));
				isn_server++;

				System.arraycopy(data_header, 0, combined, 0, data_header.length);
				System.arraycopy(payload_byte_array, 0, combined, data_header.length, payload_byte_array.length);
			/*
			 * put the fragmented data into the Hash map
			 */
				add_to_hashmap(isn_server, combined);
				//Printing Hashmap
				Set set = file_map.entrySet();
				Iterator it =set.iterator();
				while(it.hasNext())
				{
					Map.Entry me = (Map.Entry)it.next();
					System.out.print(me.getKey() + " : ");
					System.out.println(me.getValue());
				}
				send_data(combined, "2222", "4444", "127.0.0.1", "127.0.0.1");
			/*	byte[] temp = receive_data("4444");
				while(true)
				{
					 temp = receive_data("4444");
				}*/
			}
		}
		data_header = create_header(isn_server, 0 , 'F');
		payload_byte_array = create_payload(broken_payload.substring(i, i+rem));
		isn_server++;
		byte[] combined_last = new byte[data_header.length + payload_byte_array.length];
		System.arraycopy(data_header, 0, combined_last, 0, data_header.length);
		System.arraycopy(payload_byte_array, 0, combined_last, data_header.length, payload_byte_array.length);
		send_data(combined_last, "2222", "4444", "127.0.0.1", "127.0.0.1");
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
	
	public void add_to_hashmap(Integer isn, byte[] hash_data)
	{
		file_map.put(isn, hash_data);
	}
}
