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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import javax.swing.Timer;

import datatypes.Datagram;

// TODO: Auto-generated Javadoc
/**
 * The Class TTPServer.
 */
public class TTPServer {

	private int isn_server;
	String dest_port = "2222";
	String src_port = "4444";
	String src_ip = "127.0.0.1";
	String dest_ip = "127.0.0.1";
	int window_size = 3; 
	int nextSeqNum = 0;
	int check = 0;
	private int baseSeqNum = 0;
	private HashMap<Integer, byte[]> file_map = new HashMap<Integer, byte[]>();
	private int timer_value = 8000;
	public TTPServer(){
		System.out.println("Enter the Window Size : ");
		Scanner scanIn = new Scanner(System.in);
		window_size = scanIn.nextInt();
		System.out.println("Enter the Timer value : ");	
		timer_value = scanIn.nextInt();
		timer_value *= 1000;
		scanIn.close(); 
		timer.setDelay(timer_value);
	}

	/** The resend data. */
	ActionListener resendData = new ActionListener(){
		public void actionPerformed(ActionEvent event){
			for(int i=baseSeqNum;i<nextSeqNum-1;i++)
			{
				try {
					send_data(file_map.get(i), dest_port, src_port, src_ip, dest_ip);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("TTP Server resends the data after timeout!");
		}
	};

	Timer timer = new Timer(timer_value,resendData);
	String payload = null;
	byte[] payload_byte_array = new byte[1294]; 
	byte[] data_header = new byte[9];
	int seq_num = 0;
	int ack = 0;

	/** The flag. */
	char flag = 'S';

	/** The ds. */
	private static DatagramService ds;

	/**
	 * Send_data.
	 *
	 * @param data the data
	 * @param dest_port the dest_port
	 * @param src_port the src_port
	 * @param src_ip the src_ip
	 * @param dest_ip the dest_ip
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void send_data(byte[] data, String dest_port, String src_port, String source_ip, String destination_ip) throws IOException	{

		Datagram datagram = new Datagram();
		datagram.setData(data);
		datagram.setSrcaddr(src_ip);
		src_ip = source_ip;
		dest_ip = destination_ip;
		datagram.setDstaddr(dest_ip);
		datagram.setDstport(Short.parseShort(dest_port));
		datagram.setSrcport(Short.parseShort(src_port));
		short checksum = calculate_checksum(datagram);
		datagram.setChecksum(checksum);
		//	System.out.println("sent"+checksum);
		//	System.out.println(datagram.toString());
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
		//		Checksum checksum = new CRC32();  
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

		/*		checksum.update(d,0,(int)d.length);   
		short value = (short) checksum.getValue(); //this is the real checksum
		return value;*/		
	}

	/**
	 * Receive_data.
	 *
	 * @param port the port
	 * @return the byte[]
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public byte[] receive_data(String port) throws ClassNotFoundException, IOException {
		if(ds==null){
			ds = new DatagramService(Short.parseShort(port), 10);
		}
		Datagram datagram = new Datagram();
		datagram = ds.receiveDatagram();

		byte[] data = (byte[]) datagram.getData();	//data from the datagram
		byte[] data1 = new byte[1294]; //byte array for data
		byte[] temp = new byte[1294+9];
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
			nextSeqNum = isn_server;
			baseSeqNum = isn_server;
			check = isn_server;
			byte[] header = new byte[9]; //byte array to store the flag
			int received_seq_num = (int)(header[0] | header[1] << 8 | header[2] << 16| header[3] << 24);
			header = create_header(nextSeqNum, 0, 'B'); // create a header with both syn and ack set

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

			byte[] received_data = (byte[]) datagram.getData();
			//	int received_seq_num = received_data[3] << 24 | (received_data[2] & 0xFF) << 16 | (received_data[1] & 0xFF) << 8 | (received_data[0] & 0xFF);
			return received_data;
		}
		else if(data[8] == 32)
		{
			return data;
		}
		else if(data[8] == 4)
		{
			System.out.println("server received FIN_CLOSE");
			byte[] received_data = (byte[]) datagram.getData();
			int received_seq_num = (int)(received_data[0] | received_data[1] << 8 | received_data[2] << 16| received_data[3] << 24);
			System.out.println("server sends FIN_ACK++++++++++++++++++++++++");
			data_header = create_header(isn_server, received_seq_num + 1, 'V');
			send_data(data_header, dest_port, src_port, src_ip, dest_ip);

			isn_server++;

			System.out.println("server sends FIN_close++++++++");
			data_header = create_header(isn_server, received_seq_num + 1, 'C');
			send_data(data_header, dest_port, src_port, src_ip, dest_ip);

			temp =receive_data(src_port);
			while(temp[8]!=5)
			{
				temp = receive_data(src_port);
			}

		}
		if(temp[8] == 5)
		{
	//		file_map.clear();

			System.out.println("server receeived FIN_ACK connection closed at server");
		}
		return data;
	}

	/**
	 * Send_file.
	 *
	 * @param data_full the data_full
	 * @param seq_num the seq_num
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public void send_file(byte[] data_full, int seq_num) throws IOException, ClassNotFoundException, NoSuchAlgorithmException { 

		if(data_full == null)
		{
			boolean contains = file_map.containsKey(seq_num-1);
			if(contains)
			{
				int i;

				if(file_map.containsKey(seq_num-1))
				{
					System.out.println("removing from hashmap:" + (seq_num-1));
					timer.stop();
					file_map.remove(seq_num-1);	
					System.out.println("Now the Hash Map is:");
					Set set = file_map.entrySet();
					Iterator it =set.iterator();
					while(it.hasNext())
					{
						Map.Entry me = (Map.Entry)it.next();
						System.out.print(me.getKey() + " : ");
						System.out.println(me.getValue());
					}
				}

				baseSeqNum++;
				for(i=0;i<window_size;i++){
					if(nextSeqNum <= baseSeqNum + window_size)
					{

						if(file_map.containsKey(nextSeqNum))
						{
							System.out.println("the message i am sending is : " + nextSeqNum);
							send_data(file_map.get(nextSeqNum),  dest_port, src_port, src_ip, dest_ip);
							if(baseSeqNum == nextSeqNum)
								timer.start();
						}		
						nextSeqNum++;		
					}	
				}

			}
		}
		else
		{

			String broken_payload = new String(data_full);
			int rem = broken_payload.length()%1285;
			if(rem == 0)
				rem+=1285;
			int i;
			int received_seq_num =(int) ( data_full[0] | data_full[1] << 8| data_full[3] << 16| data_full[4] << 24 );
			for(i=0;i<data_full.length-1285;i+=1285){
				byte[] combined = new byte[1294];
				data_header = create_header(nextSeqNum, received_seq_num+1 , 'D');
				payload_byte_array = create_payload(broken_payload.substring(i, i+1285));


				System.arraycopy(data_header, 0, combined, 0, data_header.length);
				System.arraycopy(payload_byte_array, 0, combined, data_header.length, payload_byte_array.length);
				/*
				 * put the fragmented data into the Hash map
				 */
				add_to_hashmap(nextSeqNum, combined);
				nextSeqNum++;
			}

