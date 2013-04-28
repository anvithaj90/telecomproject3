package services;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	int window_size = 3; 
	int baseSeqNum = 0;

	private HashMap<Integer, byte[]> file_map = new HashMap<Integer, byte[]>();
	public TTPServer(){
	}

	String payload = null;
	byte[] payload_byte_array = new byte[1296]; 
	byte[] data_header = new byte[9];

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
	//	System.out.println("sent"+checksum);
	//	System.out.println(datagram.toString());
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
	//	System.out.println("received checksum = "+checksum+"flag = "+data[4]);
		if(data[8] == 1)
		{
			/* Generate a random number for the other side and set the 
			 * flag to SYN and ACK i.e. B. Get the source and the destination IP
			 * and port addresses and call send_data to send the SYN-ACK packet.
			 *  Increment the syn value of the prev  and 
			 */
			Random r = new Random();
			isn_server = r.nextInt(40000);
			baseSeqNum = isn_server;
			byte[] header = new byte[9]; //byte array to store the flag
			int received_seq_num = (int)(header[0] | header[1] << 8 | header[2] << 16| header[3] << 24);
			header = create_header(baseSeqNum, received_seq_num + 1, 'B'); // create a header with both syn and ack set

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

		else if(data[8] == 8)
		{
	//		System.out.println("Connection is established, start sending data");
			byte[] received_data = (byte[]) datagram.getData();
			int received_seq_num = received_data[3] << 24 | (received_data[2] & 0xFF) << 16 | (received_data[1] & 0xFF) << 8 | (received_data[0] & 0xFF);
			System.out.println("Ack received for data is->" + received_seq_num);
			return received_data;
		}
		else if(data[8] == 32)
		{

		//	System.out.println("came here where flag is 32 in ttp server");
			return data;

		}

	//	System.out.println(data1.toString());
		return data;
	}
	public void connection_close() { 

	}


	public void send_file(byte[] data_full, int seq_num) throws IOException, ClassNotFoundException, NoSuchAlgorithmException { 
		if(data_full == null)
		{
			boolean contains = file_map.containsKey(seq_num);
			if(contains)
			{
				baseSeqNum++;
				file_map.remove(seq_num);						
			}
		}
		else
		{
			byte[] combined = new byte[1300];
			String broken_payload = new String(data_full);
			int rem = broken_payload.length()%1285;
			if(rem == 0)
				rem+=1285;
			int i;
			int received_seq_num =(int) ( data_full[0] | data_full[1] << 8| data_full[3] << 16| data_full[4] << 24 );
			baseSeqNum++;
			for(i=0;i<data_full.length-1285;i+=1285){
				if(broken_payload.length()>1285){
			//		System.out.println("isn server value is: "+ baseSeqNum);
					data_header = create_header(baseSeqNum, received_seq_num+1 , 'D');
					payload_byte_array = create_payload(broken_payload.substring(i, i+1285));
					baseSeqNum++;

					System.arraycopy(data_header, 0, combined, 0, data_header.length);
					System.arraycopy(payload_byte_array, 0, combined, data_header.length, payload_byte_array.length);
				/*
				 * put the fragmented data into the Hash map
				 */
					add_to_hashmap(baseSeqNum, combined);
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
					
				}
			}
			
			data_header = create_header(baseSeqNum, 0 , 'F');
			
			payload_byte_array = create_payload(broken_payload.substring(i, i+rem));
			baseSeqNum++;
			byte[] combined_last = new byte[data_header.length + payload_byte_array.length];
			System.arraycopy(data_header, 0, combined_last, 0, data_header.length);
			System.arraycopy(payload_byte_array, 0, combined_last, data_header.length, payload_byte_array.length);
			send_data(combined_last, "2222", "4444", "127.0.0.1", "127.0.0.1");
		
			/*
			 * 
			 * Add MD5 after the file
			 * 
			 */
			data_header = create_header(baseSeqNum, 0 , 'M');
			//byte[] bytesOfMessage = data_full;
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(data_full);
		//	byte[] payload_byte_array_md5 = create_payload(thedigest.toString());
			baseSeqNum++;
			byte[] combined_md5 = new byte[data_header.length + thedigest.length];
			System.arraycopy(data_header, 0, combined_md5, 0, data_header.length);
			System.arraycopy(thedigest, 0, combined_md5, data_header.length, thedigest.length);
			send_data(combined_md5, "2222", "4444", "127.0.0.1", "127.0.0.1");
			System.out.println("*************************Sent MD5 = "+thedigest.toString());
		}
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
		//B == Both 
		if(flag == 'B')
			header[8] = 0x09;
		else if(flag == 'F')
			header[8] = 0x02;
		//C == close connection
		else if(flag == 'C')
			header[8] = 0x04;
		else if(flag == 'M')
			header[8] = 0x40;
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