			data_header = create_header(nextSeqNum, 0 , 'F');

			payload_byte_array = create_payload(broken_payload.substring(i, i+rem));
			byte[] combined_last = new byte[payload_byte_array.length + data_header.length];
			System.arraycopy(data_header, 0, combined_last, 0, data_header.length);
			System.arraycopy(payload_byte_array, 0, combined_last, data_header.length, payload_byte_array.length);
			add_to_hashmap(nextSeqNum, combined_last);
			nextSeqNum++;

			data_header = create_header(nextSeqNum, 0 , 'M');

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(data_full);
			String temp5 = new String(data_full); 

			byte[] combined_md5 = new byte[data_header.length + thedigest.length];
			System.arraycopy(data_header, 0, combined_md5, 0, data_header.length);
			System.arraycopy(thedigest, 0, combined_md5, data_header.length, thedigest.length);
			add_to_hashmap(nextSeqNum, combined_md5);
			Set set = file_map.entrySet();
			Iterator it =set.iterator();
			while(it.hasNext())
			{
				Map.Entry me = (Map.Entry)it.next();
				System.out.print(me.getKey() + " : ");
				System.out.println(me.getValue());
			}


			nextSeqNum = check;
			for(i=0;i<data_full.length-1285;i+=1285){
				if( nextSeqNum < baseSeqNum + window_size){
					System.out.println("I am sending before ack with this :" + nextSeqNum);

					send_data(file_map.get(nextSeqNum),  dest_port, src_port, src_ip, dest_ip);
					if(baseSeqNum == nextSeqNum)
						timer.start();
					nextSeqNum++;	
				}	
			}
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
		byte[] header = new byte[9];
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
		case 'M': header[8] = 0x40;
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
	 * Create_payload.
	 *
	 * @param payload the payload
	 * @return the byte[]
	 */
	public byte[] create_payload(String payload) {
		byte[] payload_byte = payload.getBytes(); 	    	
		return payload_byte;
	}

	/**
	 * Add_to_hashmap.
	 *
	 * @param isn the isn
	 * @param hash_data the hash_data
	 */
	public void add_to_hashmap(Integer isn, byte[] hash_data)
	{
		file_map.put(isn, hash_data);
	}
}

